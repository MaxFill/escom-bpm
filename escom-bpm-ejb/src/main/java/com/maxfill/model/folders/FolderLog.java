
package com.maxfill.model.folders;

import com.maxfill.model.BaseLogTable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author mfilatov
 */
@Entity
@Table(name = "foldersLog")
@DiscriminatorColumn(name="REF_TYPE")
public class FolderLog extends BaseLogTable<Folder>{
    private static final long serialVersionUID = 3958438152438492100L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;    
           
    public FolderLog() {
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolderLog)) {
            return false;
        }
        FolderLog other = (FolderLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.partners.PartnersLog[ id=" + id + " ]";
    }
    
}
