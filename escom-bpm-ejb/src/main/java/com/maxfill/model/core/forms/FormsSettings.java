package com.maxfill.model.core.forms;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Сущность для хранения дефолтных настроек форм
 */
@Entity
@Table(name = "formsSettings",
        indexes = {@Index(name="FormsDefSettings_INDEX", columnList = "FormName", unique = true)})
public class FormsSettings implements Serializable{    
    private static final long serialVersionUID = -3746491857350793872L;
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)
    @Size(max = 255)
    @Column(name = "Name")
    @NotNull
    private String name;
    
    @Basic(optional = false)    
    @Column(name = "Width")
    private Integer width;
    
    @Basic(optional = false)    
    @Column(name = "Height")
    private Integer height;

    public FormsSettings() {
    }

    public FormsSettings(String name, Integer width, Integer height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }
    
    /* GETS & SETS */
    
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

    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    /* *** *** */

    @Override
    public String toString() {
        return "FormsDefSettings{" + "id=" + id + ", formName=" + name + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FormsSettings other = (FormsSettings) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
