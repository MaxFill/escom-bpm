package com.maxfill.model.basedict.processType.roles;

import com.maxfill.model.Dict;
import com.maxfill.model.basedict.processType.ProcessType;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Роли вида процесса
 * @author maksim
 */
@Entity
@Table(name = "processRoles")
public class ProcessRole implements Serializable, Dict{
    private static final long serialVersionUID = 5076062464471540016L;

    @TableGenerator(
        name="RolesIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Roles_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="RolesIdGen")
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "Name")
    private String name;
     
    @Basic(optional = false)
    @Column(name = "Type")
    private String type = "singl"; //вид роли list/single
    
    @Basic(optional = false)
    @Column(name = "Sourse", length = 2048)
    private String sourceJson; //откуда получать значения для списка или конкретные значения
            
    @JoinColumn(name = "ProcessType", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ProcessType procType;
    
    public ProcessRole() {
    }    

    public ProcessRole(ProcessType procType) {
        this.procType = procType;
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

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }        

    public ProcessType getProcType() {
        return procType;
    }
    public void setProcType(ProcessType procType) {
        this.procType = procType;
    } 

    public String getSourceJson() {
        return sourceJson;
    }
    public void setSourceJson(String sourceJson) {
        this.sourceJson = sourceJson;
    }
        
    /* *** *** */

    @Override
    public String toString() {
        return "Role {" + "id=" + id + ", name=" + name + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.id);
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
        final ProcessRole other = (ProcessRole) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
        
}
