package com.maxfill.escom.beans.system.worktime;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.eventfeed.EventFeedFacade;
import com.maxfill.services.worktime.DayType;
import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.services.worktime.WorkTimeService;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;

/**
 * Контролер карточки "Добавление события"
 */
@ViewScoped
@Named
public class AddEventWTBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 3482773173068260166L;

    @EJB
    private EventFeedFacade eventFeedFacade;    
    @EJB
    private WorkTimeService workTimeService;
    
    private Date beginDate;
    private Date endDate;
    
    private boolean publicEventFeed = true;           
    private DayType selectedDayType = new DayType(DateUtils.HOLLYDAY, MsgUtils.getBandleLabel("Hollyday"), "holyday");
    private List<DayType> dayTypes;
    private Staff staff;
    
    @Override 
    public void doBeforeOpenCard(Map<String, String> params){        
        if (sourceBean != null){                
            initDayTypes();
            beginDate = DateUtils.clearDate(new Date());
            endDate = DateUtils.addDays(beginDate, 1);
            staff = ((CalendarBean)sourceBean).getSelectedStaff();
        }
    }

    private void initDayTypes(){
        dayTypes = new ArrayList<>();                   
        dayTypes.add(new DayType(DateUtils.WEEKEND, MsgUtils.getBandleLabel("Weekend"), "weekend"));
        dayTypes.add(new DayType(DateUtils.HOLLYDAY, MsgUtils.getBandleLabel("Hollyday"), "holyday"));
        dayTypes.add(new DayType(DateUtils.HOSPITALDAY, MsgUtils.getBandleLabel("HospitalDay"), "hospital"));
        dayTypes.add(new DayType(DateUtils.MISSIONDAY, MsgUtils.getBandleLabel("MissionDay"), "mission"));               
    }
    
    @Override
    public String onCloseCard(Object param){
        if (SysParams.EXIT_NOTHING_TODO.equals((String)param)){
            return super.onCloseCard(SysParams.EXIT_NOTHING_TODO);
        }
        publicEvent();
        changeCalendar();        
        return super.onCloseCard(SysParams.EXIT_NEED_UPDATE);
    }
    
    private void changeCalendar(){
        //цикл по всем дням периода        
        while(beginDate.before(endDate) || beginDate.equals(endDate)){
            WorkTimeCalendar wtc = workTimeService.getWorkTimeDate(beginDate, staff, null);
            wtc.setStandart(false);
            wtc.setDayType(selectedDayType.getId());
            workTimeService.update(wtc);
            beginDate = DateUtils.addDays(beginDate, 1);
        }
    }
    
    private void publicEvent(){
        if (isPublicEventFeed()){
            StringBuilder sb = new StringBuilder();          
            sb.append(DateUtils.dateToString(beginDate, DateFormat.SHORT, null, getLocale())).append(" - ");
            sb.append(DateUtils.dateToString(endDate, DateFormat.SHORT, null, getLocale())); 
            sb.append(" ").append(selectedDayType.getName()).append(" ");             
            sb.append(staff.getStaffFIO());
            eventFeedFacade.publicEventToFeed(sb.toString(), selectedDayType.getIconName(), getCurrentUser());
        }        
    }
        
    /* GETS & SETS */    
    
    public boolean getReadOnly(){
        return false;
    }
        
    @Override
    public String getFormName() {
        return DictFrmName.FRM_ADD_WTEVENT;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("AddEvent"); 
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

    public Date getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
         
    public List<DayType> getDayTypes() {
        return dayTypes;
    }  
        
}
