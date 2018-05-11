package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Вход в процесс" - точка входа
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StartElem extends WFConnectedElem{    
    private static final long serialVersionUID = -7870980216646008269L;

    public StartElem() {
    }

    public StartElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_START;
    }

    @Override
    public String getBundleKey() {
        return "enter";
    }
    
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        StartElem startElem = (StartElem) o;

        return uid.equals(startElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "StartElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
