package com.maxfill.model.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Состояние"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StateElem extends WFConnectedElem{
    private static final long serialVersionUID = 8873088059837269820L;

    @XmlElement(name = "styleType")
    private String styleType;

    @XmlElement(name = "state")
    private Integer stateId;
    
    public StateElem() {
    }

    public StateElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    /* Gets & Sets */

    public Integer getStateId() {
        return stateId;
    }
    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }
        
    @Override
    public String getStyle() {
        return "ui-diagram-" + styleType + "-state";
    }

    public void setStyleType(String styleType) {
        this.styleType = styleType;
    }
    public String getStyleType() {
        return styleType;
    }

    @Override
    public String getBundleKey() {
        return "State";
    }
    
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        StateElem stateElem = (StateElem) o;

        return uid.equals(stateElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "StateElem{" +
                "styleType='" + styleType + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}