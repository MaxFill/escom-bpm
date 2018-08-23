package com.maxfill.services.worktime;

import com.maxfill.model.Dict;
import com.maxfill.model.staffs.Staff;
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
    private Integer beginTime;  //начало рабочего дня в секундах    
    
    @NotNull
    @Basic(optional = false)
    @Column(name = "DayType")
    private String dayType;
    
    @JoinColumn(name = "Staff", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff staff;        //штатная единица, для которой задано исключение
        
    public WorkTimeCalendar() {
    }

    public WorkTimeCalendar(Date date, Integer workTime, String dayType) {
        this.date = date;
        this.workTime = workTime;
        this.dayType = dayType;
    }     
    
    public WorkTimeCalendar(Date date, Integer beginTime, Integer workTime, String dayType) {
        this.date = date;
        this.workTime = workTime;
        this.dayType = dayType;
        this.beginTime = beginTime;
    }     
     
    public boolean isWorkDay(){
        return "workday".equals(dayType);
    }    
    public boolean isHolliDay(){
        return "hollyday".equals(dayType);
    }
    public void setHolliDay(){
        dayType = "hollyday";
    }
    public void setWorkDay(){
        dayType = "workday";
    }
    
    public String getStyle(){
        return dayType;
    }
    
    public Date getStart(){        
        return DateUtils.addSeconds(date, beginTime);
    }
    
    public Date getEnd(){
        Integer endTime = beginTime + workTime * 3600;
        return DateUtils.addSeconds(date, endTime);
    }
    
    /* GETS & SETS */

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
        
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
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
