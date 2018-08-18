package com.maxfill.model.process.schemes.elements;

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
    
    @XmlElement(name = "finalize")
    private Boolean finalize = true;
    
    public ExitElem() {
    }

    public ExitElem(String caption, Boolean finalize, int x, int y) {
        this.caption = caption;
        this.finalize = finalize;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
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
    public String getStyle() {
        return finalize ? DictWorkflowElem.STYLE_FINISH : DictWorkflowElem.STYLE_EXIT;        
    }

    @Override
    public String getBundleKey() {
        return "Exit";
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
