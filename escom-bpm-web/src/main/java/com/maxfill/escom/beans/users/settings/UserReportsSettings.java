package com.maxfill.escom.beans.users.settings;

import java.io.Serializable;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class UserReportsSettings implements Serializable {
    private static final long serialVersionUID = -9134687911663005503L;

    public UserReportsSettings() {
    }
    
    @XmlElement(name = "settings")
    private HashMap<String, Object> setting = new HashMap<>();

    public HashMap<String, Object> getSetting() {
        return setting;
    }
    public void setSetting(HashMap<String, Object> setting) {
        this.setting = setting;
    }    
    
}
