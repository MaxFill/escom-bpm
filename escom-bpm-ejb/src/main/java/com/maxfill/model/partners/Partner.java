package com.maxfill.model.partners;

import com.maxfill.model.partners.contacts.PartnersContacts;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.utils.SysParams;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;

/* Контрагенты */
@Entity
@Table(name = "partners")
@DiscriminatorColumn(name="REF_TYPE")
public class Partner extends BaseDict<PartnerGroups, Partner, Partner, PartnersLog>{     
    private static final long serialVersionUID = 9082567804115998647L;
        
    @TableGenerator(
        name="PartnerIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="PARTNER_ID", allocationSize = 1)
          
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="PartnerIdGen")
    @Column(name = "ID")
    private Integer id;
    
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private PartnerGroups owner;
        
    @JoinColumn(name = "Type", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private PartnerTypes type;
    
    @Column(name = "Code")
    private String code;
    
    @Basic(optional = false)
    @Column(name = "FullName")
    @Size(max = 512)
    private String fullName;     
    
    /**
     * E-mail
     */
    @Column(name = "Email")
    @Size(max = 256)
    private String email;    
    
    @JoinTable(name = "partnersInGroup", joinColumns = {
        @JoinColumn(name = "PartnerId", referencedColumnName = "ID")}, inverseJoinColumns = {
        @JoinColumn(name = "GroupId", referencedColumnName = "Id")})
    @ManyToMany
    private List<PartnerGroups> partnersGroupsList = new ArrayList<>();
    
    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "partner")
    private List<Doc> docsList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "partner")
    private List<PartnersContacts> partnersContactsList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<PartnersLog> itemLogs = new ArrayList<>();
    
    @Transient
    @XmlTransient
    private Integer templRegNumber; //номер регистрационный, сохраняется только на время регистрации для отката
        
    public Partner() {
    }

    @Override
    public List<PartnersLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<PartnersLog> itemLogs) {
        this.itemLogs = itemLogs;
    }
    
    /**
     * Формирует наименование Контрагента ограниченной длинны, заканчивающуюся точками для отображения в таблицах
     * @return 
     */
    @Override
    public String getNameEndElipse(){
        StringBuilder builder = new StringBuilder();        
        builder.append(StringUtils.abbreviate(getName(), SysParams.LENGHT_NAME_ELIPSE));
        if (type != null){
           builder.append(" ").append(type.getName());
        }
        return builder.toString();
    }
    
    /**
     * Формирует титульное название организации
     * @return 
     */
    public String getTitleName(){
        StringBuilder builder = new StringBuilder();        
        if (type != null){
           builder.append(type.getName()).append(" ");
        }
        if (StringUtils.isNotBlank(getName())){
            builder.append(getName());
        }
        if (builder.length() == 0){
            builder.append(ItemUtils.getBandleLabel("NewPartner"));
        }
        return builder.toString();
    }
        
    public List<PartnerGroups> getPartnersGroupsList() {
        return partnersGroupsList;
    }
    public void setPartnersGroupsList(List<PartnerGroups> partnersGroupsList) {
        this.partnersGroupsList = partnersGroupsList;
    }
    
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Doc> getDocsList() {
        return docsList;
    }
    public void setDocsList(List<Doc> docsList) {
        this.docsList = docsList;
    }
    
    public List<PartnersContacts> getPartnersContactsList() {
        return partnersContactsList;
    }
    public void setPartnersContactsList(List<PartnersContacts> partnersContactsList) {
        this.partnersContactsList = partnersContactsList;
    }

    public Integer getTemplRegNumber() {
        return templRegNumber;
    }
    public void setTemplRegNumber(Integer templRegNumber) {
        this.templRegNumber = templRegNumber;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public PartnerTypes getType() {
        return type;
    }
    public void setType(PartnerTypes type) {
        this.type = type;
    }
    
    @Override
    public String getIconName() {
        iconName = "doc16";
        return iconName;
    }
    
    @Override
    public PartnerGroups getOwner() {
        return owner;
    }
    @Override
    public void setOwner(PartnerGroups owner) {
        this.owner = owner;
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
        if (!(object instanceof Partner)) {
            return false;
        }
        Partner other = (Partner) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.partners.Partners[ id=" + id + " ]";
    }   

}
