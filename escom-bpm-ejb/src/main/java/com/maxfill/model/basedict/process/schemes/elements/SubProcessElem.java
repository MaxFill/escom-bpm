package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.model.basedict.process.Process;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность "Элемент схемы процесса "Подпроцесс"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SubProcessElem extends WFConnectedElem{
    private static final long serialVersionUID = 5314328285810719081L;

    @XmlElement(name = "proctype")
    private Integer proctypeId;
    
    @XmlElement(name = "proctempl")
    private Integer proctemplId;
    
    @XmlElement(name = "showcard")
    private boolean showCard = true;
    
    @XmlTransient
    private Process subProcess;
    
    public SubProcessElem() {
        this.uid = EscomUtils.generateGUID();
    }
    
    public SubProcessElem(String caption, String x, String y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }    
    
    @Override
    public String getImage() {
        if (showCard){
            return "importance-20";
        }
        return "";
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_SUB_PROCESS; 
    }

    @Override
    public String getBundleKey() {
        return "SubProcess";
    }

    /* GETS & SETS */

    public boolean isShowCard() {
        return showCard;
    }
    public void setShowCard(boolean showCard) {
        this.showCard = showCard;
    }        

    public Integer getProctypeId() {
        return proctypeId;
    }
    public void setProctypeId(Integer proctypeId) {
        this.proctypeId = proctypeId;
    }

    public Integer getProctemplId() {
        return proctemplId;
    }
    public void setProctemplId(Integer proctemplId) {
        this.proctemplId = proctemplId;
    }

    public Process getSubProcess() {
        return subProcess;
    }
    public void setSubProcess(Process subProcess) {
        this.subProcess = subProcess;
    }
         
    /* *** *** */
    
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        SubProcessElem elem = (SubProcessElem) o;

        return uid.equals(elem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "SubProcessElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
