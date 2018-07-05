package com.maxfill.escom.beans.scheduler;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.task.Task;
import com.maxfill.utils.DateUtils;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 * Контролер формы "Планировщик"
 */
@Named
@ViewScoped
public class SchedulerBean extends BaseViewBean {
    private static final long serialVersionUID = -2515586022679502172L;

    @EJB
    private TaskFacade taskFacade;
    @Inject
    private TaskBean taskBean;    
            
    private final ScheduleModel eventModel = new DefaultScheduleModel();
    private SchedulerTask schedulerTask = new SchedulerTask();
    
    @Override
    protected void initBean(){    
        List<Task> tasks = taskFacade.findTaskByStaff(getCurrentStaff());
        tasks.stream()
                .filter(task-> task.getBeginDate() != null && task.getPlanExecDate() != null)
                .forEach(task-> eventModel.addEvent(new SchedulerTask(task)));
    };    
  
    /**
     * Обработка события создания нового поручения
     * @param beanId
     */
    public void onCreateTask(String beanId){
        Task task = taskFacade.createTask("", getCurrentStaff(), getCurrentUser(), schedulerTask.getEndDate());
        task.setBeginDate(new Date());
        schedulerTask.setTask(task);
        onOpenTask(beanId);
    }
    
    public void onOpenTask(String beanId){        
        taskBean.prepEditChildItem(getTask(), getParamsMap());
    }
    
    /**
     * Обработка события после закрытия карточки задания
     * @param event
     */
    public void onTaskDlgClose(SelectEvent event){
        if (event.getObject() == null) return;        
        String action = (String) event.getObject();
        Task task = getTask();
        if ("delete".equals(action)){
            if (task.getId() != null){
                taskFacade.remove(task);
                eventModel.deleteEvent(schedulerTask);
            }
        } else {
            schedulerTask.setStyleClass(task.getStyle());
            if (task.getId() == null){                
                taskFacade.create(getTask());
                eventModel.addEvent(schedulerTask);
            } else {
                taskFacade.edit(getTask());
            }
        }
        modelRefresh();
    }
    
    /* ОБРАБОТКА СОБЫТИЙ ПЛАНИРОВЩИКА */
    
    public void onEventSelect(SelectEvent selectEvent) {
        schedulerTask = (SchedulerTask) selectEvent.getObject();
        PrimeFaces.current().executeScript("document.getElementById('centerFRM:btnOpenTask').click();");
    }
     
    public void onDateSelect(SelectEvent selectEvent) {
        Integer offset  = ZonedDateTime.now().getOffset().getTotalSeconds();
        Date startDateTime = DateUtils.addSeconds((Date) selectEvent.getObject(), offset);
        Date endDateTime = DateUtils.addHour(startDateTime, 1); //ToDO нужно вычислять с учётом рабочего времени!
        schedulerTask = new SchedulerTask("", startDateTime, endDateTime);        
        PrimeFaces.current().executeScript("document.getElementById('centerFRM:btnCreateTask').click();");
    }
     
    public void onEventMove(ScheduleEntryMoveEvent event) {        
        schedulerTask = (SchedulerTask) event.getScheduleEvent();
        Integer dayDelta = event.getDayDelta();
        Integer minuteDelta = event.getMinuteDelta();
        Task task = getTask();
        Date newDate = DateUtils.addDays(task.getBeginDate(), dayDelta);
        newDate = DateUtils.addMinute(newDate, minuteDelta);
        task.setBeginDate(newDate);
        taskFacade.edit(task);
    }
     
    public void onEventResize(ScheduleEntryResizeEvent event) {        
        schedulerTask = (SchedulerTask) event.getScheduleEvent();
        Integer dayDelta = event.getDayDelta();
        Integer minuteDelta = event.getMinuteDelta();
        Task task = getTask();
        Date newDate = DateUtils.addDays(task.getPlanExecDate(), dayDelta);
        newDate = DateUtils.addMinute(newDate, minuteDelta);
        task.setPlanExecDate(newDate);
        taskFacade.edit(task);
    }
    
    public void modelRefresh(){
        PrimeFaces.current().ajax().update("centerFRM");
    }
    
    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_SCHEDULER;
    }
    
    /* GETS & SETS */

    public ScheduleModel getEventModel() {
        return eventModel;
    }
    
    private Task getTask(){
        return getSourceItem();
    }
    
    @Override
    public Task getSourceItem() {
        return schedulerTask.getTask();
    }
}