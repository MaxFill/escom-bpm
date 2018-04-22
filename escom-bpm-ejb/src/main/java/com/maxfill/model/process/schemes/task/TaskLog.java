package com.maxfill.model.process.schemes.task;

import com.maxfill.model.BaseLogItems;
import com.maxfill.model.process.Process;

import javax.persistence.*;

@Entity
@Table(name = "taskLog")
@DiscriminatorColumn(name="REF_TYPE")
public class TaskLog extends BaseLogItems<Task>{
    private static final long serialVersionUID = -9197466421481526458L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    public TaskLog() {
    }

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
        if (!(object instanceof TaskLog)) {
            return false;
        }
        TaskLog other = (TaskLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskLog[ id=" + id + " ]";
    }
    
}
