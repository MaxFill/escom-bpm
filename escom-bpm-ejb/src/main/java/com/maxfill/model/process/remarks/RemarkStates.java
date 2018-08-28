package com.maxfill.model.process.remarks;

import com.maxfill.model.states.BaseStateItem;
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
 * @author maksim
 */
@Entity
@Table(name = "remarksStates")
@DiscriminatorColumn(name="REF_TYPE")
public class RemarkStates extends BaseStateItem{    
    private static final long serialVersionUID = 8352494850160818608L;
    
    @TableGenerator(
        name="RemarkStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="RemarkStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="RemarkStatesGen")
    @Column(name = "Id")
    private Integer id;

    public RemarkStates() {
    }
    
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
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final RemarkStates other = (RemarkStates) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RemarkStates{" + "id=" + id + '}';
    }
    
}
