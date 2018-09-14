package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.MsgUtils;
import static com.maxfill.escom.utils.MsgUtils.getBandleLabel;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.task.result.ResultFacade;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.companies.Company;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.task.TaskStates;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.primefaces.event.SelectEvent;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DualListModel;
import org.springframework.util.CollectionUtils;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseCardBean<Task>{
    private static final long serialVersionUID = -2860068605023348908L;

    @EJB
    private TaskFacade taskFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private ResultFacade resultFacade;
    @EJB
    private ProcessFacade processFacade;    
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private DocBean docBean;

    private boolean readOnly;

    private List<Result> taskResults;
    private DualListModel<Result> results;
    private String currentResult;
    
    private int deadLineDeltaDay = 0;
    private int deadLineDeltaHour = 0;
    private int reminderDeltaDay = 0;
    private int reminderDeltaHour = 0;
    private int reminderDeltaMinute = 0;
      
    private ProcReport currentReport;
    
    private List<String> selectedDays;
    
    private List<SelectItem> daysOfWeek = new ArrayList<>();
    {        
        daysOfWeek.add(new SelectItem("Sun", getBandleLabel("SUNDAY")));
        daysOfWeek.add(new SelectItem("Mon", getBandleLabel("MONDAY")));
        daysOfWeek.add(new SelectItem("Tue", getBandleLabel("TUESDAY")));
        daysOfWeek.add(new SelectItem("Wed", getBandleLabel("WEDNESDAY")));
        daysOfWeek.add(new SelectItem("Thu", getBandleLabel("THURSDAY")));
        daysOfWeek.add(new SelectItem("Fri", getBandleLabel("FRIDAY")));
        daysOfWeek.add(new SelectItem("Sat", getBandleLabel("SATURDAY")));
    }
    
    @Override
    public void doPrepareOpen(Task task){              
        TaskStates taskStates = task.getState();
        State state = taskStates.getCurrentState();
        int id = state.getId();
        if (task.getScheme() != null && (DictStates.STATE_RUNNING == id || DictStates.STATE_COMPLETED == id)){
            readOnly = true;
        }
        initDateFields(task);        
    }
      
    /**
     * Проверка корректности задачи 
     * @param task
     * @param errors 
     */
    @Override
    protected void checkItemBeforeSave(Task task, Set<String> errors) {
        saveDateFields(task);
        super.checkItemBeforeSave(task, errors);
        ///проверка наличия результатов
        if (StringUtils.isEmpty(task.getAvaibleResultsJSON())){
            errors.add(MsgUtils.getMessageLabel("TaskNoHaveListResult"));
        }        
        if (task.getOwner() == null){
            errors.add(MsgUtils.getMessageLabel("ExecutorNotSet"));
        }
        //проверка для срока исполнения
        switch (task.getDeadLineType()){
            case "delta":{
                if (task.getDeltaDeadLine() == 0){
                   errors.add(MsgUtils.getMessageLabel("DeadlineIncorrect"));
                }
                if (task.getBeginDate() == null){
                    errors.add(MsgUtils.getMessageLabel("DateBeginNoSet"));
                }
                break;
            }
            case "data":{
                if (task.getPlanExecDate() == null ){
                    errors.add(MsgUtils.getMessageLabel("DeadlineIncorrect"));
                } 
                /*
                else 
                    if (task.getPlanExecDate().before(new Date())){
                        errors.add(MsgUtils.getMessageLabel("DeadlineSpecifiedInPastTime"));
                    }
                */
            }
        }
        //проверка для напоминания
        switch (task.getReminderType()){
            case "repeat":{                
                switch (task.getReminderRepeatType()){
                    case "everyday":{
                        if (task.getReminderTime() == null){
                            errors.add(MsgUtils.getMessageLabel("ReminderTimeNotSet"));
                        }
                        break;
                    }
                    case "everyweek":{
                        if (selectedDays == null){
                            errors.add(MsgUtils.getMessageLabel("ReminderPeriodIncorrect"));
                        }
                        break;
                    }
                    default:{
                        errors.add(MsgUtils.getMessageLabel("InternalErrorSavingTask"));
                    }
                }
                break;
            }
            case "singl":{
                break;
            }            
        }        
    }
    
    /**
     * Проверка задачи перед её выполнением
     * @param task
     * @param result
     * @param errors 
     */
    private void checkTaskBeforeExecute(Task task, Result result, Set<String> errors ){
        if (StringUtils.isEmpty(task.getComment()) || task.getComment().length() < 3){
            switch (result.getName()){
                case DictResults.RESULT_CANCELLED :{
                    errors.add(MsgUtils.getMessageLabel("ReportIsNotFilled"));
                    break;
                }
                case DictResults.RESULT_REFUSED :{
                    errors.add(MsgUtils.getMessageLabel("ReportIsNotFilled"));
                    break;
                }
                case DictResults.RESULT_AGREE_WITH_REMARK :{
                    errors.add(MsgUtils.getMessageLabel("ReportIsNotFilled"));
                    break;
                }
            }
        }
    }
    
    @Override
    protected void onBeforeSaveItem(Task task){
        saveDateFields(task);
        super.onBeforeSaveItem(task);
    }
    
    @Override
    protected void onAfterSaveItem(Task task){
        //изменение в листе согласования процесса, если изменили задачу        
        if (task.getScheme() == null) return;   //задача не связана с процессом                 
        if (getTypeEdit() == DictEditMode.CHILD_MODE) return ;   
        workflow.replaceReportExecutor(task, getCurrentUser());               
    } 
     
    /**
     * Обработка события выполнения задачи
     * @return 
     */
    public String onExecute(){ 
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();  
        currentResult = params.get("result");
        if (StringUtils.isEmpty(currentResult)) return "";
        
        List<Result> rs = resultFacade.findByName(currentResult);
        Result result = rs.get(0);
        Set<String> errors = new HashSet<>();
        
        Task task = getEditedItem();                 
        checkTaskBeforeExecute(task, result, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return "";
        } 
        
        doSaveItem();        
        task = (Task) sourceBean.getSourceItem(); 
        if (task.getScheme() != null){
            Process process;
            if (sourceBean instanceof ProcessCardBean){
                process = ((ProcessCardBean)sourceBean).getEditedItem();
            } else {
                process = processFacade.find(task.getScheme().getProcess().getId());
            }

            workflow.executeTask(process, task, result, getCurrentUser(), errors);
            if (!errors.isEmpty()){
                MsgUtils.showErrorsMsg(errors);
                return "";
            }
        } else {
            taskFacade.taskDone(task, result, getCurrentUser());
        }
        return closeItemForm(SysParams.EXIT_EXECUTE);
    }
    
    /**
     * Обработка события открытия карточки процесса 
     */
    public void onOpenProcess(){
        Process process = getProcess();
        if (process != null){
            processBean.prepEditItem(process, getParamsMap());
        } else {
            MsgUtils.errorMsg("LinkProcessIncorrect");
        }
    }
    
    /**
     * Обработка события открытия карточки документа 
     */
    public void onOpenDocument(){        
        Process process = getProcess();        
        if (process == null){ 
            MsgUtils.errorMsg("LinkProcessIncorrect");
            return;
        }
        Doc doc = process.getDocument();
        if (doc == null){
            MsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            return;
        }        
        docBean.prepEditItem(doc, getParamsMap());                
    }
    
    /**
     * Обработка события просмотра документа
     */
    public void onViewDocument(){
        Process process = getProcess();
        if (process == null){ 
            MsgUtils.errorMsg("LinkProcessIncorrect");
            return;
        }        
        Doc doc = process.getDocument();
        if (doc == null){
            MsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            return;
        }        
        if (doc != null){
            docBean.onViewMainAttache(doc);
        } else {
            MsgUtils.errorMsg("DocumentDoNotContainMajorVersion");
        }    
    }
    
    private Process getProcess(){
        Scheme scheme = getEditedItem().getScheme();
        if (scheme == null) return null;
        return scheme.getProcess();
    }
    
    /**
     * Обработка события выбора Исполнителя
     * @param event
     */
    public void onExecutorChanged(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()) return;
        getEditedItem().setOwner(items.get(0));
        onItemChange();
    }
    public void onExecutorChanged(ValueChangeEvent event){
         getEditedItem().setOwner((Staff) event.getNewValue());
    }
    
    public Boolean isShowExtTaskAtr(){
        boolean flag = false;
        if (getEditedItem() != null && getEditedItem().getScheme() == null){
            flag = true;
        }
        return flag;
    }
    
    private void initDateFields(Task task){        
        //восстанавливаем срок задачи в формате дни часы
        long hoursInMilli = 3600;
        long daysInMilli = hoursInMilli * 24;
        if (task.getDeltaDeadLine() > 0){
            long deltaSec = task.getDeltaDeadLine();

            Long elapsedDays = deltaSec / daysInMilli;
            deadLineDeltaDay = elapsedDays.intValue();
            deltaSec = deltaSec % daysInMilli;

            Long elapsedHours = deltaSec / hoursInMilli;
            deadLineDeltaHour = elapsedHours.intValue();        
        }
        //восстанавливаем время напоминания в формате дни часы минуты
        if (task.getDeltaReminder() >0){
            long remDeltaSec = task.getDeltaReminder();
            Long remDays = remDeltaSec / daysInMilli;
            reminderDeltaDay = remDays.intValue();
            remDeltaSec = remDeltaSec % daysInMilli;

            Long remHour = remDeltaSec / hoursInMilli;
            reminderDeltaHour = remHour.intValue();
            remDeltaSec = remDeltaSec % hoursInMilli;
            
            Long remMinute = remDeltaSec / 60;
            reminderDeltaMinute = remMinute.intValue();            
        } 
        
        if (StringUtils.isNotEmpty(task.getReminderDays())){
            String[] arr = task.getReminderDays().split(",");
            selectedDays = new ArrayList<>(Arrays.asList(arr));            
        }
    }
    
    private void saveDateFields(Task task){
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        task.setDeltaDeadLine(seconds);
        if (selectedDays != null){
            task.setReminderDays(String.join(",", selectedDays));
        }
        if ("singl".equals(task.getReminderType())){
            int sec = reminderDeltaDay * 86400;
            sec = sec + reminderDeltaHour * 3600;
            sec = sec + reminderDeltaMinute * 60;
            task.setDeltaReminder(sec);
        }
    }
    
    /**
     * Вычисление планового срока исполнения
     */
    public void calculateDeadline(){
        Task task = getEditedItem();
        Set<String> errors = new HashSet<>();
        if (deadLineDeltaDay == 0 && deadLineDeltaHour == 0){
            errors.add("DeadlineIncorrect");            
        }
        if (task.getBeginDate() == null){
            errors.add("DateBeginNoSet");            
        }
        if (task.getOwner() == null){
            errors.add("ExecutorNotSet");
        }
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        task.setDeltaDeadLine(seconds);
        taskFacade.makeDatePlan(task, getLocale());
        String strDate = DateUtils.dateToString(task.getPlanExecDate(),  DateFormat.SHORT, DateFormat.MEDIUM, getLocale());
        MsgUtils.succesFormatMsg("DeadlineCalcWorkingCalendar", new Object[]{strDate});
    }
    
    /**
     * Формирует локализованное наименование дня недели по его значению
     * @param day
     * @return 
     */
    public String getDayWeekName(Integer day){
        DayOfWeek dayOfWeek = DayOfWeek.of(day);
        return getLabelFromBundle(dayOfWeek.name());
    }
    
    public void onOpenExeReport(ProcReport report){
        currentReport = report;
    }
    
    /**
     * Проверка даты при изменении срока исполнения
     * @param event 
     */
    public void onPlanExecDateChange(ValueChangeEvent event){
        Date newValue = (Date) event.getNewValue();
        Staff staff = getEditedItem().getOwner();
        Company company = staffFacade.findCompanyForStaff(staff);
        //TODO нужно починить
        /*
        if (staff != null && workTimeService.isHolliday(newValue, staff, company)){
            MsgUtils.warnMsg("SelectedDateIsWeekend");
        }
        */
    }
    
    /* GETS & SETS */
    
    @Override
    public Integer getRightColSpan(){
        return 7;
    }
        
    public int getReminderDeltaDay() {
        return reminderDeltaDay;
    }
    public void setReminderDeltaDay(int reminderDeltaDay) {
        this.reminderDeltaDay = reminderDeltaDay;
    }

    public int getReminderDeltaHour() {
        return reminderDeltaHour;
    }
    public void setReminderDeltaHour(int reminderDeltaHour) {
        this.reminderDeltaHour = reminderDeltaHour;
    }

    public int getReminderDeltaMinute() {
        return reminderDeltaMinute;
    }
    public void setReminderDeltaMinute(int reminderDeltaMinute) {
        this.reminderDeltaMinute = reminderDeltaMinute;
    }
    
    public int getDeadLineDeltaDay() {
        return deadLineDeltaDay;
    }
    public void setDeadLineDeltaDay(int deadLineDeltaDay) {
        this.deadLineDeltaDay = deadLineDeltaDay;
    }

    public int getDeadLineDeltaHour() {
        return deadLineDeltaHour;
    }
    public void setDeadLineDeltaHour(int deadLineDeltaHour) {
        this.deadLineDeltaHour = deadLineDeltaHour;
    }    

    public ProcReport getCurrentReport() {
        return currentReport;
    }
    public void setCurrentReport(ProcReport currentReport) {
        this.currentReport = currentReport;
    }
        
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public DualListModel<Result> getResults() {
        if (results == null){
            List<Result> allResults = resultFacade.findAll(getCurrentUser());
            allResults.removeAll(getTaskResults());
            results = new DualListModel<>(allResults, getTaskResults());
        }
        return results;
    }
    public void setResults(DualListModel<Result> results) {
        this.results = results;
        getEditedItem().setResults(results.getTarget());                
    }   
        
    public List<Result> getTaskResults() {
        if (taskResults == null){
            taskResults = resultFacade.findTaskResults(getEditedItem());
        }
        return taskResults;
    }
    
    public String getTaskStatus(){
        return sessionBean.getItemStatus(getEditedItem());
    }
    
    @Override
    protected BaseDictFacade getFacade() {
        return taskFacade;
    }
    
    public List<SelectItem> getDaysOfWeek() {
        return daysOfWeek;
    }
    public void setDaysOfWeek(List<SelectItem> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    public List<String> getSelectedDays() {
        return selectedDays;
    }
    public void setSelectedDays(List<String> selectedDays) {
        this.selectedDays = selectedDays;
    }
    
    
}