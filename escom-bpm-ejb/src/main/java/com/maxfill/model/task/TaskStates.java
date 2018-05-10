package com.maxfill.model.task;

import com.maxfill.model.states.BaseStateItem;

import javax.persistence.*;

import static javax.persistence.GenerationType.TABLE;

@Entity
@Table(name = "tasksStates")
@DiscriminatorColumn(name = "REF_TYPE")
public class TaskStates extends BaseStateItem{
    private static final long serialVersionUID = 9135272264608244516L;

    @TableGenerator(
            name = "TaskStatesGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "TaskStates_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "TaskStatesGen")
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

    /* *** *** */
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof TaskStates)) {
            return false;
        }
        TaskStates other = (TaskStates) object;
        if((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskStates[ id=" + id + " ]";
    }
}
