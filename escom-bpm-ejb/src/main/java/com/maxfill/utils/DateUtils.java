package com.maxfill.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public final class DateUtils {
    public final static Logger LOGGER = Logger.getLogger(DateUtils.class.getName());
    public final static Integer MINUTE_TYPE = 0;
    public final static Integer HOUR_TYPE = 1;
    public final static Integer DAILY_REPEAT = 0;
    public final static Integer WEEKLY_REPEAT = 1;
    public final static Integer MONTHLY_REPEAT = 2;
    
    public final static int WORKDAY = 1;
    public final static int WEEKEND = 2;
    public final static int HOLLYDAY = 3;
    public final static int HOSPITALDAY = 4;
    public final static int MISSIONDAY = 5;  //командировка
            
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now(ZoneId.systemDefault()).getOffset();

    
    private DateUtils() { }

    public static Date periodStartDate(String period, Date dateStart){         
        switch (period) {
            case "today":{
                dateStart = clearDate(new Date());
                break;
            }
            case "curWeek":{
                dateStart = firstDayWeek(LocalDate.now());
                break;
            }
            case "сurMonth":{
                dateStart = firstDayMounth(LocalDate.now());
                break;
            }
            case "curQuarter":{
                dateStart = firstDayQuarter(LocalDate.now());
                break;
            }
            case "curYear":{
                dateStart = firstDayYear(LocalDate.now());
                break;
            }
        }
        return dateStart;
    }
    
    public static Date periodEndDate(String period, Date dateEnd){
        switch (period) {
            case "today":{
                dateEnd = endDate(new Date());
                break;
            }
            case "curWeek":{
                dateEnd = lastDayWeek(LocalDate.now());
                break;
            }
            case "сurMonth":{
                dateEnd = lastDayMounth(LocalDate.now());
                break;
            }
            case "curQuarter":{
                dateEnd = lastDayQuarter(LocalDate.now());
                break;
            }
            case "curYear":{
                dateEnd = lastDayYear(LocalDate.now());
                break;
            }
        }
        return dateEnd;
    }
    
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
    public static Date convertStrToDate(String dateStr, String formatStr){
        DateFormat format = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return date;
    }
    
    /* Конвертация строки в дату */
    public static Date convertStrToDate(String dateStr, String formatStr, Locale locale){           
        DateFormat format = new SimpleDateFormat(formatStr, locale);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Возвращает дату, как конец дня
     * @param date
     * @return 
     */
    public static Date endDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 99);
        return calendar.getTime();
    }
    
    public static Date firstDayWeek(LocalDate ld ){
        ld = ld.with(ChronoField.DAY_OF_WEEK, 1);        
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }       
    
    public static Date firstDayMounth(LocalDate ld ){
        return Date.from(ld.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    public static Date firstDayYear(LocalDate ld){
        LocalDate firstDay = ld.with(firstDayOfYear());
        return Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
        
    public static Date firstDayQuarter(LocalDate ld){  
        LocalDate firstDay = ld.with(ld.getMonth().firstMonthOfQuarter()).with(TemporalAdjusters.firstDayOfMonth());
        return Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    public static Date lastDayWeek(LocalDate ld){
        ld = ld.with(ChronoField.DAY_OF_WEEK, 7);            
        return Date.from(ld.atTime(23, 59, 59).toInstant(ZONE_OFFSET));
    }
    
    public static Date lastDayMounth(LocalDate ld){        
        return Date.from(ld.withDayOfMonth(ld.lengthOfMonth()).atTime(23, 59, 59).toInstant(ZONE_OFFSET));
    }    
    
    public static Date lastDayYear(LocalDate ld){   
        LocalDate lastDay = ld.with(lastDayOfYear());
        return Date.from(lastDay.atTime(23, 59, 59).toInstant(ZONE_OFFSET));
    }   
    
    public static Date lastDayQuarter(LocalDate ld){   
        LocalDate firstDay = ld.with(ld.getMonth().firstMonthOfQuarter()).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = firstDay.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());        
        return Date.from(lastDay.atTime(23, 59, 59).toInstant(ZONE_OFFSET));
    }
    
    /* Создание локальной даты  */
    public static LocalDate createLocalDate(){
        return LocalDate.now();
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
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    public static LocalDateTime toLocalDateTime(Date date){  
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
    
    /** 
     * Формирует текущую дату с нулевым временем!
     * @return 
     */
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
    
    /* Добавление к дате указанного числа милисекунд */
    public static Date addMilliseconds(Date date, int miliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, miliseconds); 
        return cal.getTime();
    }
    
    /**
     * Добавление времени к дате 
     * @param source
     * @param timeAdd
     * @return 
     */
    public static Date addTime(Date source, Date timeAdd){
        Long seconds = timeAdd.getTime()/1000;  
        return addSeconds(source, seconds.intValue());
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
    
    /**
     * Вычисление даты по смещению, указанному в секундах
     * @param dateBegin
     * @param deltaSeconds
     * @return 
     */
    public static Date calculateDate(Date dateBegin, long deltaSeconds){
        Long elapsedDays = deltaSeconds / SECONDS_PER_DAY;
        dateBegin = addDays(dateBegin, elapsedDays.intValue());        
        deltaSeconds = deltaSeconds % SECONDS_PER_DAY;
        Long elapsedHours = deltaSeconds / SECONDS_PER_HOUR;        
        dateBegin = addHour(dateBegin, elapsedHours.intValue());
        return dateBegin;
    }
    
    /**
     * Находит ближайшую предстоящую дату на основании данных из списка дней недели
     * @param days
     * @param current
     * @return 
     */
    public static Date getNextDateByDays(String[] days, Date current){
        LocalDate ld = toLocalDate(current);
        DayOfWeek curentDay = ld.getDayOfWeek();
        DayOfWeek nextDay = DayOfWeek.SATURDAY;
        for (String strDay : days) {  
            int day = Integer.parseInt(strDay);
            if (day > curentDay.getValue() && day <= nextDay.getValue()){
                nextDay = DayOfWeek.of(day);
            }

        }
        ld = ld.with(TemporalAdjusters.next(nextDay));
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }  
    
    public static Date convertHourToUTCTimeZone(Date inputDate)  {        
        Date result = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String dateString = ""+((hours>9)?""+hours:"0"+hours)+":"+((hours>9)?""+minutes:"0"+minutes)+"";
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));        
            result = sdf.parse(dateString);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
      * convert a date with hour format (HH:mm) from UTC time zone to local time zone
      * @param inputDate
      * @return 
      * @throws java.text.ParseException
      */
    public static Date convertHourFromUTCToLocalTimeZone(Date inputDate) {
        Date result = null;
        try {
            Date localFromGmt = new Date(inputDate.getTime() - TimeZone.getDefault().getOffset(inputDate.getTime()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(localFromGmt);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String dateString = ""+((hours>9)?""+hours:"0"+hours)+":"+((hours>9)?""+minutes:"0"+minutes)+"";
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            result = sdf.parse(dateString);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
     }
}
