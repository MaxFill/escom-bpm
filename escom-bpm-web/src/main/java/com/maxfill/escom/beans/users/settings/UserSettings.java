package com.maxfill.escom.beans.users.settings;

import com.maxfill.utils.Tuple;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    private ConcurrentHashMap<String, Tuple<Integer, Integer>> formsSize = new ConcurrentHashMap<>();
    
    @XmlElement(name = "ExplFormSize")
    private ConcurrentHashMap<String, String> explFormParam = new ConcurrentHashMap<>();
        
    @XmlElement(name = "ReportsSettings")
    private ConcurrentHashMap<String, UserReportsSettings> reportSetting = new ConcurrentHashMap<>();
    
    @XmlElement(name = "AgreeLicens")
    private boolean agreeLicense = false;

    public boolean isAgreeLicense() {
        return agreeLicense;
    }
    public void setAgreeLicense(boolean agreeLicense) {
        this.agreeLicense = agreeLicense;
    }
        
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

    public ConcurrentHashMap<String, Tuple<Integer, Integer>> getFormsSize() {
        return formsSize;
    }
    public void setFormsSize(ConcurrentHashMap<String, Tuple<Integer, Integer>> formsSize) {
        this.formsSize = formsSize;
    }

    public ConcurrentHashMap<String, String> getExplFormParam() {
        return explFormParam;
    }
    public void setExplFormParam(ConcurrentHashMap<String, String> explFormParam) {
        this.explFormParam = explFormParam;
    }

    public ConcurrentHashMap<String, UserReportsSettings> getReportSetting() {
        return reportSetting;
    }
    public void setReportSetting(ConcurrentHashMap<String, UserReportsSettings> reportSetting) {
        this.reportSetting = reportSetting;
    }       
    
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);        
        return sw.toString();
    }
    
}
