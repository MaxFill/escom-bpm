package com.maxfill.escom.beans.system.worktime;

import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import org.primefaces.model.DefaultScheduleEvent;

/**
 * Сущность "Запись календаря рабочего времени"
 */
public class CalendarDay extends DefaultScheduleEvent{    
    private static final long serialVersionUID = -7090644915491990070L;
    private WorkTimeCalendar wtc;
    private ZoneId zoneId;
    private Locale locale;    
    
    public CalendarDay() {
    }

    public CalendarDay(WorkTimeCalendar wtc, ZoneId zoneId, Locale locale) {        
        super("", wtc.getStart(), wtc.getEnd(), wtc.getStyle());        
        this.wtc = wtc;
        this.zoneId = zoneId;
        this.locale = locale;
    }

    @Override
    public String getTitle() {
        if (wtc == null) return super.getTitle(); 
        if (wtc.isWorkDay()){
            StringBuilder sb = new StringBuilder();
            sb.append(wtc.getWorkTime()).append(ItemUtils.getBandleLabel("HourShort", locale));
            return sb.toString();
        } else {
            return ItemUtils.getBandleLabel(wtc.getStyle(), locale);
        }
    }   

    @Override
    public Date getStartDate() {
        if (wtc == null) return super.getStartDate(); 
        Date dt = wtc.getStart();        
        Integer offset = ZonedDateTime.ofInstant(dt.toInstant(), zoneId).getOffset().getTotalSeconds();
        dt = DateUtils.addSeconds(dt, offset);
        return dt;
    }
    
    @Override
    public Date getEndDate() {
        if (wtc == null) return super.getEndDate();
        Date dt = wtc.getEnd();
        Integer offset = ZonedDateTime.ofInstant(dt.toInstant(), zoneId).getOffset().getTotalSeconds();
        dt = DateUtils.addSeconds(dt, offset);
        return dt;
    }
    
    public WorkTimeCalendar getWtc() {
        return wtc;
    }
    public void setWtc(WorkTimeCalendar wtc) {
        this.wtc = wtc;
    }
}