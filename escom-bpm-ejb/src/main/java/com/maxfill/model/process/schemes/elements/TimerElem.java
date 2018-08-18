package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность элемент "Таймер" схемы процесса 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TimerElem extends WFConnectedElem {    
    private static final long serialVersionUID = -1829446194973668643L;

    @XmlTransient
    private ProcTimer procTimer;
     
    @XmlElement(name = "timerId")
    private Integer timerId;
    
    public TimerElem() {
        this.uid = EscomUtils.generateGUID();
    }
    
    public TimerElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    /* GETS & SETS */

    public ProcTimer getProcTimer() {
        return procTimer;
    }
    public void setProcTimer(ProcTimer procTimer) {
        this.procTimer = procTimer;
    }
        
    public Integer getTimerId() {
        return timerId;
    }       
    public void setTimerId(Integer timerId) {
        this.timerId = timerId;
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_TIMER;
    }

    @Override
    public String getBundleKey() {
        return "timer";
    }

    @Override
    public String getImage() {
        return "timer-32";
    }
    
    /* *** *** */

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TimerElem elem = (TimerElem) o;

        return uid.equals(elem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "TimerElem{" +                
                " caption='" + caption + '\'' +
                '}';
    }    
    
}
