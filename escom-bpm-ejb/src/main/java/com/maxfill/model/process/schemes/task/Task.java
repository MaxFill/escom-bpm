package com.maxfill.model.process.schemes.task;

import com.maxfill.model.Dict;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.staffs.Staff;
import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

import static javax.persistence.GenerationType.TABLE;

/**
 * Сущность "Элемент схемы процесса "Поручение"
 */
@Entity
@Table(name = "tasks",
        indexes = {@Index(name="TaskLinkUID_INDEX", columnList = "TaskLinkUID", unique = true)})
public class Task implements Serializable, Dict{
    private static final long serialVersionUID = 2862379210656085637L;

    @TableGenerator(
            name = "TaskIdGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "TASK_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "TaskIdGen")
    private Integer id;

    /* Исполнитель, он же Владелец "Штатная единица" */
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff owner;

    /* Ссылка на схему процесса */
    @JoinColumn(name = "Scheme", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Scheme scheme;

    @Size(max = 255)
    @Column(name = "Name")
    private String name;

    /* Дата выдачи (назначения) поручения */
    @Column(name = "BeginDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginDate;

    /* Плановый срок исполнения */
    @Column(name = "PlanExecDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date planExecDate;

    /* Фактический срок исполнения */
    @Column(name = "FactExecDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date factExecDate;
    
    /* Ссылка на визуальный элемент схемы процесса */
    @Column(name = "TaskLinkUID")
    private String taskLinkUID;
    
    /* Категории */
    //ToDo добавить категории

    /* Состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private TaskStates state;

    /* Лог */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<TaskLog> itemLogs = new ArrayList<>();

    public Task() {
    }

    /* GETS & SETS */

    @Override
    public Integer getId() {
        return null;
    }
    @Override
    public void setId(Integer id) {

    }

    public String getTaskLinkUID() {
        return taskLinkUID;
    }
    public void setTaskLinkUID(String taskLinkUID) {
        this.taskLinkUID = taskLinkUID;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Staff getOwner() {
        return owner;
    }
    public void setOwner(Staff owner) {
        this.owner = owner;
    }

    public Date getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getPlanExecDate() {
        return planExecDate;
    }
    public void setPlanExecDate(Date planExecDate) {
        this.planExecDate = planExecDate;
    }

    public Date getFactExecDate() {
        return factExecDate;
    }
    public void setFactExecDate(Date factExecDate) {
        this.factExecDate = factExecDate;
    }

    public TaskStates getState() {
        return state;
    }
    public void setState(TaskStates state) {
        this.state = state;
    }

    public List <TaskLog> getItemLogs() {
        return itemLogs;
    }
    public void setItemLogs(List <TaskLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    public Scheme getScheme() {
        return scheme;
    }
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Task)) {
            return false;
        }
        Task other = (Task) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Task [ id=" + id + " ] [" + name + "]";
    }

}
