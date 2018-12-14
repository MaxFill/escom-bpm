package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.utils.MsgUtils;
import static com.maxfill.escom.utils.MsgUtils.getBandleLabel;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.result.ResultFacade;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.assistant.AssistantFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.Tuple;
import java.text.DateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import java.util.Date;
import java.util.HashMap;
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
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

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
    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private AssistantFacade assistantFacade;
    @EJB
    private DocFacade docFacade;
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private DocBean docBean;

    private List<Result> taskResults;
    private DualListModel<Result> results;
    
    private int deadLineDeltaDay = 0;
    private int deadLineDeltaHour = 0;
    private int reminderDeltaDay = 0;
    private int reminderDeltaHour = 0;
    private int reminderDeltaMinute = 0;
      
    private ProcReport currentReport;
    private boolean isNeedUpdateProcessReports; 
    private List<String> selectedDays;
    private List<Staff> executors;
    private List<BaseDict> forShow;
    private String resultName;
    
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
        initDateFields(task);
        executors = initExecutors(task).stream()
                .filter(staff->Objects.nonNull(staff))
                    .sorted(Comparator.comparing(Staff::getName, nullsFirst(naturalOrder())))
                    .collect(Collectors.toList());
    }

    /**
     * Проверка корректности задачи 
     * @param task
     * @param errors 
     */
    @Override
    protected void checkItemBeforeSave(Task task, FacesContext context, Set<String> errors) {
        saveDateFields(task);
        super.checkItemBeforeSave(task, context, errors);
        
        //проверка наличия результатов
        if (StringUtils.isBlank(task.getAvaibleResultsJSON())){
            errors.add(MsgUtils.getMessageLabel("TaskNoHaveListResult"));
        }        
        if (task.getOwner() == null && task.getRoleInProc() == null){
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:selExecutor");
            input.setValid(false);
            errors.add(MsgUtils.getMessageLabel("ExecutorNotSet"));
        }
        
        //проверка срока исполнения
        switch (task.getDeadLineType()){
            case "delta":{
                if (task.getDeltaDeadLine() == 0){
                   errors.add(MsgUtils.getMessageLabel("DeadlineIncorrect"));
                }
                if (task.getBeginDate() == null && task.getScheme() == null){ //если задача из процесса, то дата начала не нужна
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
        if (task.getReminderType() == null){
            task.setReminderType("no");
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
    private void checkTaskBeforeExecute(Task task, Result result, Set<Tuple> errors ){        
        switch (result.getName()){
            case DictResults.RESULT_CANCELLED :{
                checkReport(task, errors);
                break;
            }
            case DictResults.RESULT_REFUSED :{
                checkReport(task, errors);
                if (!isHaveActualRemarks(task, errors)){
                    errors.add(new Tuple("NoYourRemarksActionRequiresRemarks", new Object[]{}));
                }
                break;
            }
            case DictResults.RESULT_AGREE_WITH_REMARK :{
                checkReport(task, errors);                
                if (!isHaveActualRemarks(task, errors)){
                    errors.add(new Tuple("NoYourRemarksActionRequiresRemarks", new Object[]{}));
                }
                break;
            }
            case DictResults.RESULT_AGREED :{
                if (isHaveActualRemarks(task, errors)){
                    errors.add(new Tuple("ActionNotAvailableHaveActualRemarks", new Object[]{}));
                }
                break;
            }
        }        
    }
    
    private void checkReport(Task task, Set<Tuple> errors){
        if (StringUtils.isEmpty(task.getComment()) || task.getComment().length() < 3){
            errors.add(new Tuple("ReportIsNotFilled", new Object[]{}));
        }
    }
    
    /**
     * Проверка наличия у текущего пользователя актуальных (не снятых) замечаний
     * @param task
     * @param user
     * @param errors 
     */
    private boolean isHaveActualRemarks(Task task, Set<Tuple> errors){
        Process process = task.getScheme().getProcess();
        Doc doc = process.getDocument();
        if (doc == null) return false;
        doc = docFacade.find(doc.getId());
        if (doc == null) return false;
        Remark remark = doc.getDetailItems().stream()
                .filter(r -> Objects.equals(r.getAuthor(), getCurrentUser()) 
                    && DictStates.STATE_ISSUED == r.getState().getCurrentState().getId())
                .findFirst()
                .orElse(null);
        return remark != null;
    }
    
    /**
     * Опредеяет, должно ли быть заполнено поле исполнитель
     * @return 
     */
    public boolean isRequiredExecutor(){
        if (getEditedItem().getRoleInProc() != null) return false;
        return getEditedItem().getOwner() == null;
    }
    
    @Override
    protected void onBeforeSaveItem(Task task){
        saveDateFields(task);
        /*
        if (isChangeExecutor){
            replaceReportExecutor(task, getCurrentUser());
        }
        */
        taskFacade.actualizeRoles(task);
        super.onBeforeSaveItem(task);
    }
    
    @Override
    protected void onAfterSaveItem(Task task){
        //изменение в листе согласования процесса, если изменили задачу        
        if (task.getScheme() == null) return;   //задача не связана с процессом 
        if (getTypeEdit() == DictEditMode.CHILD_MODE) return;
        if (isNeedUpdateProcessReports){
            workflow.makeProcessReport(task.getScheme().getProcess(), getCurrentUser());
        }
    }
     
    /**
     * Обработка события выполнения задачи
     * @param resultName
     * @return 
     */
    public String onExecute(String resultName){         
        return onExecute();        
    }        
    
    /**
     * Выполнение задачи
     * @return 
     */
    public String onExecute() {    
        Task task = getEditedItem();

        List<Result> rs = resultFacade.findByName(resultName);
        Result result = rs.get(0);
                
        if (StringUtils.isBlank(task.getComment())){
            task.setComment(MsgUtils.getBandleLabel(resultName));
        }

        Set<Tuple> errors = new HashSet<>();
                                 
        checkTaskBeforeExecute(task, result, errors);
        if (!errors.isEmpty()){
            MsgUtils.showTupleErrsMsg(errors);            
            return "";            
        }
        
        doSaveItem();
                
        if (task.getScheme() == null){
            taskFacade.taskDone(task, result, getCurrentUser());
            return closeTaskForm(); //завершаем задачу, если она не связана с процессом
        }
        
        Process process = processFacade.find(task.getScheme().getProcess().getId());
        forShow = new ArrayList<>(workflow.executeTask(process, task, result, getCurrentUser(), new HashMap<>(), errors));
        if (!errors.isEmpty()){
            MsgUtils.showTupleErrsMsg(errors);
            return "";
        }        
        
        if (!forShow.isEmpty()){ //если есть что нужно инициализировать, то показываем диалоговое окно            
            setEditedItem(taskFacade.find(getEditedItem().getId()));
            PrimeFaces.current().ajax().update("mainFRM");
            PrimeFaces.current().ajax().update("initObjFRM");
            PrimeFaces.current().executeScript("PF('InitObjectsWV').show();");            
            return "";
        }
        return closeTaskForm();                
    }
    
    public void onUpdateProcesses(){
        forShow = forShow.stream()
                .map(proc->processFacade.find(proc.getId()))
                .filter(proc->Objects.equals(proc.getState().getCurrentState().getId(), DictStates.STATE_DRAFT))
                .collect(Collectors.toList());
        if(forShow.isEmpty()){
            PrimeFaces.current().executeScript("PF('InitObjectsWV').hide();");
        } else {
            PrimeFaces.current().ajax().update("initObjFRM");
        }
    }
    
    /**
     * Закрытие карточки задачи после запуска из-неё подпроцесса
     * @return 
     */    
    public String closeTaskForm() {
        //setEditedItem(taskFacade.find(getEditedItem().getId()));
        //PrimeFaces.current().ajax().update("mainFRM");        
        return closeItemForm(SysParams.EXIT_EXECUTE);
    }
    
    /**
     * Формирование параметра для открытия процесса в мониторе по url
     * @return 
     */
    public String paramUrlProc(){
        String result = "";
        Process process = (Process)getProcess().getRoot();
        if (process != null){
            result = "?procId=" + process.getId();
        }
        return result;
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
        Map<String, List<String>> params = getParamsMap();
        params.put("processID", Collections.singletonList(process.getId().toString()));
        params.put("taskID", Collections.singletonList(getEditedItem().getId().toString()));
        docBean.doViewMainAttache(doc, params);
    }
    
    public Process getProcess(){
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
        isNeedUpdateProcessReports = true;
        onItemChange();
    }
    public void onExecutorChanged(ValueChangeEvent event){
        getEditedItem().setOwner((Staff) event.getNewValue());
        isNeedUpdateProcessReports = true;
        onItemChange();
    }
    public void onExecutorChanged(){        
        isNeedUpdateProcessReports = true;
        onItemChange(); 
        if (isShowBtnResults()){
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:btnTaskExe");
        }
    }
    
    public void onConsidInProcReportChange(){
        isNeedUpdateProcessReports = true;
    }
    
    public Boolean isShowExtTaskAtr(){
        boolean flag = false;
        if (getEditedItem() != null && getEditedItem().getScheme() == null){
            flag = true;
        }
        return flag;
    }
    
    /**
     * Формирование списка доступных для выбора Исполнителей
     * @param task 
     */
    private Set<Staff> initExecutors(Task task){        
        Set<Staff> staffs = new HashSet<>();
        boolean isCurator = false;
        if (task.getScheme() != null){
            Staff curator = task.getScheme().getProcess().getCurator();
            if (curator != null){
                isCurator = Objects.equals(curator.getEmployee(), getCurrentUser());
            }
        }
        //админ или куратор процесса может выбрать любого
        if (userFacade.isAdmin(getCurrentUser()) || isCurator){
            return new HashSet<>(staffFacade.findActualStaff());            
        }        
        //владелец может выбрать себя или кого-то из своих замов
        if (task.getAuthor().equals(getCurrentUser())) {
            staffs.addAll(assistantFacade.findAssistByUser(getCurrentUser()));
            staffs.add(task.getOwner());
            if (getCurrentUser().getStaff() != null){
                staffs.add(getCurrentUser().getStaff());
            }
            return staffs;
        }        
        if (task.getOwner() != null){
            staffs.add(task.getOwner());
            //исполнитель может выбрать кого-то из своих замов
            if (task.getOwner().getEmployee().equals(getCurrentUser())) {
                staffs.addAll(assistantFacade.findAssistByUser(getCurrentUser())); 
                return staffs;
            }
            //руководитель исполнителя, может выбрать себя или кого-то из замов
            if (assistantFacade.isChief(getCurrentUser(), task.getOwner().getEmployee())){
                staffs.add(getCurrentUser().getStaff());
                staffs.addAll(assistantFacade.findAssistByUser(getCurrentUser())); 
                return staffs;
            }
            //заместитель исполнителя может указать себя
            if (assistantFacade.isAssistant(task.getOwner().getEmployee(), getCurrentUser())){
                if (getCurrentUser().getStaff() != null){
                    staffs.add(getCurrentUser().getStaff());
                }
            }
        }
        return staffs;
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
        if (task.getBeginDate() == null && task.getScheme() == null){
            errors.add("DateBeginNoSet");
        }
        if (task.getOwner() == null){
            errors.add("ExecutorNotSet");
        }
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        if (task.getBeginDate() == null){
            task.setBeginDate(new Date());
        }
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        task.setDeltaDeadLine(seconds);
        taskFacade.makeDatePlan(task);
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
    
    public boolean isShowBtnResults(){
        Task task = getEditedItem();
        State currentState = task.getState().getCurrentState();
        return DictStates.STATE_RUNNING == currentState.getId() 
                && taskFacade.checkUserInRole(task, DictRoles.ROLE_EXECUTOR_ID, getCurrentUser());
    }    
    
    /**
     * Определяет отображение на форме чекбокса "Добавить в лист согласования/исполнения"
     * @return 
     */
    public boolean isShowCheckBoxAdd(){       
        if (getEditedItem().getScheme() == null) return false;
        ProcessType processType = processTypesFacade.getProcTypeForOpt(getEditedItem().getScheme().getProcess().getOwner());
        return processType.isShowReports();
    }
    
    public String getCheckBoxAddCaption(){
        if (getEditedItem().getScheme() == null) return "";
        ProcessType processType = processTypesFacade.getProcTypeForOpt(getEditedItem().getScheme().getProcess().getOwner());
        if ("ApprovalSheet".equals(processType.getNameReports())) {
            return getLabelFromBundle("EnterInApprovalSheet");
        } else 
            if ("ExecutionSheet".equals(processType.getNameReports())){
                return getLabelFromBundle("EnterInExecutionSheet");
            }
        return "";
    }
        
    /* GETS & SETS */
    
    @Override
    public Task getEditedItem() {
        return super.getEditedItem();
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

    public List<Staff> getExecutors() {
        return executors;
    }
    public void setExecutors(List<Staff> executors) {
        this.executors = executors;
    }

    public boolean isCanShowTaskSettings(){
        return Objects.equals(DictStates.STATE_DRAFT, getEditedItem().getState().getCurrentState().getId());
    }
            
    /**
     * Расширенные ограничения к некоторым полям задачи
     * если задача в статусе RUNNING, то поле на форме нельзя редактировать
     * @return 
     */
    public boolean isTaskReadOnly(){
        if (isReadOnly()) return true;
        if (userFacade.isAdmin(getCurrentUser())) return Boolean.FALSE;        
        Task task = getEditedItem();
        boolean flag = DictStates.STATE_RUNNING == task.getState().getCurrentState().getId();
        return flag;
    }
    
    /**
     * Определяет доступ к полям на вкладке "Информация"
     * @return 
     */
    @Override
    public boolean isInfoReadOnly(){
        return isTaskReadOnly();
    }
    
    /**
     * Проверяет наличие права на выполнение задачи
     * @return 
     */
    @Override
    public boolean isHaveRightExec() {
        Task task = getEditedItem();
        boolean result = getFacade().isHaveRightEdit(task) && Objects.equals(getCurrentStaff(), task.getOwner());
        return result;
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

    public List<BaseDict> getForShow() {
        return forShow;
    }

    public String getResultName() {
        return resultName;
    }
    public void setResultName(String resultName) {
        this.resultName = resultName;
    }
     
}