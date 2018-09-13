package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Процедура"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcedureElem extends WFConnectedElem{    
    private static final long serialVersionUID = 9080220938735475001L;

    @XmlElement(name = "procedure")
    private Integer procedureId;

    public ProcedureElem() {
        this.uid = EscomUtils.generateGUID();
    }
    
    public ProcedureElem(String caption, Integer procedureId, int x, int y) {
        this.caption = caption;
        this.procedureId = procedureId;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }
    
    /* GETS & SETS */
    
    @Override
    public String getImage() {
        return null;
    }

    @Override
    public String getStyle() {
        return  DictWorkflowElem.STYLE_PROCEDURE; 
    }

    @Override
    public String getBundleKey() {
        return "Procedure";
    }

    public Integer getProcedureId() {
        return procedureId;
    }
    public void setProcedureId(Integer procedureId) {
        this.procedureId = procedureId;
    }

    /* *** *** */
    
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ProcedureElem elem = (ProcedureElem) o;

        return uid.equals(elem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "ProcedureElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
