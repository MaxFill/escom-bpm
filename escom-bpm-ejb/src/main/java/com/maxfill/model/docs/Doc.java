package com.maxfill.model.docs;

import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.users.User;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.StringUtils;

/* Класс сущности "Документы" */
@Entity
@Table(name = "docs")
@DiscriminatorColumn(name = "REF_TYPE")
public class Doc extends BaseDict<Folder, Doc, Doc, DocLog, DocStates> {            
    private static final long serialVersionUID = 5208895312598249913L;

    @TableGenerator(
        name = "docIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "DOC_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "docIdGen")
    private Integer id;

    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Folder owner;
        
    @JoinColumn(name = "DocType", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private DocType docType;
    
    @JoinColumn(name = "Company", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company company;
        
    @JoinColumn(name = "Partner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Partner partner;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "docs")
    private DocDou docsDou;

    @Size(max = 50)
    @Column(name = "RegNumber")
    private String regNumber;
    
    @Column(name = "RoleJson", length = 2048)
    private String roleJson;
    
    @Column(name = "DateDoc")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDoc;

    @JoinTable(name = "favoriteDocs", joinColumns = {
        @JoinColumn(name = "DocId", referencedColumnName = "Id")}, inverseJoinColumns = {
        @JoinColumn(name = "UserId", referencedColumnName = "Id")})
    @ManyToMany
    private List<User> userList;

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private DocStates state;
        
    /* Список статусов документа  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doc")
    private List<DocStatuses> docsStatusList = new ArrayList<>();
    
    /* Версии файлов  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doc")
    private List<Attaches> attachesList = new ArrayList<>();
    
    /* Лог */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<DocLog> itemLogs = new ArrayList<>();
       
    public Doc() {
    }

    @Override
    public DocStates getState() {
        return state;
    }
    @Override
    public void setState(DocStates state) {
        this.state = state;
    }
    
    @Override
    public Folder getOwner() {
        return owner;
    }
    @Override
    public void setOwner(Folder owner) {
        this.owner = owner;
    }

    @Override
    public String getPath(){        
        return getOwner().getPath();
    }
        
    /* Возвращает текущую версию вложения  */
    public Attaches getAttache() {
        Attaches rezult = null;
        for (Attaches attache : attachesList){
            if (Boolean.TRUE.equals(attache.getCurrent())){
                rezult = attache;
                break;
            }
        }
        return rezult;
    }
    
    /* Вычисление значка папки модерируемая-немодерируемая  */
    public String getStateIcon() {
        String stateIcon = "doc16";
        //ToDo нужно сделать обработку значка статуса документа
        return stateIcon;
    }

    /* Возвращает следующий номер версии документа  */
    public Integer getNextVersionNumber() {
        Integer max = 0;
        if (attachesList.size() > 0) {
            for (int i = 0; i < attachesList.size(); i++) {
                if (attachesList.get(i).getNumber() > max) {
                    max = attachesList.get(i).getNumber();
                }
            }
        }
        max++;
        return max;
    }

    /* Возвращает номер текущей версии документа */
    public Integer getCurrentVersionNumber() {
        if (getAttache() == null) return null; 
        return getAttache().getNumber();        
    }
    
    /* Возвращает полное имя документа */
    @Override
    public String getFullName(){        
        StringBuilder builder = new StringBuilder();
        if (docType != null && StringUtils.isNotBlank(docType.getName())){
            builder.append(docType.getName()).append(" ");
        }
        if (StringUtils.isNotBlank(regNumber)){
            builder.append(regNumber).append(" ");
        }    
        builder.append(getNameEndElipse());
        return builder.toString();
    }    
     
    public Partner getPartner() {
        return partner;
    }
    public void setPartner(Partner partner) {
        this.partner = partner;
    }
 
    public String getRoleJson() {
        return roleJson;
    }
    public void setRoleJson(String roleJson) {
        this.roleJson = roleJson;
    }
    
    @Override
    public String getIconName() {
        return "doc16";
    }

    @Override
    public List<DocLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<DocLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    public DocType getDocType() {
        return docType;
    }
    public void setDocType(DocType docType) {
        this.docType = docType;
    } 

    public DocDou getDocsDou() {
        return docsDou;
    }
    public void setDocsDou(DocDou docsDou) {
        this.docsDou = docsDou;
    }

    public Date getDateDoc() {
        return dateDoc;
    }
    public void setDateDoc(Date dateDoc) {
        this.dateDoc = dateDoc;
    }

    public List<Attaches> getAttachesList() {
        return attachesList;
    }
    public void setAttachesList(List<Attaches> attachesList) {
        this.attachesList = attachesList;
    }

    public String getRegNumber() {
        return regNumber;
    }
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
       
    public List<User> getUserList() {
        return userList;
    }
    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public List<DocStatuses> getDocsStatusList() {
        return docsStatusList;
    }
    public void setDocsStatusList(List<DocStatuses> docsStatusList) {
        this.docsStatusList = docsStatusList;
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
        if (!(object instanceof Doc)) {
            return false;
        }
        Doc other = (Doc) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.Docs[ id=" + id + " ]";
    }

}