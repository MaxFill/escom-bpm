package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Цикл" 
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LoopElem extends WFConnectedElem{
    private static final long serialVersionUID = 6677608454096489898L;

    public LoopElem() {
    }

    public LoopElem(String x, String y) {
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {        
        return DictWorkflowElem.STYLE_ENTER;
    }

    @Override
    public String getBundleKey() {
        return "Loop";
    }
    
    @Override
    public String getImage() {
        return "refresh-32";
    }
    
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        LoopElem exitElem = (LoopElem) o;

        return uid.equals(exitElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "LoopElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
