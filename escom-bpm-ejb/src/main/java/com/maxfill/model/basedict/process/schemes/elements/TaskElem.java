package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.utils.EscomUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность "Задача" 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskElem extends WFConnectedElem{
    private static final long serialVersionUID = -148365590532225862L;
    
    @XmlTransient
    private Task task;
    
    @XmlElement(name = "tasks")
    private List<Integer> tasksExec = new ArrayList<>(); //список id выполненных задач, пришедших в эту задачу
        
    @XmlElement(name = "consid")
    private Boolean considInProc = true;
    
    @XmlElement(name = "staffId")
    private Integer staffId;
    
    @XmlElement(name = "roleproc")
    private Integer roleInProc;
        
    @XmlElement(name = "dlType")
    private String deadLineType = "delta";
    
    @XmlElement(name = "name")
    private String name = "";
    
    @XmlElement(name = "deltaDL")
    private Integer deltaDeadLine = 0;
    
    @XmlElement(name = "remType")
    private String reminderType;
       
    @XmlElement(name = "remRType")
    private String reminderRepeatType = "no";
    
    @XmlElement(name = "remDelta")
    private Integer deltaReminder = 0;
      
    @XmlElement(name = "remTime")
    private Date reminderTime;
    
    @XmlElement(name = "remDay")
    private String reminderDays; 
    
    @XmlElement(name = "resultJSON")
    private String avaibleResultsJSON;
    
    public TaskElem() {
        this.uid = EscomUtils.generateGUID();
    }

    public TaskElem(String caption, String x, String y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getImage() {
        return null;
    }
    
    @Override
    public String getCaption() {
        if (task != null){
            StringBuilder sb = new StringBuilder();            
            Staff owner = task.getOwner();
            if (owner != null){
                /*
                if (owner.getPost() != null){
                    sb.append(owner.getPost().getName()).append(" ");
                }
                */
                if (owner.getEmployee() != null){
                    sb.append(owner.getEmployee().getShortFIO());
                }
                sb.append(": ").append(task.getNameEndElipse());
            } else {
                sb.append("???");
            }
            caption = sb.toString();
            return caption;
        } else {
            return super.getCaption(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    @Override
    public String getStyle() {
        StringBuilder sb = new StringBuilder(DictWorkflowElem.STYLE_TASK);
        if (task != null){
            sb.append(" ").append(task.getStyle());
        }
        return sb.toString();
    }
    
    /* GETS & SETS */

    public Integer getStaffId() {
        return staffId;
    }
    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public String getAvaibleResultsJSON() {
        return avaibleResultsJSON;
    }
    public void setAvaibleResultsJSON(String avaibleResultsJSON) {
        this.avaibleResultsJSON = avaibleResultsJSON;
    }
    
    public String getReminderDays() {
        return reminderDays;
    }
    public void setReminderDays(String reminderDays) {
        this.reminderDays = reminderDays;
    }
    
    public Date getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public Integer getDeltaReminder() {
        return deltaReminder;
    }
    public void setDeltaReminder(Integer deltaReminder) {
        this.deltaReminder = deltaReminder;
    }
    
    public String getReminderRepeatType() {
        return reminderRepeatType;
    }
    public void setReminderRepeatType(String reminderRepeatType) {
        this.reminderRepeatType = reminderRepeatType;
    }
    
    public Integer getDeltaDeadLine() {
        return deltaDeadLine;
    }
    public void setDeltaDeadLine(Integer deltaDeadLine) {
        this.deltaDeadLine = deltaDeadLine;
    }

    public String getReminderType() {
        return reminderType;
    }
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
        
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDeadLineType() {
        return deadLineType;
    }
    public void setDeadLineType(String deadLineType) {
        this.deadLineType = deadLineType;
    }

    public Integer getRoleInProc() {
        return roleInProc;
    }
    public void setRoleInProc(Integer roleInProc) {
        this.roleInProc = roleInProc;
    }   
    
    public Boolean getConsidInProc() {
        return considInProc;
    }
    public void setConsidInProc(Boolean considInProc) {
        this.considInProc = considInProc;
    }         

    public List<Integer> getTasksExec() {
        return tasksExec;
    }
    public void setTasksExec(List<Integer> tasksExec) {
        this.tasksExec = tasksExec;
    }
    
    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
        this.staffId = task.getOwner().getId();
    }
    
    @Override
    public String getBundleKey() {
        return "Task";
    }
    
    /* *** *** */
    
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TaskElem startElem = (TaskElem) o;

        return uid.equals(startElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "TaskElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
