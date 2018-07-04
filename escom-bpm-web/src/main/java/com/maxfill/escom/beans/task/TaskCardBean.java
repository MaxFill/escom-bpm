package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.ResultFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
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
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.primefaces.event.SelectEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Inject;
import liquibase.util.StringUtils;
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
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private DocBean docBean;

    private boolean readOnly;

    private List<Result> taskResults;
    private DualListModel<Result> results;
    private int deadLineDeltaDay = 0;
    private int deadLineDeltaHour = 0;
    private int reminderDeltaDay = 0;
    private int reminderDeltaHour = 0;
    private int reminderDeltaMinute = 0;
    private String[] reminderDays;
    private List<String> sourceDays;
    
    private ProcReport currentReport;
    
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
        //проверка для срока исполнения
        switch (task.getDeadLineType()){
            case "delta":{
                if (task.getDeltaDeadLine() == 0){
                   errors.add(MsgUtils.getMessageLabel("DeadlineIncorrect"));
                }       
                break;
            }
            case "data":{
                if (task.getPlanExecDate() == null ){
                    errors.add(MsgUtils.getMessageLabel("DeadlineIncorrect"));
                } else 
                    if (task.getPlanExecDate().before(new Date())){
                        errors.add(MsgUtils.getMessageLabel("DeadlineSpecifiedInPastTime"));
                    }
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
                        if (reminderDays == null){
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
    
    /**
     * Обработка события выполнения задачи
     * @param result
     * @return 
     */
    public String onExecute(Result result){        
        Set<String> errors = new HashSet<>();
        checkItemBeforeSave(getEditedItem(), errors); 
        checkTaskBeforeExecute(getEditedItem(), result, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return "";
        } 
        
        Task task = getEditedItem();
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
        if (process != null){ 
            Doc doc = process.getDoc();
            if (doc != null){
               docBean.prepEditItem(doc, getParamsMap());
            } else {
               MsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            }
        } else {
            MsgUtils.errorMsg("LinkProcessIncorrect");
        }
    }
    
    /**
     * Обработка события просмотра документа
     */
    public void onViewDocument(){
        Process process = getProcess();
        if (process != null){ 
            Doc doc = process.getDoc();
            if (doc != null){
               docBean.onViewMainAttache(doc);
            } else {
               MsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            }
        } else {
            MsgUtils.errorMsg("LinkProcessIncorrect");
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
        if (getEditedItem().getScheme() == null){
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
            reminderDays = task.getReminderDays().split(",");
        }
    }
    
    private void saveDateFields(Task task){
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        task.setDeltaDeadLine(seconds);
        if (reminderDays != null){
            task.setReminderDays(String.join(",", reminderDays));
        }
        if ("singl".equals(task.getReminderType())){
            int sec = reminderDeltaDay * 86400;
            sec = sec + reminderDeltaHour * 3600;
            sec = sec + reminderDeltaMinute * 60;
            task.setDeltaReminder(sec);
        }
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

    public String[] getReminderDays() {
        return reminderDays;
    }
    public void setReminderDays(String[] reminderDays) {
        this.reminderDays = reminderDays;
    }

    public List<String> getSourceDays() {
        if (sourceDays == null){
            sourceDays = new ArrayList<>();
            for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.values())){
                sourceDays.add(String.valueOf(dayOfWeek.getValue()));
            }
        }
        return sourceDays;
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
            List<Result> allResults = resultFacade.findAll();
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
}