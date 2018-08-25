package com.maxfill.services.worktime;

import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
import java.util.Date;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public interface WorkTimeService {
    Date getStartDate(Date dateBegin);
    Date getFinishDate(Date dateBegin, int minute);
    Date calcWorkDay(Date startDate, Integer deltasec, Staff staff);
    WorkTimeCalendar getWorkTimeDate(Date date, Staff staff, Company company);
    boolean isHolliday(Date date, Staff staff, Company company);
    boolean isWorkday(Date date, Staff staff, Company company);
    void update(WorkTimeCalendar wtc);
}
