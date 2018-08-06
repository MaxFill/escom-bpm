package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.process.schemes.elements.TimerElem;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.process.timers.ProcTimerFacade;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Свойства условия процесса"
 */
@Named
@ViewScoped
public class TimerCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -5186880746110498838L;
    
    @EJB
    private ProcTimerFacade procTimerFacade;
    
    private ProcTimer procTimer = null;
    private TimerElem sourceItem = null;
    private TimerElem editedItem = new TimerElem();    
    private String[] daysOfWeek;
    private int reminderDeltaDay = 0;
    private int reminderDeltaHour = 0;
    private int reminderDeltaMinute = 0;
    private Date reminderTime;
    private String[] reminderDays;
    private List<String> sourceDays;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){                        
            if (sourceBean != null){
                sourceItem = (TimerElem)((ProcessCardBean)sourceBean).getBaseElement();                 
            
                if (sourceItem != null){
                    if (sourceItem.getTimerId() != null){
                        procTimer = procTimerFacade.find(sourceItem.getTimerId());
                    } else {
                        procTimer = sourceItem.getProcTimer();
                    }
                    try {
                        BeanUtils.copyProperties(editedItem, sourceItem);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    @Override
    public String onCloseCard(Object param){
        try {
            if (procTimer != null){
                editedItem.setTimerId(procTimer.getId());
                editedItem.setCaption(procTimer.getName());
            }
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_TIMER;
    }     

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Timer");
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
    
    /* GETS & SETS */

    public Date getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public String[] getDaysOfWeek() {
        return daysOfWeek;
    }
    public void setDaysOfWeek(String[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    public ProcTimer getProcTimer() {
        return procTimer;
    }
    public void setProcTimer(ProcTimer procTimer) {
        this.procTimer = procTimer;
    }    

    public TimerElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(TimerElem editedItem) {
        this.editedItem = editedItem;
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
}
