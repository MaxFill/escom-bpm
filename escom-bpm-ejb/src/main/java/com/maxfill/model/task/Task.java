package com.maxfill.model.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.BaseDict;
import com.maxfill.model.Results;
import com.maxfill.model.WithDatesPlans;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.result.Result;
import java.io.IOException;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static javax.persistence.GenerationType.TABLE;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

/**
 * Сущность "Элемент схемы процесса "Поручение"
 */
@Entity
@Table(name = "tasks"
    //, indexes = {@Index(name="TaskLinkUID_INDEX", columnList = "TaskLinkUID", unique = true)}
)
public class Task extends BaseDict<Staff, Task, Task, TaskLog, TaskStates> implements Results, WithDatesPlans{
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
    
    @Column(name="СonsidInProcReport")
    private Boolean considInProcReport = true; //включать в лист согласования
    
    @Column(name="DeltaDeadLine")
    private Integer deltaDeadLine = 0;      //срок исполнения в секундах
    
    @Column(name="DeadLineType")
    private String deadLineType = "delta";  //вид установки срока исполнения
    
    @Column(name="ReminderType")
    private String reminderType = "no";     //вид установки напоминания
        
    @Column(name="ReminderRepeatType")
    private String reminderRepeatType = "everyday"; //вид повтора напоминания
    
    @Column(name="DeltaReminder")
    private Integer deltaReminder = 0;  //срок напоминания в секундах до срока исполнения
    
    @Column(name="ReminderDateTime")
    private Date reminderTime;  //время напоминания 
    
    @Column(name="ReminderDays")
    private String reminderDays; //Дни напоминания
    
    @Column(name="NextReminder")
    private Date nextReminder;  //дата:время следующего напоминания 
    
    /* Ссылка на визуальный элемент схемы процесса */
    @Column(name = "TaskLinkUID")
    private String taskLinkUID;
    
    @Column(name = "Result")
    private String result;    
                
    @Column(name = "Comment")
    private String comment;
    
    @Column(name = "AvaibleResults")
    private String avaibleResultsJSON;    
    
    @Column(name = "RoleJson", length = 2048)
    private String roleJson;       
      
    @Size(max = 50)
    @Column(name = "RegNumber")
    private String regNumber;
        
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "task")
    private List<TaskReport> reports = new ArrayList<>();
        
    /* Категории */
    //ToDo добавить категории

    /* Состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private TaskStates state;

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
                if (!considInProcReport){
                    style = style + " notInList"; 
                }
                break;
            }
            case DictStates.STATE_DRAFT : {
                if (owner == null){
                    style = "noExecutor";
                } else {
                    style = "draft";
                }
                if (!considInProcReport){
                    style = style + " notInList"; 
                }
                break;
            }
            case DictStates.STATE_COMPLETED : {
                style = "finished";
                if (!considInProcReport){
                    style = style + " finishAndNotInList"; 
                }
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
    
    @Override
    public void setResults(List<Result> taskResults) {
        Gson gson = new Gson();
        String json = gson.toJson(taskResults.stream().map(r->r.getId()).collect(Collectors.toList()));
        avaibleResultsJSON = json;
    }

    @Override
    public String getIconName() {
        if (StringUtils.isNotEmpty(iconName)){
            return iconName; 
        } else {
            return "task";
        }
    }  

    public Boolean getConsidInProcReport() {
        return considInProcReport;
    }
    public void setConsidInProcReport(Boolean considInProcReport) {
        this.considInProcReport = considInProcReport;
    }
    
    @Override
    public String getRegNumber() {
        return regNumber;
    }
    @Override
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }        
    
    @Override
    public Map<String, Set<Integer>> getRoles() {
        if (roles == null){
            roles = new HashMap<>();
            if (StringUtils.isBlank(getRoleJson())) return roles;
            try {
                ObjectMapper mapper = new ObjectMapper();            
                roles = mapper.readValue(roleJson, new TypeReference<HashMap<String, HashSet<Integer>>>() {});
                setRoles(roles);
            } catch (IOException ex) {
                Logger.getLogger(com.maxfill.model.process.Process.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return roles;
    }
    
    /* GETS & SETS */

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {

    }

    @Override
    public String getRoleJson() {
        return roleJson;
    }
    @Override
    public void setRoleJson(String roleJson) {
        this.roleJson = roleJson;
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
    public TaskStates getState() {
        return state;
    }
    @Override
    public void setState(TaskStates state) {
        this.state = state;
    }
    
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getTaskLinkUID() {
        return taskLinkUID;
    }
    public void setTaskLinkUID(String taskLinkUID) {
        this.taskLinkUID = taskLinkUID;
    }
    
    public List<TaskReport> getReports() {
        return reports;
    }
    public void setReports(List<TaskReport> reports) {
        this.reports = reports;
    }
    
    @Override
    public Date getBeginDate() {
        return beginDate;
    }
    @Override
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public Date getPlanExecDate() {
        return planExecDate;
    }
    @Override
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
    
    @Override
    public Date getFactExecDate() {
        return factExecDate;
    }
    @Override
    public void setFactExecDate(Date factExecDate) {
        this.factExecDate = factExecDate;
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

    @Override
    public String getAvaibleResultsJSON() {
        return avaibleResultsJSON;
    }
    @Override
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

    public Date getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getReminderDays() {
        return reminderDays;
    }
    public void setReminderDays(String reminderDays) {
        this.reminderDays = reminderDays;
    }

    public Date getNextReminder() {
        return nextReminder;
    }
    public void setNextReminder(Date nextReminder) {
        this.nextReminder = nextReminder;
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
        return "Task [ id=" + id + " ]";
    }

}
