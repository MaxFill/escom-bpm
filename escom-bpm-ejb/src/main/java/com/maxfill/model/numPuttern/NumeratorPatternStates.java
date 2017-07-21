package com.maxfill.model.numPuttern;

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
@Table(name = "numeratorPatternStates")
@DiscriminatorColumn(name="REF_TYPE")
public class NumeratorPatternStates extends BaseStateItem{
    private static final long serialVersionUID = 1163256380071750081L;
 
    @TableGenerator(
        name="NumeratorPatternStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="NumeratorPatternStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="NumeratorPatternStatesGen")
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
        if (!(object instanceof NumeratorPatternStates)) {
            return false;
        }
        NumeratorPatternStates other = (NumeratorPatternStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NumeratorPatternStates[ id=" + id + " ]";
    }
}
