package com.maxfill.model.folders;

import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.users.User;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/* Класс сущности "Папки документов"  */
@Entity
@Table(name = "folders")
@DiscriminatorColumn(name="REF_TYPE")
public class Folder extends BaseDict<Folder, Folder, Doc, FolderLog, FolderStates>{
    private static final long serialVersionUID = -7531636538666889579L;

    @TableGenerator(
        name="idGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="FOLDER_ID", allocationSize = 1)
 
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    @GeneratedValue(strategy=TABLE, generator="idGen")
    private Integer id;
            
    @OneToMany
    @JoinColumn(name = "parent")
    private List<Folder> childItems;
    
    @JoinColumn(name = "Moderator", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User moderator;
    
    @XmlTransient
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "State", referencedColumnName = "Id")
    private FolderStates state;
        
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsModeration")
    private boolean isModeration;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsLocked")
    private boolean isLocked = false;
    
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Doc> detailItems = new ArrayList<>();
    
    @JoinColumn(name = "DocTypeDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private DocType docTypeDefault;
    
    @JoinColumn(name = "PartnerDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Partner partnerDefault;
          
    @JoinColumn(name = "CompanyDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company companyDefault;
      
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsInheritCompany")
    private boolean inheritCompany = true;  
    
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsInheritDocType")
    private boolean inheritDocType = true;
    
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsInheritPartner")
    private boolean inheritPartner = true;
        
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<FolderLog> itemLogs = new ArrayList<>();

    public Folder(){
    }

    public Company getCompanyDefault() {
        return companyDefault;
    }
    public void setCompanyDefault(Company companyDefault) {
        this.companyDefault = companyDefault;
    }

    public boolean isInheritCompany() {
        return inheritCompany;
    }
    public void setInheritCompany(boolean inheritCompany) {
        this.inheritCompany = inheritCompany;
    }

    public boolean isInheritDocType() {
        return inheritDocType;
    }
    public void setInheritDocType(boolean inheritDocType) {
        this.inheritDocType = inheritDocType;
    }

    public boolean isInheritPartner() {
        return inheritPartner;
    }
    public void setInheritPartner(boolean inheritPartner) {
        this.inheritPartner = inheritPartner;
    }
        
    @Override
    public FolderStates getState() {
        return state;
    }
    @Override
    public void setState(FolderStates state) {
        this.state = state;
    }

    public Partner getPartnerDefault() {
        return partnerDefault;
    }
    public void setPartnerDefault(Partner partnerDefault) {
        this.partnerDefault = partnerDefault;
    }
        
    @Override
    public List<Doc> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List<Doc> detailItems) {
        this.detailItems = detailItems;
    }
    
    @Override
    public List<FolderLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<FolderLog> itemLogs) {
        this.itemLogs = itemLogs;
    }       
    
    public String getRegNumber(){
        if (getNumber() == null){
            return "";
        }
        return getNumber().toString();
    }
    
    /* вычисление значка папки (модерируемая-немодерируемая) */
    public String getStateIcon(){
        String stateIcon = "ui-icon-folder-open";
        if(isModeration == true){
          stateIcon = "ui-icon-person";
        }
        return stateIcon;
    }    
               
    @Override
    public String getIconName() {
        return "folder_open20";
    }
    
    public boolean getIsModeration() {return isModeration;}
    public void setIsModeration(boolean isModeration) {       this.isModeration = isModeration;    }
    
    public boolean getIsLocked() {return isLocked;}
    public void setIsLocked(boolean isLocked) {        this.isLocked = isLocked;    }    
    
    public User getModerator() { return moderator;    }
    public void setModerator(User moderator) {        this.moderator = moderator;    }

    @Override
    public List<Folder> getChildItems() {
        return childItems;
    }
    @Override
    public void setChildItems(List<Folder> childItems) {
        this.childItems = childItems;
    }   

    public DocType getDocTypeDefault() {
        return docTypeDefault;
    }
    public void setDocTypeDefault(DocType docTypeDefault) {
        this.docTypeDefault = docTypeDefault;
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
        if (!(object instanceof Folder)) {
            return false;
        }
        Folder other = (Folder) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Folder [ id=" + id + " ]";
    }   


}
