package com.maxfill.model.basedict.doc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.model.basedict.docStatuses.DocStatuses;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.StringUtils;

/* Класс сущности "Документы" */
@Entity
@Table(name = "docs")
@DiscriminatorColumn(name = "REF_TYPE")
public class Doc extends BaseDict<Folder, Doc, Remark, DocLog, DocStates> {
    private static final long serialVersionUID = 5208895312598249913L;

    @TableGenerator(
        name = "docIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "DOC_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "docIdGen")
    private Integer id;          

    @Size(max = 50)
    @Column(name = "RegNumber")
    private String regNumber;
    
    @Column(name = "RoleJson", length = 2048)
    private String roleJson;
    
    @Column(name = "DateDoc")
    @Temporal(TemporalType.TIMESTAMP)
    private Date itemDate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "docs", orphanRemoval=true)
    private DocDou docsDou;
        
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private DocStates state;
    
    /* Замечания */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.LAZY, orphanRemoval=true)    
    private List<Remark> detailItems = new ArrayList<>();
        
    /* Список ссылающихся документов  */
    @OneToMany(mappedBy = "mainDoc", fetch = FetchType.LAZY)
    private List<Doc> docsLinks = new ArrayList<>();
    
    /* Список статусов документа  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doc", fetch = FetchType.LAZY, orphanRemoval=true)
    private List<DocStatuses> docsStatusList = new ArrayList<>();   
    
    /* Версии файлов  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doc", fetch = FetchType.LAZY)
    private List<Attaches> attachesList = new ArrayList<>();  
    
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Folder owner; 
        
    @ManyToMany(mappedBy = "docs", fetch = FetchType.LAZY)
    private List<Process> processList = new ArrayList<>();
        
    @JoinColumn(name = "DocType", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DocType docType;
    
    @JoinColumn(name = "Company", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Company company;
        
    @JoinColumn(name = "Partner", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Partner partner;

    @JoinColumn(name = "MainDoc", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Doc mainDoc;     
    
    public Doc() {
    }
    
    @Override
    public String getCompanyName() {
        return company != null ? company.getName() : "";
    }
    
    @Override
    public String getDocTypeName(){
        return docType != null ? docType.getName() : "";
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
    public Date getItemDate() {
        return itemDate;
    }
    @Override
    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }
    
    public Doc getMainDoc() {
        return mainDoc;
    }
    public void setMainDoc(Doc mainDoc) {
        this.mainDoc = mainDoc;
    }

    public List<Doc> getDocsLinks() {
        return docsLinks.stream().filter(doc -> !doc.isDeleted()).collect(Collectors.toList());
    }
    public void setDocsLinks(List<Doc> docsLinks) {
        this.docsLinks = docsLinks;
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
    public List<Remark> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List<Remark> detailItems) {
        this.detailItems = detailItems;
    }
        
    @Override
    public String getPath(){        
        return getOwner().getPath();
    }
        
    /* Возвращает текущую версию вложения  */
    public Attaches getMainAttache() {
        return attachesList.stream().filter(a -> a.getCurrent()).findFirst().orElse(null);        
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
        if (getMainAttache() == null) return null; 
        return getMainAttache().getNumber();        
    }
    
    /* Возвращает полное имя документа */
    @Override
    public String getFullName(){        
        StringBuilder builder = new StringBuilder();
        if (docType != null && StringUtils.isNotBlank(docType.getName())){
            builder.append(docType.getName()).append(" ");
        } 
        if (StringUtils.isNotBlank(regNumber)){
            builder.append("№").append(regNumber).append(" ");
        }
        builder.append(getName());
        return builder.toString();
    }
    
    public String getRegInfo(Locale locale){
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(regNumber)){
            sb.append(ItemUtils.getBandleLabel("NumberShort", locale)).append(" ").append(regNumber);
        } 
        if (itemDate != null){
            sb.append(" ").append(DateUtils.dateToString(itemDate, DateFormat.SHORT, null, locale));
        }
        return sb.toString();
    }
    
    public Partner getPartner() {
        return partner;
    }
    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    @Override
    public String getRoleJson() {
        return roleJson;
    }
    @Override
    public void setRoleJson(String roleJson) {
        this.roleJson = roleJson;
    }
    
    @Override
    public Map<String, Set<Integer>> getRoles() {
        if (roles == null){
            roles = new HashMap<>();
            if (StringUtils.isBlank(getRoleJson())) return roles;
            try {
                ObjectMapper mapper = new ObjectMapper();            
                roles = mapper.readValue(roleJson, new TypeReference<HashMap<String, HashSet<Integer>>>() {});
                setRoles(roles);
            } catch (IOException ex) {
                Logger.getLogger(Doc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return roles;
    }
    
    @Override
    public String getIconName() {
        return "doc16";
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

    public List<Attaches> getAttachesList() {
        return attachesList;
    }
    public void setAttachesList(List<Attaches> attachesList) {
        this.attachesList = attachesList;
    }

    @Override
    public String getRegNumber() {
        return regNumber;
    }
    @Override
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
       
    public List<DocStatuses> getDocsStatusList() {
        return docsStatusList;
    }
    public void setDocsStatusList(List<DocStatuses> docsStatusList) {
        this.docsStatusList = docsStatusList;
    }

    public List<Process> getProcessList() {
        return processList;
    }
    public void setProcessList(List<Process> processList) {
        this.processList = processList;
    }
          
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
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
        return "Docs[ id=" + id + " ] [" + getName() + "]";
    }

}