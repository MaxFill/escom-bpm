package com.maxfill.model;

import com.maxfill.dictionary.DictStates;
import com.maxfill.model.core.states.BaseStateItem;
import java.util.Date;

/**
 *
 * @author maksim
 */
public interface WithDatesPlans {
    
    public Date getBeginDate();
    public void setBeginDate(Date beginDate);

    public Date getPlanExecDate();
    public void setPlanExecDate(Date planExecDate);

    public Date getFactExecDate();
    public void setFactExecDate(Date factExecDate); 
    
    public BaseStateItem getState();
    
    default boolean isRunning(){
        return DictStates.STATE_RUNNING == getState().getCurrentState().getId();
    }
    
    default boolean isCompleted(){
        return DictStates.STATE_COMPLETED == getState().getCurrentState().getId();
    } 
    
    default boolean isCanceled(){
        return DictStates.STATE_CANCELLED == getState().getCurrentState().getId();
    }
}
