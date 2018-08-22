package com.maxfill.services.worktime;

import com.maxfill.model.staffs.Staff;
import java.util.Date;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public interface WorkTimeService {
    Date getStartDate(Date dateBegin);
    Date getFinishDate(Date dateBegin, int minute);
    Date calcWorkDay(Date startDate, Integer deltasec, Staff staff);
    WorkTimeCalendar getWorkTimeDate(Date date, Staff staff);
    boolean isHolliday(Date date, Staff staff);
    boolean isWorkday(Date date, Staff staff);     
}
