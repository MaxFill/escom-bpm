package com.maxfill.model.task.result;

import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Справочник результатов выполнения задач
 * @author maksim
 */
@Entity
@Table(name = "results")
@DiscriminatorColumn(name = "REF_TYPE")
public class Result extends BaseDict<Result, Result, Result, ResultLog, ResultStates> {    
    private static final long serialVersionUID = -3383003884974574336L;
    
    @TableGenerator(
            name = "IdResultGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "RESULT_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "IdResultGen")
    @Column(name = "Id")
    private Integer id;    
        
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ResultStates state;

    /* *** GETS & SETS *** */
        
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }  
    
    @Override
    public ResultStates getState() {
        return state;
    }
    @Override
    public void setState(ResultStates state) {
        this.state = state;
    }
         
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final Result other = (Result) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Result{" + "id=" + id + '}';
    }    
    
}
