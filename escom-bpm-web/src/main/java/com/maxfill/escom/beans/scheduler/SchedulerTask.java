package com.maxfill.escom.beans.scheduler;

import com.maxfill.model.process.schemes.task.Task;
import java.util.Date;
import org.primefaces.model.DefaultScheduleEvent;

/**
 * Сущность "Запись планировщика"
 */
public class SchedulerTask extends DefaultScheduleEvent{    
    private static final long serialVersionUID = -7090644915491990070L;
    private Task task;

    public SchedulerTask() {
    }

    public SchedulerTask(Task task) {        
        super(task.getName(), task.getBeginDate(), task.getPlanExecDate());
        this.task = task;
    }

    public SchedulerTask(String title, Date start, Date end) {
        super(title, start, end);
    }   

    @Override
    public String getTitle() {
        if (getTask() != null){
            return getTask().getName();
        } else 
            return super.getTitle(); 
    }
    
    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public Date getEndDate() {
        if (getTask() != null){
            return getTask().getPlanExecDate();
        } else 
        return super.getEndDate(); 
    }

    @Override
    public Date getStartDate() {
        if (getTask() != null){
            return getTask().getBeginDate();
        } else 
        return super.getStartDate(); 
    }    
    
}
