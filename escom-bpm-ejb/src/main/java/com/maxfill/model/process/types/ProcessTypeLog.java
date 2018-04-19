package com.maxfill.model.process.types;

import com.maxfill.model.BaseLogItems;
import com.maxfill.model.posts.Post;

import javax.persistence.*;

@Entity
@Table(name = "processesTypesLog")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcessTypeLog extends BaseLogItems<ProcessType>{
    private static final long serialVersionUID = 7709134449437875808L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    public ProcessTypeLog() {
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
        if (!(object instanceof ProcessTypeLog)) {
            return false;
        }
        ProcessTypeLog other = (ProcessTypeLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcessesTypesLog[ id=" + id + " ]";
    }
    
}
