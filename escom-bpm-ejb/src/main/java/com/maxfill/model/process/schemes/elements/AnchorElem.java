package com.maxfill.model.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AnchorElem extends WorkflowElement{
    private static final long serialVersionUID = 8128988436740920824L;

    public static final String STYLE_YES = "{fillStyle:'#099b05'}";
    public static final String STYLE_NO = "{fillStyle:'#C33730'}";
    public static final String STYLE_MAIN = "{fillStyle:'#98AFC7'}";

    @XmlElement(name = "position")
    private String position;

    @XmlElement(name = "type")
    private Boolean type;

    @XmlElement(name = "style")
    private String style;

    @XmlTransient
    private WorkflowConnectedElement owner;

    public AnchorElem() {
    }

    public AnchorElem(String caption, String position, Boolean type, WorkflowConnectedElement owner) {
        this.caption = caption;
        this.position = position;
        this.type = type;
        this.owner = owner;
        this.uid = EscomUtils.generateGUID();
    }

    public WorkflowConnectedElement getOwner() {
        return owner;
    }
    public void setOwner(WorkflowConnectedElement owner) {
        this.owner = owner;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getType() {
        return type;
    }
    public void setType(Boolean type) {
        this.type = type;
    }

    public Boolean isSource(){
        return type;
    }

    public Boolean isTarget(){
        return !type;
    }

    @Override
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }

    /* *** *** */

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        AnchorElem that = (AnchorElem) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "AnchorElem{" +
                "position='" + position + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
