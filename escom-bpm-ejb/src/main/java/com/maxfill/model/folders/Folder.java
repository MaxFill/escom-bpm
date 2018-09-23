package com.maxfill.model.folders;

import com.maxfill.model.BaseDict;;
import com.maxfill.model.companies.Company;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/* Класс сущности "Папки документов"  */
@Entity
@Table(name = "folders")
@DiscriminatorColumn(name="REF_TYPE")
public class Folder extends BaseDict<Folder, Folder, Doc, FolderLog, FolderStates> {
    private static final long serialVersionUID = -7531636538666889579L;

    @TableGenerator(
        name="idGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="FOLDER_ID", allocationSize = 1)
 
    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy=TABLE, generator="idGen")
    private Integer id;            
        
    @Basic(optional = false)
    @Column(name = "IsModeration")
    private boolean isModeration;
    
    @Basic(optional = false)
    @Column(name = "IsLocked")
    private boolean isLocked;

    @Basic(optional = false)
    @Column(name = "FolderNumber")
    private String folderNumber;

    @Column(name = "DateFolder")
    @Temporal(TemporalType.TIMESTAMP)
    private Date itemDate;
    
    @XmlTransient
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "State", referencedColumnName = "Id")
    private FolderStates state;
        
    @OneToMany
    @JoinColumn(name = "parent")
    private List<Folder> childItems; 
        
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Doc> detailItems = new ArrayList<>();
    
    @JoinColumn(name = "Moderator", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User moderator;   
        
    @JoinColumn(name = "DocTypeDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DocType docTypeDefault;
    
    @JoinColumn(name = "PartnerDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Partner partnerDefault;
          
    @JoinColumn(name = "CompanyDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Company companyDefault;
      
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsInheritCompany")
    private boolean inheritCompany = true;  
    
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsInheritDocType")
    private boolean inheritDocType = true;
    
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsInheritPartner")
    private boolean inheritPartner = true;

    public Folder(){}

    /* Вычисление значка папки (модерируемая-немодерируемая) */
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

    @Override
    public String getFullName() {
        return super.getPath();
    }

    /**
     * Возвращает сокращённое по длине имя папки, начинающееся с индекса дела для отображения в дереве папок
     * @return
     */
    @Override
    public String getNameEndElipse() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(folderNumber)){
            sb.append(folderNumber).append(" ");
        }
        sb.append(super.getNameEndElipse());
        return sb.toString();
    }

    /**
     * Возвращает полный индекс дела
     * @return
     */
    public String getFolderFullNumber(){
        if (StringUtils.isBlank(folderNumber)) return null;

        StringBuilder sb = new StringBuilder();
        String parentNumber = getParentNumber();
        if (StringUtils.isNotBlank(parentNumber)){
            sb.append(parentNumber);
        }
        sb.append(folderNumber);
        return sb.toString();
    }

    /**
     * Возвращает составной номер родительской папки
     * @return
     */
    public String getParentNumber(){
        if (getParent() == null) return null;

        StringBuilder sb = new StringBuilder();
        ItemUtils.makeParentNumber(sb, getParent());
        return sb.toString();
    }

    /**
     * Возвращает заголовок для карточки папки
     * @return
     */
    @Override
    public String getCaption(){
        StringBuilder sb = new StringBuilder();
        String fullNumber = getFolderFullNumber();
        if (StringUtils.isNotBlank(fullNumber)) {
            sb.append(fullNumber).append(" ");
        }
        sb.append(super.getNameEndElipse());
        return sb.toString();
    }

    /**
     * Возвращает флаг - является ли папка делом
     * @return
     */
    public boolean isCase(){
        return StringUtils.isNotBlank(folderNumber);
    }

    /**
     * Возвращает индекс дела для отображения его в таблице обозревателя
     * @return
     */
    @Override
    public String getRegNumber(){
        return getFolderFullNumber();
    }   
    @Override
    public void setRegNumber(String regNumber) {
        folderNumber = regNumber;
    }
    
    /* gets & sets */

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public List<Folder> getChildItems() {
        return childItems;
    }
    @Override
    public void setChildItems(List<Folder> childItems) {
        this.childItems = childItems;
    }

    @Override
    public Date getItemDate() {
        return itemDate;
    }    
    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }
    
    @Override
    public FolderStates getState() {
        return state;
    }
    @Override
    public void setState(FolderStates state) {
        this.state = state;
    }
        
    @Override
    public List<Doc> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List<Doc> detailItems) {
        this.detailItems = detailItems;
    }

    public String getFolderNumber() {
        return folderNumber;
    }
    public void setFolderNumber(String folderNumber) {
        this.folderNumber = folderNumber;
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

    public boolean getIsModeration() {return isModeration;}
    public void setIsModeration(boolean isModeration) {       this.isModeration = isModeration;    }
    
    public boolean getIsLocked() {return isLocked;}
    public void setIsLocked(boolean isLocked) {        this.isLocked = isLocked;    }    
    
    public User getModerator() { return moderator;    }
    public void setModerator(User moderator) {        this.moderator = moderator;    }

    public DocType getDocTypeDefault() {
        return docTypeDefault;
    }
    public void setDocTypeDefault(DocType docTypeDefault) {
        this.docTypeDefault = docTypeDefault;
    }

    public Partner getPartnerDefault() {
        return partnerDefault;
    }
    public void setPartnerDefault(Partner partnerDefault) {
        this.partnerDefault = partnerDefault;
    }

    /* *** *** */
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
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
