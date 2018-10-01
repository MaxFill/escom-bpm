package com.maxfill.services.worktime;

import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.staff.Staff;
import java.util.Date;
import java.util.Locale;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public interface WorkTimeService {
    Date getStartDate(Date dateBegin);
    Date getFinishDate(Date dateBegin, int minute);
    Date calcWorkDay(Date startDate, Integer deltasec, Staff staff, Locale locale);
    WorkTimeCalendar getWorkTimeDate(Date date, Staff staff, Company company, Locale locale);
    void update(WorkTimeCalendar wtc);
}
