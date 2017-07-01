package com.maxfill.model.licence;

import com.maxfill.utils.DateUtils;
import java.io.Serializable;
import java.util.Date;

public class Licence implements Serializable{
    private static final long serialVersionUID = 1156677344730726415L;
    
    private String versionNumber;       //версия используемого релиза
    private String releaseNumber;       //номер используемого релиза
    private String releasePage;         //страница на сайте тех. поддержки используемого релиза
    private Date releaseDate;           //дата используемого релиза    
    
    private String actualVersionNumber; //версия актуального релиза
    private String actualReleaseNumber; //номер актуального релиза
    private String actualReleasePage;   //страница на сайте тех. поддержки актуального релиза
    private Date actualReleaseDate;     //дата актуального релиза
    
    private Integer totalLicence;       //всего лицензий
    private String licenceName;
    private String licenceNumber;
    private Date termLicence;           //срок действия лицензии
    
    //EscomUtils.getBandleLabel("Indefinitely") TODO нужно выводить для бессрочных лицензий!
    public Integer getTotalLicence() {
        return totalLicence;
    }
    public void setTotalLicence(Integer totalLicence) {
        this.totalLicence = totalLicence;
    }
    
    public String getLicenceName() {
        return licenceName;
    }
    public void setLicenceName(String licenceName) {
        this.licenceName = licenceName;
    }
    
    public Date getTermLicence() {
        return termLicence;
    }
    public String getStrTermLicence() {
        Date clearDate = DateUtils.clearDate(termLicence);
        return DateUtils.dateToString(clearDate, "dd.MM.YY");
    }
    
    public Boolean isExpired(){
        Date currentClearDate = DateUtils.clearDate(new Date());
        Date licenceClearDate = DateUtils.clearDate(termLicence);
        return !licenceClearDate.after(currentClearDate);         
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
