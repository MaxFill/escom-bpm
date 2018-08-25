package com.maxfill.services.worktime;

import com.maxfill.model.Dict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.utils.EscomUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Сущность для хранения дат - исключений: как не рабочих, так и рабочих (если перенос), а так же сокращённых дней
 * @author maksim
 */
@Entity
@Table(name = "workTimeCalendar",
        indexes = {@Index(name="WorkTimeCalendar_INDEX", columnList = "DateCalendar", unique = true)})
public class WorkTimeCalendar implements Serializable, Dict{
    private static final long serialVersionUID = -102040552117502275L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @Column(name = "DateCalendar")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date date;
    
    @Basic(optional = false)
    @Column(name = "WorkTime")
    private Integer workTime;   //кол-во рабочих часов в дне

    @Basic(optional = false)
    @Column(name = "BeginTime")
    private Integer beginTime;  //начало рабочего дня в милисекундах    
    
    @Basic(optional = false)
    @Column(name = "DayType")
    private String dayType;
    
    @JoinColumn(name = "Staff", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff staff;        //штатная единица, для которой задано исключение
        
    @Transient    
    private Boolean standart = false;
    @Transient
    protected String uid;
    
    public WorkTimeCalendar() {
        this.uid = EscomUtils.generateGUID();
    }

    public WorkTimeCalendar(Date date, Integer workTime, String dayType) {
        this.date = date;
        this.workTime = workTime;
        this.dayType = dayType;
        this.uid = EscomUtils.generateGUID();
    }     
    
    public WorkTimeCalendar(Date date, Integer beginTime, Integer workTime, String dayType) {
        this.date = date;
        this.workTime = workTime;
        this.dayType = dayType;
        this.beginTime = beginTime;
        this.uid = EscomUtils.generateGUID();
    }     
     
    public boolean isWorkDay(){
        return "workday".equals(dayType);
    }    
    public boolean isHolliDay(){
        return "hollyday".equals(dayType);
    }
    public boolean isWeekEnd(){
        return "weekend".equals(dayType);
    }
    
    public void setHolliDay(){
        dayType = "hollyday";
    }
    public void setWeekEnd(){
        dayType = "weekend";
    }
    public void setWorkDay(){
        dayType = "workday";
    }
    
    public String getStyle(){
        return dayType;
    }
    
    public Date getStart(){
        return DateUtils.addMilliseconds(date, beginTime);
    }
    
    public Date getEnd(){
        Integer endTime = beginTime + workTime * 3600 * 1000;
        return DateUtils.addMilliseconds(date, endTime);
    }
    
    /* GETS & SETS */

    public Boolean getStandart() {
        return standart;
    }
    public void setStandart(Boolean standart) {
        this.standart = standart;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getWorkTime() {
        return workTime;
    }
    public void setWorkTime(Integer workTime) {
        this.workTime = workTime;
    }

    public Staff getStaff() {
        return staff;
    }
    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Integer getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Integer beginTime) {
        this.beginTime = beginTime;
    }

    public String getDayType() {
        return dayType;
    }
    public void setDayType(String dayType) {
        this.dayType = dayType;
    }
          
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.uid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorkTimeCalendar other = (WorkTimeCalendar) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WorkTimeCalendar{" + "id=" + id + '}';
    }
        
}
