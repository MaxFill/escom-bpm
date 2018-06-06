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
public class EnterElem extends WFConnectedElem{
    private static final long serialVersionUID = 6677608454096489898L;


    public EnterElem() {
    }

    public EnterElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {
        StringBuilder sb = new StringBuilder(DictWorkflowElem.STYLE_ENTER);
        if (isDone()){
            sb.append(" ").append("finished");
        }
        return sb.toString();
    }

    @Override
    public String getBundleKey() {
        return "Enter";
    }
    
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        EnterElem exitElem = (EnterElem) o;

        return uid.equals(exitElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "EnterElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
