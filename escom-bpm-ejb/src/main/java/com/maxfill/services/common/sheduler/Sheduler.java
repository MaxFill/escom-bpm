
package com.maxfill.services.common.sheduler;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author mfilatov
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Sheduler implements Serializable{   
    private static final long serialVersionUID = -8740884204663924630L;
    
    /**
     * Признак автозапуска службы
     */
    @XmlElement(name = "AutoStart")
    private Boolean autoStart = true;

    /**
     * Тип повтора:
     * 0 - ежедневно
     * 1 - еженедельно
     * 2 - ежемесячно
     */
    @XmlElement(name = "RepeatType")
    private Integer repeatType = 0;
    
    /**
     * Значение типа интервала:
     * 0 - каждую минуту
     * 1 - каждый час
     */
    @XmlElement(name = "RepeatEachType")
    private Integer repeatEachType = 2;
    
    /**
     * Значение интервала повтора
     */
    @XmlElement(name = "Interval")
    private Integer interval = 1;
    
    /**
     * Дата запуска и начала отсчёта
     */
    @XmlElement(name = "StartDate")
    private Date startDate = new Date();
    
    /**
     * Список дней в которые будет запуск сервиса
     */
    @XmlElement(name = "DayOfWeek")
    private String[] dayOfWeek;
    
    /**
     * Интервал повтора в днях, если установлен режим "Ежедневно"
     */
    @XmlElement(name = "RepeatDayInterval")
    private Integer repeatDayInterval = 1;
    
    public Sheduler() {
    }

    public Boolean getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(Boolean autoStart) {
        this.autoStart = autoStart;
    }

    public Integer getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(Integer repeatType) {
        this.repeatType = repeatType;
    }

    public Integer getRepeatEachType() {
        return repeatEachType;
    }

    public void setRepeatEachType(Integer repeatEachType) {
        this.repeatEachType = repeatEachType;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String[] getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String[] dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getRepeatDayInterval() {
        return repeatDayInterval;
    }

    public void setRepeatDayInterval(Integer repeatDayInterval) {
        this.repeatDayInterval = repeatDayInterval;
    }
        
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
