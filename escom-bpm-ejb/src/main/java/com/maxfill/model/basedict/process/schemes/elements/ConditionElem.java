package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сущность "Элемент схемы процесса "Условие"
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConditionElem extends WFConnectedElem{    
    private static final long serialVersionUID = 7842115174869991399L;

    @XmlElement(name = "conditon")
    private Integer conditonId;
    
    @XmlElement(name = "params")
    private Map<String, Object> params = new HashMap<>(); ;
    
    public ConditionElem() {
        this.uid = EscomUtils.generateGUID();
    }

    public ConditionElem(String caption, Integer conditonId, String x, String y) {
        this.caption = caption;
        this.conditonId = conditonId;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }
    
    @Override
    public String getStyle() {
        StringBuilder sb = new StringBuilder(DictWorkflowElem.STYLE_CONDITION);
        if (isDone()){
            sb.append(" ").append("finished");
        }
        return sb.toString();
    }

    @Override
    public String getBundleKey() {
        return "Condition";
    }   
    
    public Integer getConditonId() {
        return conditonId;
    }
    public void setConditonId(Integer conditonId) {
        this.conditonId = conditonId;
    }  

    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
        
    public Set<AnchorElem> getSecussAnchors(){
        return getAnchors().stream()
                .filter(a->DictWorkflowElem.STYLE_YES.equals(a.getStyle()))
                .collect(Collectors.toSet());
    }
    
    public Set<AnchorElem> getFailAnchors(){
        return getAnchors().stream()
                .filter(a->DictWorkflowElem.STYLE_NO.equals(a.getStyle()))
                .collect(Collectors.toSet());
    }
     
    @Override
    public String getImage() {
        return null;
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