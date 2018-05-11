package com.maxfill.services.worktime;

import java.util.Date;

/**
 * Обеспечивает вычисление дат с учётом рабочего времени и производственного календаря
 */
public class WorkTimeImpl implements WorkTimeService{

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
    
}
