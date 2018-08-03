package com.maxfill.model.process.timers;

import com.maxfill.model.process.Process;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

/**
 * Сущность для хранения настроек таймера
 * @author maksim
 */
@Entity
@Table(name = "proc_timers")
public class ProcTimer implements Serializable {
    private static final long serialVersionUID = -1228064449198654818L;
    
    @TableGenerator(
        name = "ProcTimerIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "ProcTimer_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "ProcTimerIdGen")
    private Integer id;
    
    @Column(name = "Name")
    private String name;
    
    /**
     * Ссылка на процесс
     */
    @NotNull
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;
    
    /**
     * Дата запуска и начала отсчёта
     */
    @Column(name = "StartDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate = new Date();
    
    /**
     * Тип повтора:
     * no          - без повтора 
     * everyday    - ежедневно
     * everyweek   - еженедельно
     * everymounth - ежемесячно
     */
    @Column(name = "RepeatType")    
    private String repeatType = "no";
    
    /**
     * Значение интервала для типа повтора в секундах
     */
    @Column(name = "RepeatTypeInterval")
    private Integer repeatTypeInterval = 1;
    
    /**
     * Тип интервала повтора:
     * 0 - каждую минуту
     * 1 - каждый час
     * 2 - каждый день
     */
    @Column(name = "RepeatEachType")
    private Integer repeatEachType = 2;
    
    /**
     * Значение интервала повтора в секундах
     */
    @Column(name = "RepeatEachInterval")
    private Integer repeatEachInterval = 1;
    
    /**
     * Список дней в которые будет запуск в формате Json
     */
    @Column(name = "DaysOfWeek")
    private String daysOfWeek;    

    public ProcTimer() {
    }
        
    /* gets & sets */

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
    }
        
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getRepeatType() {
        return repeatType;
    }
    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public Integer getRepeatTypeInterval() {
        return repeatTypeInterval;
    }
    public void setRepeatTypeInterval(Integer repeatTypeInterval) {
        this.repeatTypeInterval = repeatTypeInterval;
    }

    public Integer getRepeatEachType() {
        return repeatEachType;
    }
    public void setRepeatEachType(Integer repeatEachType) {
        this.repeatEachType = repeatEachType;
    }

    public Integer getRepeatEachInterval() {
        return repeatEachInterval;
    }
    public void setRepeatEachInterval(Integer repeatEachInterval) {
        this.repeatEachInterval = repeatEachInterval;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }
    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.id);
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
        final ProcTimer other = (ProcTimer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcTimer{" + "id=" + id + ", name=" + name + '}';
    }
        
}
