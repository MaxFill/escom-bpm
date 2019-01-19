package com.maxfill.escom.beans.system.worktime;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.core.eventfeed.EventFeedFacade;
import com.maxfill.services.worktime.DayType;
import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.utils.DateUtils;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
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

    @EJB
    private EventFeedFacade eventFeedFacade;
    
    private WorkTimeCalendar editedItem;
    private WorkTimeCalendar sourceItem;
    
    private Date beginTime;
    private Date endTime;
    
    private boolean publicEventFeed = false;           
    private DayType selectedDayType = null;
    private List<DayType> dayTypes;
    
    @Override 
    public void doBeforeOpenCard(Map<String, String> params){
        if (editedItem == null){                 
            if (sourceBean != null){                
                sourceItem = ((CalendarBean)sourceBean).getSelected();
                initDayTypes(sourceItem);
                if (sourceItem.getDayType() != null){
                    selectedDayType = getDayTypes().get(sourceItem.getDayType());
                } 
                Integer time = sourceItem.getBeginTime(); //время в секундах
                beginTime = new Date(time);
                beginTime = DateUtils.convertHourFromUTCToLocalTimeZone(beginTime);                 
            }
            if (sourceItem != null){
                try {
                    editedItem = new WorkTimeCalendar();
                    BeanUtils.copyProperties(editedItem, sourceItem);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }        
    }

    private void initDayTypes(WorkTimeCalendar wtc){
        dayTypes = new ArrayList<>();            
        dayTypes.add(new DayType(null, MsgUtils.getBandleLabel("EmptySelData"), ""));
        dayTypes.add(new DayType(DateUtils.WORKDAY, MsgUtils.getBandleLabel("Workday"), "portfolio"));
        dayTypes.add(new DayType(DateUtils.WEEKEND, MsgUtils.getBandleLabel("Weekend"), "weekend"));
        if (wtc.getStaff() != null){
            dayTypes.add(new DayType(DateUtils.HOLLYDAY, MsgUtils.getBandleLabel("Hollyday"), "holyday"));
            dayTypes.add(new DayType(DateUtils.HOSPITALDAY, MsgUtils.getBandleLabel("HospitalDay"), "hospital"));
            dayTypes.add(new DayType(DateUtils.MISSIONDAY, MsgUtils.getBandleLabel("MissionDay"), "mission"));       
        }
    }
    
    @Override
    public String onCloseCard(Object param){
        if (SysParams.EXIT_NOTHING_TODO.equals((String)param)){
            return super.onCloseCard(SysParams.EXIT_NOTHING_TODO);
        }
        try {
            if (selectedDayType != null){
                editedItem.setDayType(selectedDayType.getId());                
            } else {
                editedItem.setDayType(null);
            }
            
            if (!editedItem.getStandart() && !editedItem.isWorkDay()){
                editedItem.setWorkTimeHour(0);
                editedItem.setWorkTimeMinute(0);
                editedItem.setBeginTime(0);
            } else {
                beginTime = DateUtils.convertHourToUTCTimeZone(beginTime);
                Long time = beginTime.getTime();
                editedItem.setBeginTime(time.intValue());    
            }
            publicEvent();
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(SysParams.EXIT_NEED_UPDATE);
    }
    
    private void publicEvent(){
        if (isPublicEventFeed()){
            StringBuilder sb = new StringBuilder();            

            Date dateDay = DateUtils.convertStrToDate(getEditedItem().getDateCalendar(), "MM/dd/yy");
            Long timeStart = beginTime.getTime();
            Long timeEnd = endTime.getTime();
            Date start = DateUtils.addMilliseconds(dateDay, timeStart.intValue());
            Date end = DateUtils.addMilliseconds(dateDay, timeEnd.intValue());
            
            if (selectedDayType.getId() == DateUtils.WORKDAY){
                sb.append(MsgUtils.getBandleLabel("With")).append(" ")
                            .append(DateUtils.dateToString(start, DateFormat.SHORT, DateFormat.SHORT, getLocale())).append(" ");
                sb.append(MsgUtils.getBandleLabel("At")).append(" ")
                            .append(DateUtils.dateToString(end, DateFormat.SHORT, DateFormat.SHORT, getLocale())); 
            } else {
                sb.append(DateUtils.dateToString(start, DateFormat.SHORT, null, getLocale()));
            }
            
            sb.append(" ").append(selectedDayType.getName()).append(" ");
            
            if (getEditedItem().getStaff() != null){                                
                sb.append(getEditedItem().getStaff().getStaffFIO());
            }
            eventFeedFacade.publicEventToFeed(sb.toString(), selectedDayType.getIconName(), getCurrentUser());
        }        
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

    public boolean isPublicEventFeed() {
        return publicEventFeed;
    }
    public void setPublicEventFeed(boolean publicEventFeed) {
        this.publicEventFeed = publicEventFeed;
    }

    public DayType getSelectedDayType() {
        return selectedDayType;
    }
    public void setSelectedDayType(DayType selectedDayType) {
        this.selectedDayType = selectedDayType;
    }
        
    public Date getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        endTime = DateUtils.addHour(beginTime, editedItem.getWorkTimeHour());
        endTime = DateUtils.addMinute(endTime, editedItem.getWorkTimeMinute());
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }       
      
    public List<DayType> getDayTypes() {
        return dayTypes;
    }  
        
}
