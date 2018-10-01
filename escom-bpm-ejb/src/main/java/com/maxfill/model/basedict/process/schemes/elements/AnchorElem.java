package com.maxfill.model.basedict.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AnchorElem extends WFElement{
    private static final long serialVersionUID = 8128988436740920824L;

    @XmlElement(name = "position")
    private String position;

    @XmlElement(name = "type")
    private Boolean type;

    @XmlElement(name = "style")
    private String style;
    
    @XmlElement(name = "ownerUID")
    private String ownerUID;

    public AnchorElem() {
        this.uid = EscomUtils.generateGUID();
    }

    public AnchorElem(String caption, String position, Boolean type, String ownerUID) {
        this.caption = caption;
        this.position = position;
        this.type = type;
        this.ownerUID = ownerUID;        
        this.uid = EscomUtils.generateGUID();
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public String getOwnerUID() {
        return ownerUID;
    }
    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
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

    @Override
    public String getBundleKey() {
        return "Anchor";
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
