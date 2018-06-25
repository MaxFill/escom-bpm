package com.maxfill.model.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Статус документа"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusElem extends WFConnectedElem{
    private static final long serialVersionUID = 8873088059837269820L;

    @XmlElement(name = "styleType")
    private String styleType;

    @XmlElement(name = "status")
    private Integer docStatusId;
    
    public StatusElem() {
        this.uid = EscomUtils.generateGUID();
    }

    public StatusElem(String caption, Integer docStatusId, int x, int y) {
        this.caption = caption;
        this.docStatusId = docStatusId;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    /* Gets & Sets */

    public Integer getDocStatusId() {
        return docStatusId;
    }
    public void setDocStatusId(Integer docStatusId) {
        this.docStatusId = docStatusId;
    }
        
    @Override
    public String getStyle() {
        StringBuilder sb = new StringBuilder("ui-diagram-" + styleType + "-state");
        if (isDone()){
            sb.append(" ").append("finished");
        }
        return sb.toString();
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

        StatusElem stateElem = (StatusElem) o;

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