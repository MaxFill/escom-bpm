package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.process.schemes.elements.TimerElem;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Контролер формы "Свойства условия процесса"
 */
@Named
@ViewScoped
public class TimerCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -5186880746110498838L;   
    
    private ProcTimer procTimer = null;
    private TimerElem sourceItem = null;
    private TimerElem editedItem = new TimerElem();    
    private List<String> sourceDays;
    
    private String[] daysOfWeek;
    private String[] reminderDays;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){                        
            if (sourceBean != null){
                sourceItem = (TimerElem)((DiagramBean)sourceBean).getBaseElement();                             
                if (sourceItem != null){
                    procTimer = sourceItem.getProcTimer();
                    /*
                    if (sourceItem.getTimerId() != null){
                        procTimer = procTimerFacade.find(sourceItem.getTimerId());
                    } else {
                        procTimer = sourceItem.getProcTimer();
                    }
                    */
                    try {
                        BeanUtils.copyProperties(editedItem, sourceItem);
                        restoreFields(procTimer);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
        
    private void checkTimer(Set<String> errors){
        if ("on_date".equals(procTimer.getStartType()) && procTimer.getStartDate() == null){
            errors.add("DateStartNoSet");
        }
        if (!"on_date".equals(procTimer.getStartType())){
            procTimer.setStartDate(null);
        }
    }
        
    @Override
    public String onCloseCard(Object param){
        Set<String> errors = new HashSet<>();
        checkTimer(errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return "";
        }
        saveFields(procTimer);
        try {
            editedItem.setTimerId(procTimer.getId()); 
            editedItem.setRepeatType(procTimer.getRepeatType());
            editedItem.setStartType(procTimer.getStartType());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    private void restoreFields(ProcTimer timer){
        if (StringUtils.isNotEmpty(timer.getDaysWeekRepeat())){
            reminderDays = timer.getDaysWeekRepeat().split(",");
        }
    }
    
    private void saveFields(ProcTimer timer){
         if (reminderDays != null){
            timer.setDaysWeekRepeat(String.join(",", reminderDays));
        }
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
