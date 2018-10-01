package com.maxfill.model.basedict.process;

import com.maxfill.model.BaseLogItems;
import com.maxfill.model.basedict.post.Post;

import javax.persistence.*;

@Entity
@Table(name = "processesLog")
@DiscriminatorColumn(name="REF_TYPE")
public class ProcessLog extends BaseLogItems<Process>{
    private static final long serialVersionUID = 9039077067399921810L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    public ProcessLog() {
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
        if (!(object instanceof ProcessLog)) {
            return false;
        }
        ProcessLog other = (ProcessLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcessLog[ id=" + id + " ]";
    }
    
}
