package com.maxfill.escom.beans.system.worktime;

import com.maxfill.escom.beans.scheduler.*;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.services.worktime.WorkTimeFacade;
import com.maxfill.utils.DateUtils;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 * Контролер формы "Календарь рабочего времени"
 */
@Named
@ViewScoped
public class CalendarBean extends BaseViewBean {
    private static final long serialVersionUID = -2515586022679502172L;

    @EJB
    private WorkTimeFacade workTimeFacade;   
    
    private WorkTimeCalendar selected;
    
    private CalendarDay calendarDay;
    
    private final ScheduleModel eventModel = new DefaultScheduleModel();
    private final TimeZone utc=TimeZone.getTimeZone("UTC");
    private final String tzname = utc.getID();
    
    @Override
    protected void initBean(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        Date date = calendar.getTime();        
        String dayType = "holliday";
        selected = new WorkTimeCalendar(date, 8 * 3600, 8, dayType);
        calendarDay = new CalendarDay(selected);
        eventModel.addEvent(calendarDay);
    };    
  
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("CalendarWorkTime");
    }
    
    /**
     * Обработка события создания нового дня
     */
    public void onCreateDay(){
        onOpenDay();
    }
    
    public void onOpenDay(){        
        sessionBean.openDialogFrm(DictFrmName.FRM_WORKTIME, getParamsMap());
    }
    
    /**
     * Обработка события после закрытия карточки настроек дня
     * @param event
     */
    public void onDlgClose(SelectEvent event){
        if (event.getObject() == null) return;        
        String action = (String) event.getObject();

        switch (action){
            case SysParams.EXIT_NEED_UPDATE:{
                
                modelRefresh();
                break;
            }
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
        }
    }
    
    /* ОБРАБОТКА СОБЫТИЙ ПЛАНИРОВЩИКА */
    
    public void onEventSelect(SelectEvent selectEvent) {
        calendarDay = (CalendarDay) selectEvent.getObject();
        selected = calendarDay.getWtc();
        PrimeFaces.current().executeScript("document.getElementById('mainFRM:btnOpen').click();");
    }
     
    public void onDateSelect(SelectEvent selectEvent) {
        //Integer offset  = ZonedDateTime.now().getOffset().getTotalSeconds();
        //Date startDateTime = DateUtils.addSeconds((Date) selectEvent.getObject(), offset);        
        Date date = (Date) selectEvent.getObject();
        selected = new WorkTimeCalendar(date, 8 * 3600, 8, "holliday");
        calendarDay = new CalendarDay(selected);        
        PrimeFaces.current().executeScript("document.getElementById('mainFRM:btnCreate').click();");
    }
    
    public void modelRefresh(){
        PrimeFaces.current().ajax().update("mainFRM");
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_SCHEDULER;
    }
    
    /* GETS & SETS */

    public String getClientTimeZone(){
        return tzname;
    }
    
    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public WorkTimeCalendar getSelected() {
        return selected;
    }
    public void setSelected(WorkTimeCalendar selected) {
        this.selected = selected;
    }
       
}