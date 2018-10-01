package com.maxfill.model.basedict.procTempl;

import com.maxfill.model.core.states.BaseStateItem;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 *
 */
@Entity
@Table(name = "processesTemplStates")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcTemplStates extends BaseStateItem{    
    private static final long serialVersionUID = -1721988201182663097L;
    
    @TableGenerator(
        name="processesTemplStatesGen",
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="processesTemplState_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="processesTemplStatesGen")
    @Column(name = "Id")
    private Integer id;
    
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
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.id);
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
        final ProcTemplStates other = (ProcTemplStates) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcTemplStates{" + "id=" + id + '}';
    }
        
}
