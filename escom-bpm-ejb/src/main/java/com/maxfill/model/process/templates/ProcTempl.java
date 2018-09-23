package com.maxfill.model.process.templates;

import com.maxfill.model.BaseDict;
import com.maxfill.model.process.types.ProcessType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность "Шаблон процесса" 
 */
@Entity
@Table(name = "processTemplates")
public class ProcTempl extends BaseDict<ProcessType, ProcTempl, ProcTempl, ProcTemplLog, ProcTemplStates> {
    private static final long serialVersionUID = 521389402353721255L;
    
    @TableGenerator(
        name = "procTemplatesIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "PROC_TEMPL_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "procTemplatesIdGen")
    private Integer id;
    
    @Column(name="IsDefault")
    private Boolean isDefault = false;
      
    @Column(name = "TermHours")
    private Integer termHours = 72;  //типовой срок согласования в часах
        
    @Lob
    @Column(name = "Elements", length = 9024)
    private byte[] elements;            
        
    /* Состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ProcTemplStates state;

    public ProcTempl() {
    }

    @Override
    public String getIconName() {
        if (isDefault){
            return "done";
        } else {
            return "blank-20";
        }
    }
    
    
    /* GETS & SETS */
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTermHours() {
        return termHours;
    }
    public void setTermHours(Integer termHours) {
        this.termHours = termHours;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public byte[] getElements() {
        return elements;
    }
    public void setElements(byte[] elements) {
        this.elements = elements;
    }
    
    @Override
    public ProcTemplStates getState() {
        return state;
    }
    @Override
    public void setState(ProcTemplStates state) {
        this.state = state;
    }  
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 5;
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
        final ProcTempl other = (ProcTempl) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcTempl{" + "id=" + id + '}';
    }
    
}
