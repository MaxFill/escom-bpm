package com.maxfill.model.process.types;

import com.maxfill.model.states.BaseStateItem;
import javax.persistence.*;
import static javax.persistence.GenerationType.TABLE;

@Entity
@Table(name = "processesTypesStates")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcessTypeStates extends BaseStateItem{
    private static final long serialVersionUID = 7459590071605154544L;

    @TableGenerator(
        name="ProcessesTypesStatesGen",
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="ProcessesTypesStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="ProcessesTypesStatesGen")
    @Column(name = "Id")
    private Integer id;

    public ProcessTypeStates() {
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

    /* *** *** */
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ProcessTypeStates)) {
            return false;
        }
        ProcessTypeStates other = (ProcessTypeStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcessesTypesStates[ id=" + id + " ]";
    }
}
