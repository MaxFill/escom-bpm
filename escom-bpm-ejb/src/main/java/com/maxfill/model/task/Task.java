package com.maxfill.model.task;

import com.maxfill.model.BaseDict;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.users.User;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.TABLE;

/**
 * Сущность "Поручение"
 */
@Entity
@Table(name = "tasks")
@DiscriminatorColumn(name = "REF_TYPE")
public class Task extends BaseDict<Staff, Task, Task, TaskLog, TaskStates>{
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

    /* Ссылка на процесс */
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;

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

    public Task() {}

    public Task(String taskName, Staff owner, Process process) {
        this.owner = owner;
        this.process = process;
        setName(taskName);
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

    @Override
    public Staff getOwner() {
        return owner;
    }
    @Override
    public void setOwner(Staff owner) {
        this.owner = owner;
    }

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
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

    @Override
    public TaskStates getState() {
        return state;
    }
    @Override
    public void setState(TaskStates state) {
        this.state = state;
    }

    @Override
    public List <TaskLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List <TaskLog> itemLogs) {
        this.itemLogs = itemLogs;
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
        return "Task [ id=" + id + " ] [" + getName() + "]";
    }
}
