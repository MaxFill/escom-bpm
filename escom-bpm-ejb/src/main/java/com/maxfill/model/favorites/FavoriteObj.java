
package com.maxfill.model.favorites;

import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

/**
 *
 * @author mfilatov
 */
@Entity
@Table(name = "favoriteObj")

public class FavoriteObj implements Serializable {
    private static final long serialVersionUID = -1429047840316849297L;

    @TableGenerator(
        name="FavoriteObjIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="FavorObjs_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="FavoriteObjIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "ObjId")
    private Integer objId;
    
    @JoinColumn(name = "MetadateId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Metadates metadateObj;
    
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User userId;

    public FavoriteObj() {
    }
    public FavoriteObj(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getObjId() {
        return objId;
    }
    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public Metadates getMetadateObj() {
        return metadateObj;
    }
    public void setMetadateObj(Metadates metadateObj) {
        this.metadateObj = metadateObj;
    }

    public User getUserId() {
        return userId;
    }
    public void setUserId(User userId) {
        this.userId = userId;
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
        if (!(object instanceof FavoriteObj)) {
            return false;
        }
        FavoriteObj other = (FavoriteObj) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.users.favorites.FavoriteObj[ id=" + id + " ]";
    }
    
}
