
package com.maxfill.escom.beans.users.settings;

import com.maxfill.utils.Tuple;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* Класс персональных настроек для пользователя  */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class UserSettings implements Serializable{
    private static final long serialVersionUID = 3054820908994628994L;
   
    @XmlElement(name = "Theme")
    private String theme;
    
    @XmlElement(name = "Language")
    private String language;

    @XmlElement(name = "FormSize")
    private ConcurrentHashMap<String, Tuple<Double, Double>> formsSize = new ConcurrentHashMap<>();
    
    @XmlElement(name = "ExplFormSize")
    private ConcurrentHashMap<String, String> explFormParam = new ConcurrentHashMap<>();
            
    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }    

    public ConcurrentHashMap<String, Tuple<Double, Double>> getFormsSize() {
        return formsSize;
    }
    public void setFormsSize(ConcurrentHashMap<String, Tuple<Double, Double>> formsSize) {
        this.formsSize = formsSize;
    }

    public ConcurrentHashMap<String, String> getExplFormParam() {
        return explFormParam;
    }
    public void setExplFormParam(ConcurrentHashMap<String, String> explFormParam) {
        this.explFormParam = explFormParam;
    }
            
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
    
}
