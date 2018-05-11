package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Условие"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConditionElem extends WFConnectedElem{    
    private static final long serialVersionUID = 7842115174869991399L;

    public ConditionElem() {
    }

    public ConditionElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_CONDITION;
    }

    @Override
    public String getBundleKey() {
        return "Condition";
    }
    
    /* *** *** */

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ConditionElem that = (ConditionElem) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "ConditionElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
