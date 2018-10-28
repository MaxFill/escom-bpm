package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Выход из процесса" - точка выхода
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExitElem extends WFConnectedElem{
    private static final long serialVersionUID = 8512962777531919513L;
    
    @XmlElement(name = "styleType")
    private String styleType = "completed";
     
    @XmlElement(name = "finalize")
    private Boolean finalize = true;
    
    @XmlElement(name = "stateId")
    private Integer finishStateId = DictStates.STATE_COMPLETED;
    
    @XmlElement(name = "status")
    private Integer statusId;
        
    public ExitElem() {
    }

    public ExitElem(String caption, Boolean finalize, String x, String y) {
        this.caption = caption;
        this.finalize = finalize;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {
        return "ui-diagram-" + styleType + "-exit";        
    }
    
    public Boolean getFinalize() {
        return finalize;
    }
    public void setFinalize(Boolean finalize) {
        this.finalize = finalize;
    }
    
    @Override
    public String getImage() {
        return null;
    }    

    @Override
    public String getBundleKey() {
        return "Exit";
    }

    public String getStyleType() {
        return styleType;
    }
    public void setStyleType(String styleType) {
        this.styleType = styleType;
    }
    
    public Integer getFinishStateId() {
        return finishStateId;
    }
    public void setFinishStateId(Integer finishStateId) {
        this.finishStateId = finishStateId;
    }    

    public Integer getStatusId() {
        return statusId;
    }
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }
        
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ExitElem exitElem = (ExitElem) o;

        return uid.equals(exitElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "ExitElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
