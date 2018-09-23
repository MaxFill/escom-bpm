package com.maxfill.services.notification;

import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Таймер рассылки системных уведомлений
 */
@Stateless
public class NotificationTimer extends BaseTimer<NotificationSettings> {
    
    @EJB
    private NotificationService notificationService;

    public NotificationTimer() {
        super(NotificationSettings.class);
    }
    
    @Override
    public void doExecuteTask(Services service, NotificationSettings settings) {         
        ServicesEvents selectedEvent = startAction(service);
        try { 
            notificationService.makeNotifications(getDetailInfo());       
            selectedEvent.setResult(RESULT_SUCCESSFULLY);
        } finally{
            finalAction(selectedEvent);                       
        }
    }
    
}
