package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.model.task.Task;
import com.maxfill.model.staffs.Staff;
import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
    
    private Integer staffId;
    
    public TaskElem() {
        this.uid = EscomUtils.generateGUID();
    }

    public TaskElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    /* GETS & SETS */

    public Integer getStaffId() {
        return staffId;
    }
    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }
        
    @Override
    public String getCaption() {
        if (task != null){
            StringBuilder sb = new StringBuilder();
            sb.append("<").append(task.getName()).append(">").append(" ");
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
