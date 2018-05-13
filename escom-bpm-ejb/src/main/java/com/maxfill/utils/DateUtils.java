package com.maxfill.utils;

import com.google.common.base.Preconditions;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DateUtils {
    public final static Logger LOGGER = Logger.getLogger(DateUtils.class.getName());
    public final static Integer MINUTE_TYPE = 0;
    public final static Integer HOUR_TYPE = 1;
    public final static Integer DAILY_REPEAT = 0;
    public final static Integer WEEKLY_REPEAT = 1;
    public final static Integer MONTHLY_REPEAT = 2;
    
    static final int MINUTES_PER_HOUR = 60;
    static final int HOURS_PER_DAY = 24;
    static final int SECONDS_PER_MINUTE = 60;
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    
    private DateUtils() { }

    /* Преобразование строки yyyy-MM-dd в дату */
    public static Date convertStrToDate(String dateStr){ 
        Date rezult = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            rezult = df.parse(dateStr);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return rezult;
    }

    /* Конвертация строки в дату */
    public static Date convertStrToDate(String dateStr, Locale locale){        
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return date;
    }
    
    /* Обнуление времени в дате */
    public static Date clearDate(Date date) {
        Preconditions.checkNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /* Создание локальной даты  */
    public static LocalDate createLocalDate(){
        return toLocalDate(new Date());
    }
    
    /* Преобразование даты в строку */ 
    public static String dateToString(Date sourceDate, Integer formatDate, Integer formatTime, Locale locale){
        if (sourceDate == null || formatDate == null || locale == null) return null;       
        String rezult;
        if (formatTime != null){
            DateFormat df = DateFormat.getDateTimeInstance(formatDate, formatTime, locale);                
            rezult = df.format(sourceDate);
        } else {
            DateFormat df = DateFormat.getDateInstance(formatDate, locale);
            rezult = df.format(sourceDate);
        }
        return rezult;  
    } 
    
    /* Преобразование даты в локальную дату */
    public static LocalDate toLocalDate(Date date) {
        Preconditions.checkNotNull(date);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public static LocalDateTime toLocalDateTime(Date date){  
        Preconditions.checkNotNull(date);
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return ldt;
        //Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());         
    }

    /* Преобразование строки в формате unix-time в дату  */
    public static Date strLongToDate(String longStr){        
        Long unixTime = Long.valueOf(longStr);        
        return new Date(unixTime);
    }
    
    /* Преобразование даты в unix формат */ 
    public static long dateToLongConvert(Date dateDate) throws ParseException {
        long unixtime;
        DateFormat dfm = new SimpleDateFormat("yyyyMMddHHmm"); 
        String time = dfm.format(dateDate);
        dfm.setTimeZone(TimeZone.getTimeZone("GMT+4"));//Specify your timezone 
        unixtime = dfm.parse(time).getTime();  
        unixtime = unixtime/1000;
        return unixtime;
    }
    
    /* Добавление к дате указанного числа дней */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); 
        return cal.getTime();
    }
    
    public static Date today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar.getTime();
    }
        
    /* Добавление к дате указанного числа часов */
    public static Date addHour(Date date, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hour); 
        return cal.getTime();
    }
    
    /* Добавление к дате указанного числа минут */
    public static Date addMinute(Date date, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minute); 
        return cal.getTime();
    }
    
    /* Добавление к дате указанного числа месяцев  */
    public static Date addMounth(Date date, int mounth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, mounth); 
        return cal.getTime();
    }

    /* Добавление к дате указанного числа секунд */
    public static Date addSeconds(Date date, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds); 
        return cal.getTime();
    }
    
    /**
     * Возвращает разницу между двумя датами в днях
     * @param dateStart
     * @param dateEnd
     * @return
     */
    public static String differenceDays(Instant dateStart, Instant dateEnd) {
        Duration duration = Duration.between(dateStart, dateEnd);
        return String.valueOf(duration.toDays());
    }

    /* Возвращает разницу между двумя датами в виде времени в часах:минутах:секундах */
    public static String differenceTime(Date dateStart, Date dateEnd) {        
        LocalDateTime ldStart = toLocalDateTime(dateStart);
        LocalDateTime ldEnd = toLocalDateTime(dateEnd);
        Duration duration = Duration.between(ldStart, ldEnd);
        long seconds = duration.getSeconds();
        long hours = seconds / SECONDS_PER_HOUR;
        long minutes = ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        long secs = (seconds % SECONDS_PER_MINUTE);
        StringBuilder diff = new StringBuilder();
        diff.append(hours).append(":").append(minutes).append(":").append(secs);
        return diff.toString();
    }
    
    public static GregorianCalendar dateToGregorianCalendar(Date date){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date); 
        return calendar;
    }
}
