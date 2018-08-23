package com.maxfill.escom.beans.system.worktime;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.utils.DateUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.omnifaces.cdi.ViewScoped;

/**
 * Контролер карточки "Рабочее время"
 */
@ViewScoped
@Named
public class WorkTimeCardBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 3482773173068260166L;

    private WorkTimeCalendar editedItem = new WorkTimeCalendar();
    private WorkTimeCalendar sourceItem;
    private boolean inheritsWorkTime = true;
    
    private Date beginTime;
    private Date endTime;
    
    @Override 
    public void doBeforeOpenCard(Map<String, String> params){
        if (editedItem == null){                        
            if (sourceBean != null){                
                sourceItem = ((CalendarBean)sourceBean).getSelected();
                Integer time = sourceItem.getBeginTime(); //время в секундах
                beginTime = new Date(time * 1000);                
                beginTime = DateUtils.convertHourFromUTCToLocalTimeZone(beginTime);                 
            }
            if (sourceItem != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceItem);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public String onCloseCard(Object param){
        try {
            beginTime = DateUtils.convertHourToUTCTimeZone(beginTime);
            Long time = beginTime.getTime();
            editedItem.setBeginTime(time.intValue());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    /* GETS & SETS */    
    
    public boolean getReadOnly(){
        return false;
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_WORKTIME;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("WorkTime"); 
    }

    public WorkTimeCalendar getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(WorkTimeCalendar editedItem) {
        this.editedItem = editedItem;
    }

    public boolean isInheritsWorkTime() {
        return inheritsWorkTime;
    }
    public void setInheritsWorkTime(boolean inheritsWorkTime) {
        this.inheritsWorkTime = inheritsWorkTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        endTime = DateUtils.addHour(beginTime, editedItem.getWorkTime());
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
       
    
}
