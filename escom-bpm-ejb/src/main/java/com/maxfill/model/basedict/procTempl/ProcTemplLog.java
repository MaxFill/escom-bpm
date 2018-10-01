package com.maxfill.model.basedict.procTempl;

import com.maxfill.model.BaseLogItems;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author maksim
 */
@Entity
@Table(name = "processesTemplLog")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcTemplLog extends BaseLogItems<ProcTempl>{    
    private static final long serialVersionUID = -5896149131688543061L;
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    public ProcTemplLog() {
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
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.id);
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
        final ProcTemplLog other = (ProcTemplLog) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcTemplLog{" + "id=" + id + '}';
    }
        
}
