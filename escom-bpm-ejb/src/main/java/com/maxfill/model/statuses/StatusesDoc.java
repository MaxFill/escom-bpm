
package com.maxfill.model.statuses;

import com.maxfill.model.BaseDict;
import com.maxfill.utils.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/* Справочник статусов для документов */
@Entity
@Table(name = "statusesDoc")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
@DiscriminatorColumn(name="REF_TYPE")
public class StatusesDoc extends BaseDict<StatusesDoc, StatusesDoc, StatusesDoc, StatusesDocLog> {
    private static final long serialVersionUID = -8577581379121348639L;

    @TableGenerator(
        name="StatusDocIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="StatusDoc_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="StatusDocIdGen")
    @Column(name = "ID")
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<StatusesDocLog> itemLogs = new ArrayList<>();            
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public List<StatusesDocLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<StatusesDocLog> itemLogs) {
        this.itemLogs = itemLogs;
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
        if (!(object instanceof StatusesDoc)) {
            return false;
        }
        StatusesDoc other = (StatusesDoc) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.docs.docsStatus.StatusDoc[ id=" + id + " ]";
    }
    
}
