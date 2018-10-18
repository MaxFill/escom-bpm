package com.maxfill.services.worktime;

import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.staff.Staff;
import java.util.Date;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public interface WorkTimeService {
    Date getStartDate(Date dateBegin);
    Date getFinishDate(Date dateBegin, int minute);
    Date calcWorkDayByStaff(Date startDate, Integer deltasec, Staff staff);
    Date calcWorkDayByCompany(Date startDate, Integer deltasec, Company company);
    WorkTimeCalendar getWorkTimeDate(Date date, Staff staff, Company company);
    void update(WorkTimeCalendar wtc);
}
