package com.maxfill.model.task;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.Dict;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.result.Result;
import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.TABLE;

/**
 * Сущность "Элемент схемы процесса "Поручение"
 */
@Entity
@Table(name = "tasks",
        indexes = {@Index(name="TaskLinkUID_INDEX", columnList = "TaskLinkUID", unique = true)})
public class Task implements Serializable, Dict{
    private static final long serialVersionUID = 2862379210656085637L;
    private static final AtomicInteger COUNT = new AtomicInteger(0);

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
    
    @Column(name="DeltaDeadLine")
    private Integer deltaDeadLine = 0; //срок исполнения в секундах
    
    @Column(name="DeadLineType")
    private String deadLineType = "delta"; //вид установки срока исполнения
    
    @Column(name="ReminderType")
    private String reminderType = "singl"; //вид установки напоминания
        
    @Column(name="ReminderRepeatType")
    private String reminderRepeatType = "everyday"; //вид повтора напоминания
    
    @Column(name="DeltaReminder")
    private Integer deltaReminder = 0;  //срок напоминания в секундах до срока исполнения
    
    /* Ссылка на визуальный элемент схемы процесса */
    @Column(name = "TaskLinkUID")
    private String taskLinkUID;
    
    @Column(name = "Result")
    private String result;
    
    @Column(name = "Icon")
    private String iconName;
                
    @Column(name = "Comment")
    private String comment;
    
    @Column(name = "AvaibleResults")
    private String avaibleResultsJSON;    
    
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

    @Transient
    @XmlTransient
    private final Integer tempId;
     
    public Task() {
        tempId = COUNT.incrementAndGet();
    }

    public String getStyle(){
        if (state == null) return "";
        String style;
        switch (state.getCurrentState().getId()){
            case DictStates.STATE_RUNNING : {
                style = "running";
                break;
            }
            case DictStates.STATE_DRAFT : {
                style = "draft";
                break;
            }
            case DictStates.STATE_COMPLETED : {
                style = "finished";
                break;
            }
            case DictStates.STATE_CANCELLED : {
                style = "cancelled";
                break;
            }
            default:{
                style = "";
            }
        }
        return style;
    }
        
    public boolean isRunning(){
        return DictStates.STATE_RUNNING == state.getCurrentState().getId();
    }   
    public boolean isCompleted(){
        return DictStates.STATE_COMPLETED == state.getCurrentState().getId();
    }
    
    public void setTaskResults(List<Result> taskResults) {
        Gson gson = new Gson();
        String json = gson.toJson(taskResults.stream().map(r->r.getId()).collect(Collectors.toList()));
        avaibleResultsJSON = json;
    }
    
    /* GETS & SETS */

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {

    }

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }

    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
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

    public Integer getDeltaDeadLine() {
        return deltaDeadLine;
    }
    public void setDeltaDeadLine(Integer deltaDeadLine) {
        this.deltaDeadLine = deltaDeadLine;
    }

    public String getDeadLineType() {
        return deadLineType;
    }
    public void setDeadLineType(String deadLineType) {
        this.deadLineType = deadLineType;
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

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAvaibleResultsJSON() {
        return avaibleResultsJSON;
    }
    public void setAvaibleResultsJSON(String avaibleResultsJSON) {
        this.avaibleResultsJSON = avaibleResultsJSON;
    }        

    public String getReminderType() {
        return reminderType;
    }
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getReminderRepeatType() {
        return reminderRepeatType;
    }
    public void setReminderRepeatType(String reminderRepeatType) {
        this.reminderRepeatType = reminderRepeatType;
    }

    public Integer getDeltaReminder() {
        return deltaReminder;
    }
    public void setDeltaReminder(Integer deltaReminder) {
        this.deltaReminder = deltaReminder;
    }

     
    public Integer getTempId() {
        return tempId;
    }
        
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.id);
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
        final Task other = (Task) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Task [ id=" + id + " ] [" + name + "]";
    }

}
