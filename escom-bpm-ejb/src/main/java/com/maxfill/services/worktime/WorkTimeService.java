package com.maxfill.services.worktime;

import java.util.Date;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public interface WorkTimeService {
    Date getStartDate(Date dateBegin);
    Date getFinishDate(Date dateBegin, int minute);
}
