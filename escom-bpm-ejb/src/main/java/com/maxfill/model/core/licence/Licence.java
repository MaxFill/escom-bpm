package com.maxfill.model.core.licence;

import com.maxfill.utils.DateUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Licence implements Serializable{
    private static final long serialVersionUID = 1156677344730726415L;

    @XmlElement(name = "Licensor")
    private String licensor;

    @XmlElement(name = "Total")
    private Integer total;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Number")
    private String number;

    @XmlElement(name = "DateTerm")
    private Date dateTerm;
    
    @XmlElement(name = "Modules")
    private String modulesJSON;
    
    //EscomUtils.getBandleLabel("Indefinitely") TODO нужно выводить для бессрочных лицензий!

    private List<String> modules;

    public Licence() {
    }

    /**
     * Признак того, что лицензия просрочена
     * @return
     */
    public Boolean isExpired(){
        Date currentClearDate = DateUtils.clearDate(new Date());
        Date licenceClearDate = DateUtils.clearDate(dateTerm);
        return !licenceClearDate.after(currentClearDate);         
    }

    /**
     * Определяет возможность использования модулей
     * @param moduleName
     * @return
     */
    public Boolean isCanUses(String moduleName){
        return getModules().contains(moduleName);
    }

    /* gets & sets */
    
    public Integer getTotal() {
        return total;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public Date getDateTerm() {
        return dateTerm;
    }

    public String getLicensor() {
        return licensor;
    }

    public String getModulesJSON() {
        return modulesJSON;
    }
    public void setModulesJSON(String modulesJSON) {
        this.modulesJSON = modulesJSON;
    }  

    public List<String> getModules() {
        if (modules == null && modulesJSON != null ){             
            modules = Arrays.asList(modulesJSON.split(","));
        }
        return modules;
    }
       
}