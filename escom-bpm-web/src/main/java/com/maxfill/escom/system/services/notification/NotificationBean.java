package com.maxfill.escom.system.services.notification;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.services.notification.NotificationSettings;
import com.maxfill.services.notification.NotificationTimer;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы настроек системной службы рассылки уведомлений
 */
@Named
@ViewScoped
public class NotificationBean extends BaseServicesBean<NotificationSettings>{
    private static final long serialVersionUID = -8110993693519349650L;

    @EJB
    private NotificationTimer notificationTimer;           
    @EJB
    private NotificationService notificationService;
             
    @Override
    protected NotificationSettings createSettings() {
        return notificationService.createSettings(service);
    }

    @Override
    public BaseTimer getTimerFacade() {
        return notificationTimer;
    }

    @Override
    public int getSERVICE_ID() {
        return DictServices.NOTIFICATION_ID;
    }

    @Override
    public void doRunService() {        
        ServicesEvents selectedEvent = notificationTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);  
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_NOTIFICATION;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ServiceNotification");
    }
    
    /**
     * Проверка работы службы
     */
    public void onCheckServices(){
        doRunService();
    }
}