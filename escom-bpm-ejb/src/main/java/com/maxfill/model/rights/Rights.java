package com.maxfill.model.rights;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Права доступа к объекту
 * @author Maxim
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Rights implements Serializable{
    private static final long serialVersionUID = -5302893955331116353L;
    
    @XmlElement(name = "Rights")
    private List<Right> rights = new ArrayList<>();
    
    public Rights() {
    }

    public Rights(List<Right> rights) {
        this.rights = rights;
    }

    public List<Right> getRights() {
        return rights;
    }
    public void setRights(List<Right> rights) {
        this.rights = rights;
    }

    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
