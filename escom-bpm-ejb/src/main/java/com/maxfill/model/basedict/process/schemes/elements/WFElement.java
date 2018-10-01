package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.utils.EscomUtils;
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
public abstract class WFElement implements Serializable{
    private static final long serialVersionUID = 7711526969748122074L;
    
    @XmlElement(name = "uid")
    protected String uid;

    @XmlElement(name = "caption")
    protected String caption;

    @XmlElement
    private boolean done;

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

    /**
     * Признак того, что маршрут прошёл через элемент
     * @return 
     */
    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }
    
    public abstract String getStyle();
    public abstract String getBundleKey();
}
