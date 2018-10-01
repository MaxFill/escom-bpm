package com.maxfill.model.basedict.process;

import com.maxfill.model.core.states.BaseStateItem;

import javax.persistence.*;

import static javax.persistence.GenerationType.TABLE;

@Entity
@Table(name = "processStates")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcessStates extends BaseStateItem{
    private static final long serialVersionUID = 315967338389124224L;

    @TableGenerator(
        name="ProcessStatesGen",
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="ProcessStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="ProcessStatesGen")
    @Column(name = "Id")
    private Integer id;

    public ProcessStates() {
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
        if (!(object instanceof ProcessStates)) {
            return false;
        }
        ProcessStates other = (ProcessStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcessStates[ id=" + id + " ]";
    }
}
