
package com.maxfill.model.partners.types;

import com.maxfill.model.BaseDict;
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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/* Класс сущности для Типов контрагентов */
@Entity
@Table(name = "partnerTypes")
@DiscriminatorColumn(name="REF_TYPE")
public class PartnerTypes extends BaseDict<PartnerTypes, PartnerTypes, PartnerTypes, PartnerTypesLog, PartnerTypesStates> {
    private static final long serialVersionUID = 311428867470166273L;
    
    @TableGenerator(
        name="PartnerTypesIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="PartnerTypes_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="PartnerTypesIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Column(name = "FullName")
    @Size(max = 256)
    private String fullName;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<PartnerTypesLog> itemLogs = new ArrayList<>();
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private PartnerTypesStates state;
        
    public PartnerTypes() {
    }

    @Override
    public PartnerTypesStates getState() {
        return state;
    }
    @Override
    public void setState(PartnerTypesStates state) {
        this.state = state;
    }

    @Override
    public List<PartnerTypesLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<PartnerTypesLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    @Override
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
        if (!(object instanceof PartnerTypes)) {
            return false;
        }
        PartnerTypes other = (PartnerTypes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.partners.types.PartnerTypes[ id=" + id + " ]";
    }
    
}
