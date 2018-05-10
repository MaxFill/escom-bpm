package com.maxfill.escom.system.services.notification;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.services.notification.NotificationSettings;
import com.maxfill.services.notification.NotificationTimer;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
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
        notificationService.makeNotifications();
    }

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_NOTIFICATION;
    }
    
    @Override
    protected boolean isEastInitClosed(){
        return true;
    }
}