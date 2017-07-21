package com.maxfill.model.statuses;

import com.maxfill.model.states.BaseStateItem;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "statusesDocStates")
@DiscriminatorColumn(name="REF_TYPE")
public class StatusesDocStates extends BaseStateItem{
    private static final long serialVersionUID = -2801806776330065815L;
 
    @TableGenerator(
        name="StatusesDocStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="StatusesDocStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="StatusesDocStatesGen")
    @Column(name = "Id")
    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StatusesDocStates)) {
            return false;
        }
        StatusesDocStates other = (StatusesDocStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StatusesDocStates[ id=" + id + " ]";
    }
}
