package com.maxfill.model.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Коннектор"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConnectorElem extends WFElement{
    private static final long serialVersionUID = -2062835627519052276L;

    @XmlElement
    private AnchorElem from;

    @XmlElement
    private AnchorElem to;

    public ConnectorElem() {
    }

    public ConnectorElem(String caption, AnchorElem from, AnchorElem to) {
        this.caption = caption;
        this.from = from;
        this.to = to;
        this.uid = EscomUtils.generateGUID();
    }

    public AnchorElem getFrom() {
        return from;
    }
    public void setFrom(AnchorElem from) {
        this.from = from;
    }

    public AnchorElem getTo() {
        return to;
    }
    public void setTo(AnchorElem to) {
        this.to = to;
    }

    @Override
    public String getStyle() {
        return "";
    }

    @Override
    public String getBundleKey() {
        return "Сonnector";
    }
    
    /* *** *** */

    @Override
    public String toString() {
        return "ConnectorElem{" +
                "caption='" + caption + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ConnectorElem that = (ConnectorElem) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
