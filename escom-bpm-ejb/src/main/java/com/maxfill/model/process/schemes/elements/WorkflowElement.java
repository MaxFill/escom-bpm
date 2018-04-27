package com.maxfill.model.process.schemes.elements;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Абстрактный класс элементов графической модели процесса
 */
@XmlTransient
public abstract class WorkflowElement implements Serializable{
    @XmlElement(name = "uid")
    protected String uid;

    @XmlElement(name = "caption")
    protected String caption;

    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public abstract String getStyle();
}
