package com.maxfill.model.process.schemes.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс списка элементов графической модели процесса
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowElements implements Serializable{
    private static final long serialVersionUID = 5898399111315803093L;

    @XmlElement(name = "elements")
    protected List<WorkflowConnectedElement> elements = new ArrayList <>();

    public List <WorkflowConnectedElement> getElements() {
        return elements;
    }
    public void setElements(List <WorkflowConnectedElement> elements) {
        this.elements = elements;
    }
}
