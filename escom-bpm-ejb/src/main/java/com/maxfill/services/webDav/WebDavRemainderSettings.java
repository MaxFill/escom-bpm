package com.maxfill.services.webDav;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class WebDavRemainderSettings implements Serializable{
    private static final long serialVersionUID = 610304938548569863L;
    
    @XmlElement(name = "ServerAdress")
    private Integer countRemaind; 

    public Integer getCountRemaind() {
        return countRemaind;
    }
    public void setCountRemaind(Integer countRemaind) {
        this.countRemaind = countRemaind;
    }    
    
}
