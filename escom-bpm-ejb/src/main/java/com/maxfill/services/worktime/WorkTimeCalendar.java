package com.maxfill.services.worktime;

import com.maxfill.model.Dict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import javax.persistence.Transient;

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
    private String dateCalendar;
    
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
    private final String uid;
    
    public WorkTimeCalendar() {
        this.uid = EscomUtils.generateGUID();
    }

    public WorkTimeCalendar(String date, Integer workTime, String dayType) {
        this.dateCalendar = date;
        this.workTime = workTime;
        this.dayType = dayType;
        this.uid = EscomUtils.generateGUID();
    }     
    
    public WorkTimeCalendar(String date, Integer beginTime, Integer workTime, String dayType) {
        this.dateCalendar = date;
        this.workTime = workTime;
        this.dayType = dayType;
        this.beginTime = beginTime;
        this.uid = EscomUtils.generateGUID();
    }     
     
    public boolean isWorkDay(){
        return "Workday".equals(dayType);
    }    
    public boolean isHolliDay(){
        return "Hollyday".equals(dayType);
    }
    public boolean isWeekEnd(){
        return "Weekend".equals(dayType);
    }
    
    public void setHolliDay(){
        dayType = "Hollyday";
    }
    public void setWeekEnd(){
        dayType = "Weekend";
    }
    public void setWorkDay(){
        dayType = "Workday";
    }
    
    public String getStyle(){
        return dayType;
    }    
     
    @Transient
    public Date getStart(){
        Date dt = getDate();
        return DateUtils.addMilliseconds(dt, beginTime);
    }
    @Transient
    public Date getEnd(){
        Date dt = getDate();
        Integer endTime = beginTime + workTime * 3600 * 1000;
        return DateUtils.addMilliseconds(dt, endTime);
    }
    
    @Transient
    public Date getDate(){        
        return DateUtils.convertStrToDate(dateCalendar, "MM/dd/yy");
    }
    @Transient
    public void setDate(Date date){        
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        this.dateCalendar = df.format(date);        
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

    public String getDateCalendar() {
        return dateCalendar;
    }
    public void setDateCalendar(String dateCalendar) {
        this.dateCalendar = dateCalendar;
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
