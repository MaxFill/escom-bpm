package com.maxfill.model.process.schemes.task;

import com.maxfill.dictionary.SysParams;
import com.maxfill.model.BaseDict;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.staffs.Staff;

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
public class Task extends BaseDict<Staff, Task, Task, TaskLog, TaskStates> implements SchemeElement{
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

    public Task(String taskName, Staff owner, Scheme scheme) {
        this.owner = owner;
        this.scheme = scheme;
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

    @Override
    public String getCaption(){
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(getName()).append(">").append(" ");
        if (owner != null){
            if (owner.getPost() != null){
                sb.append(owner.getPost().getName()).append(" ");
            }
            if (owner.getEmployee() != null){
                sb.append(owner.getEmployee().getShortFIO());
            }
        }
        return sb.toString();
    }
    @Override
    public void setCaption(String caption) {
    }

    @Override
    public Scheme getScheme() {
        return scheme;
    }
    @Override
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
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
