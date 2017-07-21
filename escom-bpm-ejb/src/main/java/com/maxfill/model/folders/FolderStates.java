package com.maxfill.model.folders;

import com.maxfill.model.states.BaseStateItem;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "foldersStates")
@DiscriminatorColumn(name="REF_STATE")
public class FolderStates extends BaseStateItem{
    private static final long serialVersionUID = 1914065061508659519L;

    @TableGenerator(
        name="FolderStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="FolderStates_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="FolderStatesGen")
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
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FolderStates)) {
            return false;
        }
        FolderStates other = (FolderStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FolderStates[ id=" + id + " ]";
    }
}
