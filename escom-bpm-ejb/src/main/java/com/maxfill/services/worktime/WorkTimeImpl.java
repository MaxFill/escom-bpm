package com.maxfill.services.worktime;

import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.utils.DateUtils;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
@Stateless
public class WorkTimeImpl implements WorkTimeService{
    protected static final Logger LOGGER = Logger.getLogger(WorkTimeImpl.class.getName());
    
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
    public Date calcWorkDayByStaff(Date date, Integer deltasec, Staff staff){        
        Company company = staffFacade.findCompanyForStaff(staff);
        while(deltasec >= 0){
            WorkTimeCalendar wtc = getWorkTimeDate(date, staff, company);
            if (wtc.isWorkDay()){
                Integer duration = wtc.getWorkTimeHour()* 3600 + wtc.getWorkTimeMinute() * 60;
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
    
    /**
     * Вычисляет рабочую дату и время, на основании рабочего календаря указанной компании    
     * Рабочее время вычисляется с укётом рабочего календаря (например, если сокращённый день)
     * @param date
     * @param deltasec
     * @param company
     * @return 
     */
    @Override
    public Date calcWorkDayByCompany(Date date, Integer deltasec, Company company){                
        while(deltasec >= 0){
            WorkTimeCalendar wtc = getWorkTimeDate(date, null, company);
            if (wtc.isWorkDay()){
                Integer duration = wtc.getWorkTimeHour()* 3600 + wtc.getWorkTimeMinute() * 60;
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
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        String dts = df.format(date);
        
        //сначала ищем исключения для конкретной шт.ед
        if (staff != null){                         
            List<WorkTimeCalendar> dates = workTimeFacade.findDateByStaff(dts, staff);
            if (!dates.isEmpty()){               
                return prepareWtc(dates);
            }
        }
        
        //раз нет, то ищем вообще исключение        
        List<WorkTimeCalendar> dates = workTimeFacade.findDate(dts);        
        if (!dates.isEmpty()){
            WorkTimeCalendar companyWtc = prepareWtc(dates);
            
            if (staff == null) return companyWtc;
            
            WorkTimeCalendar staffWtc = createWtc(date, staff, company);
            try {
                BeanUtils.copyProperties(staffWtc, companyWtc);
                staffWtc.setId(null);
                staffWtc.setStaff(staff);
            }catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }            
            return staffWtc;
        }
        
        //раз нет исключений, то формируем дату по дефолту        
        return createWtc(date, staff, company);
    }
    
    private WorkTimeCalendar createWtc(Date date, Staff staff, Company company){
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
            wtc.setWorkTimeHour(0);
            wtc.setWorkTimeMinute(0);
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
        Integer workTimeHour = 0;
        Integer workTimeMinute = 0;
        Integer beginTime = 0;
        if (staff != null && !staff.isInheritsWorkTime()){
            workTimeHour = staff.getWorkTimeHour();
            workTimeMinute = staff.getWorkTimeMinute();
            beginTime = staff.getBeginTime();
        } else {
            if (company != null){
                workTimeHour = company.getWorkTimeHour();
                workTimeMinute = company.getWorkTimeMinute();
                beginTime = company.getBeginTime();
            }
        }
        wtc.setWorkTimeHour(workTimeHour);
        wtc.setWorkTimeMinute(workTimeMinute);
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
    
    @Override
    public boolean checkStaffAvailable(Staff staff, Company company, Date beginDate, Date endDate){
        boolean flag = true;
        while(beginDate.before(endDate) || beginDate.equals(endDate)){
            WorkTimeCalendar corpWtc = getWorkTimeDate(beginDate, null, company);
            WorkTimeCalendar staffWtc = getWorkTimeDate(beginDate, staff, null);
            if (corpWtc.isWorkDay() && !staffWtc.isWorkDay()){
                flag = false;
                break;
            }
            beginDate = DateUtils.addDays(beginDate, 1);
        }
        return flag;
    }
}
