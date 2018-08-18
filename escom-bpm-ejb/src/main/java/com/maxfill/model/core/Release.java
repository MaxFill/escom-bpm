package com.maxfill.model.core;

import java.io.Serializable;
import java.util.Date;

/**
 * Класс сущности "Релиз". Содержит информацию о текущем релизе программы
 */
public class Release implements Serializable{
    private static final long serialVersionUID = 2806296167934868905L;

    private String versionNumber;       //версия используемого релиза
    private String releaseNumber;       //номер используемого релиза
    private String releasePage;         //страница на сайте тех. поддержки используемого релиза
    private Date releaseDate;           //дата используемого релиза

    private String actualVersionNumber; //версия актуального релиза
    private String actualReleaseNumber; //номер актуального релиза
    private String actualReleasePage;   //страница на сайте тех. поддержки актуального релиза
    private Date actualReleaseDate;     //дата актуального релиза

    public Release() {
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
