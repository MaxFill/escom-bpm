package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * Сущность "Элемент схемы процесса "Логическое ветвление"
 */
public class LogicElem extends WFConnectedElem{
    private static final long serialVersionUID = 1857271531554809843L;

    public LogicElem() {
    }

    public LogicElem(String caption, String x, String y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @XmlElement(name = "tasks")
    private List<Integer> tasksExec = new ArrayList<>(); //список id выполненных задач, пришедших в этот элемент логики
     
    @Override
    public String getImage() {
        return null;
    }
    
    @Override
    public String getStyle() {
        StringBuilder sb = new StringBuilder(DictWorkflowElem.STYLE_LOGIC);
        if (isDone()){
            sb.append(" ").append("finished");
        }
        return sb.toString();
    }

    @Override
    public String getBundleKey() {
        return "Logic";
    }

    public List<Integer> getTasksExec() {
        return tasksExec;
    }
    public void setTasksExec(List<Integer> tasksExec) {
        this.tasksExec = tasksExec;
    }
        
    /* *** *** */
    
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        LogicElem logicElem = (LogicElem) o;

        return uid.equals(logicElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "LogicElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}