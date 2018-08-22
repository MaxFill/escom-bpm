package com.maxfill.services.worktime;

import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.StaffFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.time.DateUtils;

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
     * @return 
     */
    @Override
    public Date calcWorkDay(Date date, Integer deltasec, Staff staff){        
        while(deltasec >= 0){
            WorkTimeCalendar wtc = getWorkTimeDate(date, staff);
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
    public WorkTimeCalendar getWorkTimeDate(Date date, Staff staff){
        //сначала ищем исключения для конкретной штед
        List<WorkTimeCalendar> dates = workTimeFacade.findDateByStaff(date, staff);
        if (!dates.isEmpty()){
            return dates.get(0);
        }
        //раз нет, то ищем вообще исключение
        dates = workTimeFacade.findDate(date);
        if (!dates.isEmpty()){
            return dates.get(0);
        }
        //раз нет исключений, то формируем дату по дефолту
        WorkTimeCalendar wtc = new WorkTimeCalendar();        
        wtc.setStaff(staff);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        wtc.setDate(calendar.getTime());
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            wtc.setHolliDay();
        } else {
            wtc.setWorkDay();
            makeDefaultWorkTime(wtc, staff);
        }        
        return wtc;
    }
    
    /**
     * Формирует начало, конец и длительность рабочего дня
     * @param staff
     * @return 
     */
    private void makeDefaultWorkTime(WorkTimeCalendar wtc, Staff staff){
        Integer workTime = 8;
        Integer beginTime = 9 * 60 * 60;
        if (!staff.isInheritsWorkTime()){
            workTime = staff.getWorkTime();
            beginTime = staff.getBeginTime();
        } else {
            Company company = staffFacade.findCompanyForStaff(staff);
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
    
    @Override
    public boolean isHolliday(Date date, Staff staff){
        WorkTimeCalendar wtc = getWorkTimeDate(date, staff);
        return wtc.isHolliDay();
    }
    
    @Override
    public boolean isWorkday(Date date, Staff staff){
        WorkTimeCalendar wtc = getWorkTimeDate(date, staff);
        return wtc.isWorkDay();
    }
}
