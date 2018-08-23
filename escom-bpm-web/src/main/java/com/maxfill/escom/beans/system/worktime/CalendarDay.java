package com.maxfill.escom.beans.system.worktime;

import com.maxfill.services.worktime.WorkTimeCalendar;
import java.util.Date;
import org.primefaces.model.DefaultScheduleEvent;

/**
 * Сущность "Запись календаря рабочего времени"
 */
public class CalendarDay extends DefaultScheduleEvent{    
    private static final long serialVersionUID = -7090644915491990070L;
    private WorkTimeCalendar wtc;

    public CalendarDay() {
    }

    public CalendarDay(WorkTimeCalendar wtc) {        
        super("", wtc.getStart(), wtc.getEnd(), wtc.getStyle());        
        this.wtc = wtc;
    }

    public CalendarDay(String title, Date start, Date end) {
        super(title, start, end);
    }   

    @Override
    public String getTitle() {
        return super.getTitle(); 
    }
    
    @Override
    public Date getEndDate() {
        if (wtc != null){
            return wtc.getEnd();
        } else 
        return super.getEndDate(); 
    }

    @Override
    public Date getStartDate() {
        if (wtc != null){
            return wtc.getStart();
        } else 
        return super.getStartDate(); 
    }    
    
    public WorkTimeCalendar getWtc() {
        return wtc;
    }
    public void setWtc(WorkTimeCalendar wtc) {
        this.wtc = wtc;
    }
}
