package com.maxfill.services.notification;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Параметры службы формирования системных уведомлений
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class NotificationSettings implements Serializable{    
    private static final long serialVersionUID = 3976997381232643631L;

    public NotificationSettings() {
    }
    
    
    
}
