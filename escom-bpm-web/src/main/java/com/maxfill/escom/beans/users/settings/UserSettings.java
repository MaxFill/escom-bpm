
package com.maxfill.escom.beans.users.settings;

import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс настроек для пользователя
 * @author Maxim
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class UserSettings implements Serializable{

    private static final long serialVersionUID = 3054820908994628994L;

    public UserSettings() {
    }
    
    @XmlElement(name = "Theme")
    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
    
}
