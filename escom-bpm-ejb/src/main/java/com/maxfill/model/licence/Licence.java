
package com.maxfill.model.licence;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author mfilatov
 */
public class Licence implements Serializable{

    private static final long serialVersionUID = 1156677344730726415L;
    private String versionNumber;
    private String releaseNumber;
    private Integer totalLicence;   //всего лицензий
    private String licenceName;
    private String licenceNumber;
    private Date termLicence;     //срок действия лицензии
    private Date dateUpdate;
    
    //EscomUtils.getBandleLabel("Indefinitely") TODO нужно выводить для бессрочных лицензий!
    public Integer getTotalLicence() {
        return totalLicence;
    }

    public String getLicenceName() {
        return licenceName;
    }

    public Date getTermLicence() {
        return termLicence;
    }
    
    /**
    * Проверка что лицензия не просочена
    * @return 
    */
    public Boolean isExpired(){
        return false;
        //return termLicence.after(new Date()); //TODO нужно сделать проверку на срок действия лицензии!
    }
    
    public void setTermLicence(Date termLicence) {
        this.termLicence = termLicence;
    }
    
    public String getVersionNumber() {
        return versionNumber;
    }
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getReleaseNumber() {
        return releaseNumber;
    }
    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
    }  

    public String getLicenceNumber() {
        return licenceNumber;
    }
    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public void setTotalLicence(Integer totalLicence) {
        this.totalLicence = totalLicence;
    }

    public void setLicenceName(String licenceName) {
        this.licenceName = licenceName;
    }
    
    public Date getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }
}
