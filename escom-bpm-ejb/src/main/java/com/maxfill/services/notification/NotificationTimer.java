package com.maxfill.services.notification;

import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import java.util.Date;
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
    protected ServicesEvents doExecuteTask(Services service, NotificationSettings settings) {         
        ServicesEvents selectedEvent = startAction(service);
        try { 
            notificationService.makeNotifications();            
            selectedEvent.setResult(RESULT_SUCCESSFULLY);
        } finally{
            Date finishDate = new Date();
            detailInfoAddRow("The service finished in " + DateUtils.dateToString(finishDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale() ));
            selectedEvent.setDateFinish(finishDate);
            servicesEventsFacade.create(selectedEvent);
            service.getServicesEventsList().add(selectedEvent);                        
        }         
         return selectedEvent;
    }
    
}
