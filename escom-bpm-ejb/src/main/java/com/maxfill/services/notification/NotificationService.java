package com.maxfill.services.notification;

import com.maxfill.services.Services;

/**
 *
 */
public interface NotificationService {
    NotificationSettings createSettings(Services service);
    void makeNotifications();
}
