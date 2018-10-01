package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.utils.EscomUtils;
import java.util.ArrayList;
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
    
    /* GETS & SETS */

    public Integer getStaffId() {
        return staffId;
    }
    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public Boolean getConsidInProc() {
        return considInProc;
    }
    public void setConsidInProc(Boolean considInProc) {
        this.considInProc = considInProc;
    }
         
    @Override
    public String getCaption() {
        if (task != null){
            StringBuilder sb = new StringBuilder();
            Staff owner = task.getOwner();
            if (owner != null){
                if (owner.getPost() != null){
                    sb.append(owner.getPost().getName()).append(" ");
                }
                if (owner.getEmployee() != null){
                    sb.append(owner.getEmployee().getShortFIO());
                }
            } else {
                sb.append("< ? >");
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
