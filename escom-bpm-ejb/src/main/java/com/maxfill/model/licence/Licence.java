package com.maxfill.model.licence;

import com.maxfill.utils.DateUtils;
import java.io.Serializable;
import java.util.Date;

public final class Licence implements Serializable{
    private static final long serialVersionUID = 1156677344730726415L;
    
    private String versionNumber;       //версия используемого релиза
    private String releaseNumber;       //номер используемого релиза
    private String releasePage;         //страница на сайте тех. поддержки используемого релиза
    private Date releaseDate;           //дата используемого релиза    
    
    private String actualVersionNumber; //версия актуального релиза
    private String actualReleaseNumber; //номер актуального релиза
    private String actualReleasePage;   //страница на сайте тех. поддержки актуального релиза
    private Date actualReleaseDate;     //дата актуального релиза
      
    private final String licensor;
    private final Integer totalLicence;       //всего лицензий
    private final String termLicence;         //срок действия лицензии
    private final String licenceName;
    private final String licenceNumber;
    private final Date dateTermLicence;
    
    //EscomUtils.getBandleLabel("Indefinitely") TODO нужно выводить для бессрочных лицензий!

    public Licence(String termLicence, Integer totalLicence, String licenceName, String licenceNumber, String licensor) {
        this.termLicence = termLicence;
        this.totalLicence = totalLicence;
        this.licenceName = licenceName;
        this.licenceNumber = licenceNumber;
        this.licensor = licensor;

        Date newDate = DateUtils.convertStrToDate(termLicence);
        if (newDate == null){
            this.dateTermLicence = new Date();
        } else {
            this.dateTermLicence = newDate;
        }
    }
    
    public String getLicenceNumber() {
        return licenceNumber;
    }
        
    public String getLicensor() {
        return licensor;
    } 
    
    public Integer getTotalLicence() {
        return totalLicence;
    }
    
    public String getLicenceName() {
        return licenceName;
    }    

    public Date getDateTermLicence() {
        return dateTermLicence;
    }
        
    public Boolean isExpired(){
        Date currentClearDate = DateUtils.clearDate(new Date());
        Date licenceClearDate = DateUtils.clearDate(dateTermLicence);
        return !licenceClearDate.after(currentClearDate);         
    }

    public String getTermLicence() {
        return termLicence;
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

    public Date getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleasePage() {
        return releasePage;
    }
    public void setReleasePage(String releasePage) {
        this.releasePage = releasePage;
    }

    public String getActualVersionNumber() {
        return actualVersionNumber;
    }
    public void setActualVersionNumber(String actualVersionNumber) {
        this.actualVersionNumber = actualVersionNumber;
    }

    public String getActualReleaseNumber() {
        return actualReleaseNumber;
    }
    public void setActualReleaseNumber(String actualReleaseNumber) {
        this.actualReleaseNumber = actualReleaseNumber;
    }

    public Date getActualReleaseDate() {
        return actualReleaseDate;
    }
    public void setActualReleaseDate(Date actualReleaseDate) {
        this.actualReleaseDate = actualReleaseDate;
    }

    public String getActualReleasePage() {
        return actualReleasePage;
    }
    public void setActualReleasePage(String actualReleasePage) {
        this.actualReleasePage = actualReleasePage;
    }
    
}
