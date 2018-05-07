package com.maxfill.model.process.schemes.elements;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Абстрактный класс элементов графической модели процесса
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class WorkflowElement implements Serializable{
    private static final long serialVersionUID = 7711526969748122074L;
    
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
    public abstract String getBundleKey();
}
