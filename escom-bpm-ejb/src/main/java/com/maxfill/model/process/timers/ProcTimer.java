package com.maxfill.model.process.timers;

import com.maxfill.model.Dict;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
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

/**
 * Сущность для хранения настроек таймера
 * @author maksim
 */
@Entity
@Table(name = "processTimers")
public class ProcTimer implements Serializable, Dict {
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
    private Date startDate;
    
    /**
     * Тип запуска
     * on_init - при инициализации
     * on_plan - срок исполнения процесса
     * on_date - дата указывается принудительно
     */
    @Column(name = "StartType")    
    private String startType = "on_init";
    
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
     * Вариант повтора:
     * intime - в заданное время (repeatTime)
     * each   - каждую(ый) час, минуту, день (repeatEachType)
     */
    @Column(name = "RepeatVariantType")
    private String repeatVariantType = "intime";
    
    /**
     * Тип интервала повтора:
     * minute - минута
     * hour - час
     * day - день
     */
    @Column(name = "RepeatEachType")
    private String repeatEachType = "day";
    
    /**
     * Значение интервала повтора в секундах
     */
    @Column(name = "RepeatEachInterval")
    private Integer repeatEachInterval = 1;    
    
    /**
     * Список дней в которые будет запуск в формате Json
     */
    @Column(name = "DaysOfWeek")
    private String daysWeekRepeat;    

    @Column(name="RepeatDateTime")
    private Date repeatTime;  //время повтора 
    
    /* Ссылка на визуальный элемент схемы процесса */
    @Column(name = "TimerLinkUID")
    private String timerLinkUID;
    
    /**
     * Ссылка на схему процесса
     */
    @JoinColumn(name = "Scheme", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Scheme scheme;
    
    public ProcTimer() {
    }
        
    /* gets & sets */

    public Date getRepeatTime() {
        return repeatTime;
    }
    public void setRepeatTime(Date repeatTime) {
        this.repeatTime = repeatTime;
    }
    
    public Scheme getScheme() {
        return scheme;
    }
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }
    
    public String getStartType() {
        return startType;
    }
    public void setStartType(String startType) {
        this.startType = startType;
    }
    
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

    public String getRepeatVariantType() {
        return repeatVariantType;
    }
    public void setRepeatVariantType(String repeatVariantType) {
        this.repeatVariantType = repeatVariantType;
    }

    public String getRepeatEachType() {
        return repeatEachType;
    }
    public void setRepeatEachType(String repeatEachType) {
        this.repeatEachType = repeatEachType;
    }

    public Integer getRepeatEachInterval() {
        return repeatEachInterval;
    }
    public void setRepeatEachInterval(Integer repeatEachInterval) {
        this.repeatEachInterval = repeatEachInterval;
    }

    public String getDaysWeekRepeat() {
        return daysWeekRepeat;
    }
    public void setDaysWeekRepeat(String daysWeekRepeat) {
        this.daysWeekRepeat = daysWeekRepeat;
    }

    public String getTimerLinkUID() {
        return timerLinkUID;
    }
    public void setTimerLinkUID(String timerLinkUID) {
        this.timerLinkUID = timerLinkUID;
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
