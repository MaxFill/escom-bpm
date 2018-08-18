package com.maxfill.model.users.assistants;

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
@Table(name = "assistantStates")
@DiscriminatorColumn(name="REF_TYPE")
public class AssistantStates extends BaseStateItem {    
    private static final long serialVersionUID = -7077162949042660049L;
    
    @TableGenerator(
        name="AssistantStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="AssistantStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="AssistantStatesGen")
    @Column(name = "Id")
    private Integer id; 

    public AssistantStates() {
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
        hash = 59 * hash + Objects.hashCode(this.id);
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
        final AssistantStates other = (AssistantStates) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AssistantStates{" + "id=" + id + '}';
    }
        
}
