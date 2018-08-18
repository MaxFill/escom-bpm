package com.maxfill.model.licence;

import com.maxfill.dictionary.DictModules;
import com.maxfill.utils.DateUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    
    //EscomUtils.getBandleLabel("Indefinitely") TODO нужно выводить для бессрочных лицензий!

    private final Set<String> modules = new HashSet<>();

    public Licence() {
        modules.add(DictModules.MODULE_CONCORD);
        modules.add(DictModules.MODULE_PROCESSES);
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
        return modules.contains(moduleName);
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
}
