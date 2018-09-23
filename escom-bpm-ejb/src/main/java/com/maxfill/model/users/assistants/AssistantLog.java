package com.maxfill.model.users.assistants;

import com.maxfill.model.BaseLogItems;
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
 * Лог сущности Заместитель
 * @author maksim
 */
@Entity
@Table(name = "assistantLog")
@DiscriminatorColumn(name="REF_TYPE")
public class AssistantLog extends BaseLogItems<Assistant>{    
    private static final long serialVersionUID = 1779142906634099394L;
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    public AssistantLog() {
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
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final AssistantLog other = (AssistantLog) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AssistantLog{" + "id=" + id + '}';
    }
        
}
