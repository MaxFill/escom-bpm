package com.maxfill.model.states;

import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.ItemUtils;

import java.io.Serializable;
import javax.persistence.*;

import static javax.persistence.GenerationType.TABLE;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;

/**
 * Сущность "Состояние объекта"
 */
@Entity
@Table(name = "states")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class State implements Serializable{    
    private static final long serialVersionUID = 311429207470166273L;       

    @TableGenerator(
        name="stateIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="STATE_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="stateIdGen")
    @Column(name = "ID")
    @XmlElement(name = "ID")
    private Integer id;        

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Name")
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsActual")
    private boolean isActual;
    
    @Basic(optional = false)
    @NotNull    
    @Column(name = "Icon", length = 1024)
    private String icon;

    public State() {
    }    

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsActual() {
        return isActual;
    }
    public void setIsActual(boolean isActual) {
        this.isActual = isActual;
    }

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof State)) {
            return false;
        }
        State other = (State) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "State[ id=" + id + " ]";
    }

    
}
