package com.maxfill.services.worktime;

import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
@Stateless
public class WorkTimeImpl implements WorkTimeService{
    public static final int SECONDS_PER_DAY = 86400;
    @EJB
    private WorkTimeFacade workTimeFacade;
    @EJB
    private StaffFacade staffFacade;
    
    /**
     * Вычисляет рабочую дату и время, на основании рабочего календаря указанной штатной единицы     
     * Рабочее время вычисляется с укётом рабочего календаря (например, если сокращённый день)
     * @param date
     * @param deltasec
     * @param staff
     * @param locale
     * @return 
     */
    @Override
    public Date calcWorkDay(Date date, Integer deltasec, Staff staff, Locale locale){        
        Company company = staffFacade.findCompanyForStaff(staff);
        while(deltasec >= 0){
            WorkTimeCalendar wtc = getWorkTimeDate(date, staff, company, locale);
            if (wtc.isWorkDay()){
                Integer duration = wtc.getWorkTime() * 3600;
                if (deltasec <= duration){
                    date = wtc.getDate();
                    Integer beginDay = wtc.getBeginTime();                    
                    date = DateUtils.addMilliseconds(date, beginDay);
                    date = DateUtils.addSeconds(date, deltasec);
                    deltasec = -1;
                } else {
                    date = DateUtils.addDays(date, 1);
                    deltasec = deltasec - SECONDS_PER_DAY;
                }
            } else {  //если выходной, то пропускаем
                date = DateUtils.addDays(date, 1);
            }
        }
        return date;
    }
    
    @Override
    public WorkTimeCalendar getWorkTimeDate(Date date, Staff staff, Company company, Locale locale){
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        String dts = df.format(date);
        //сначала ищем исключения для конкретной штед
        if (staff != null){                         
            List<WorkTimeCalendar> dates = workTimeFacade.findDateByStaff(dts, staff);
            if (!dates.isEmpty()){               
                return prepareWtc(dates);
            }
        }
        //раз нет, то ищем вообще исключение        
        List<WorkTimeCalendar> dates = workTimeFacade.findDate(dts);        
        if (!dates.isEmpty()){            
            return prepareWtc(dates);
        }
        //раз нет исключений, то формируем дату по дефолту
        WorkTimeCalendar wtc = new WorkTimeCalendar();        
        wtc.setStaff(staff);
        wtc.setStandart(Boolean.TRUE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date clear = calendar.getTime();
        wtc.setDate(clear);
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            wtc.setWeekEnd();
            wtc.setWorkTime(0);
            wtc.setBeginTime(0);
        } else {
            wtc.setWorkDay();
            makeDefaultWorkTime(wtc, staff, company);
        }
        return wtc;
    }
    
    private WorkTimeCalendar prepareWtc(List<WorkTimeCalendar> wtcs){
        WorkTimeCalendar wtc = wtcs.get(0);
        wtc.setStandart(Boolean.FALSE);
        return wtc;
    }
    
    /**
     * Формирует начало, конец и длительность рабочего дня
     * @param staff
     * @return 
     */
    private void makeDefaultWorkTime(WorkTimeCalendar wtc, Staff staff, Company company){
        Integer workTime = 0;
        Integer beginTime = 0;
        if (staff != null && !staff.isInheritsWorkTime()){
            workTime = staff.getWorkTime();
            beginTime = staff.getBeginTime();
        } else {
            if (company != null){
                workTime = company.getWorkTime();
                beginTime = company.getBeginTime();
            }
        }
        wtc.setWorkTime(workTime);
        wtc.setBeginTime(beginTime);
    }
    
    /**
     * Возвращает указанную дату, если она рабочая или ближайшую к ней рабочую дату 
     * @param dateBegin
     * @return 
     */
    @Override
    public Date getStartDate(Date dateBegin) {
        return dateBegin;
    }

    /**
     * Возвращает вычисленную рабочую дату с указанным смещением в минутах
     * @param dateBegin
     * @param minute
     * @return 
     */
    @Override
    public Date getFinishDate(Date dateBegin, int minute) {
        return dateBegin;
    }        

    /**
     * Обновление информации о событии в календаре рабочего времени
     * @param wtc 
     */
    @Override
    public void update(WorkTimeCalendar wtc) {
        if (wtc.getStandart() && wtc.getId() == null) return;
        
        if (!wtc.getStandart()){ //если день не стандартный
            if (wtc.getId() == null){ //если записи нет в базе, то создаём запись
                workTimeFacade.create(wtc);                
            } else {    
                workTimeFacade.edit(wtc);                
            }
        } else { //если день стандартный
            if (wtc.getId() != null){
                workTimeFacade.remove(wtc); //то удаляем запись из базы
            }
        }
    }
}
