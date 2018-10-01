package com.maxfill.model.basedict.userGroups;

import com.maxfill.model.core.states.BaseStateItem;
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
@Table(name = "usersGroupsStates")
@DiscriminatorColumn(name="REF_TYPE")
public class UserGroupsStates extends BaseStateItem{
    private static final long serialVersionUID = 5658247351356830580L;    
 
    @TableGenerator(
        name="UserGroupsStatesGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="UserGroupStateID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="UserGroupsStatesGen")
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
        if (!(object instanceof UserGroupsStates)) {
            return false;
        }
        UserGroupsStates other = (UserGroupsStates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserGroupsStates[ id=" + id + " ]";
    }
}
