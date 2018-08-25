package com.maxfill.services.worktime;

import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.StaffFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        Company company = staffFacade.findCompanyForStaff(staff);
        while(deltasec >= 0){
            WorkTimeCalendar wtc = getWorkTimeDate(date, staff, company);
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
    public WorkTimeCalendar getWorkTimeDate(Date date, Staff staff, Company company){
        //сначала ищем исключения для конкретной штед
        if (staff != null){
            List<WorkTimeCalendar> dates = workTimeFacade.findDateByStaff(date, staff);
            if (!dates.isEmpty()){
                WorkTimeCalendar wtc = dates.get(0);
                wtc.setStandart(Boolean.FALSE);
                return wtc;
            }
        }
        //раз нет, то ищем вообще исключение
        List<WorkTimeCalendar> dates = workTimeFacade.findDate(date);
        if (!dates.isEmpty()){
            WorkTimeCalendar wtc = dates.get(0);
            wtc.setStandart(Boolean.FALSE);
            return wtc;
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
        wtc.setDate(calendar.getTime());
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            wtc.setWeekEnd();
            makeDefaultWorkTime(wtc, staff, company);
        } else {
            wtc.setWorkDay();
            makeDefaultWorkTime(wtc, staff, company);
        }
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
    
    @Override
    public boolean isHolliday(Date date, Staff staff, Company company){
        WorkTimeCalendar wtc = getWorkTimeDate(date, staff, company);
        return wtc.isHolliDay();
    }
    
    @Override
    public boolean isWorkday(Date date, Staff staff, Company company){
        WorkTimeCalendar wtc = getWorkTimeDate(date, staff, company);
        return wtc.isWorkDay();
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
