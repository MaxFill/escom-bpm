package com.maxfill.model.partners.groups;

import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.Partner;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;


/**
 * Группа контрагента
 * @author mfilatov
 */
@Entity
@Table(name = "partnersGroups")
@DiscriminatorColumn(name="REF_TYPE")
public class PartnerGroups extends BaseDict<PartnerGroups, PartnerGroups, Partner, PartnerGroupsLog>{
    private static final long serialVersionUID = 9082567805735998647L;

    @TableGenerator(
        name="PartnersGroupsIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="PARTNER_GROUP_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="PartnersGroupsIdGen")
    @Column(name = "Id")
    private Integer id;

    private String code;
        
    @ManyToMany(mappedBy = "partnersGroupsList", fetch = FetchType.EAGER)
    private List<Partner> partnersList = new ArrayList<>();
        
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<PartnerGroupsLog> itemLogs = new ArrayList<>();
    
    public PartnerGroups() {
    }

    public List<Partner> getPartnersList() {
        return partnersList;
    }
    public void setPartnersList(List<Partner> partnersList) {
        this.partnersList = partnersList;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public String getIconName() {
        iconName = "folder_open";
        return iconName;
    }
    
    @Override
    public List<PartnerGroupsLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<PartnerGroupsLog> itemLogs) {
        this.itemLogs = itemLogs;
    }
    
    @Override
    public List<Partner> getDetailItems() {
        return partnersList;
    }    
    @Override
    public void setDetailItems(List<Partner> detailItems) {
        this.partnersList = detailItems;
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
        if (!(object instanceof PartnerGroups)) {
            return false;
        }
        PartnerGroups other = (PartnerGroups) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.partners.groups.PartnersGroups[ id=" + id + " ]";
    }
 
}
