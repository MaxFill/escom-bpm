package com.maxfill.services.notification;

import com.maxfill.model.basedict.task.Task;
import com.maxfill.services.Services;

/**
 * Сервис рассылки уведомлений - напоминаний
 */
public interface NotificationService {
    NotificationSettings createSettings(Services service);
    void makeNotifications(StringBuilder sb);
    void makeNotification(Task task, String msg, Object[] msgParams);
}
