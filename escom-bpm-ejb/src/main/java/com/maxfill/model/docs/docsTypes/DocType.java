package com.maxfill.model.docs.docsTypes;

import com.maxfill.model.BaseDict;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.numPuttern.NumeratorPattern;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/* Вид документа */
@Entity
@Table(name = "docsTypes")
@DiscriminatorColumn(name="REF_TYPE")
public class DocType extends BaseDict<DocTypeGroups, DocType, DocType, DocTypeLog, DocTypeStates>{      
    private static final long serialVersionUID = 9082567805735735647L;
    
    @TableGenerator(
        name="DocsTypesIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="DOCS_TYPES_ID", allocationSize  = 1)
                
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="DocsTypesIdGen")
    @Column(name = "Id")
    private Integer id;
       
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private DocTypeStates state;
        
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private DocTypeGroups owner;    
    
    @JoinColumn(name = "Numerator", referencedColumnName = "Id")
    @ManyToOne
    private NumeratorPattern numerator;
     
    @Basic(optional = false)
    @Size(max=10)
    @Column(name = "Code")
    private String code;
    
    @Basic(optional = false)
    @Size(max=50)
    @Column(name = "Guide")
    private String guide;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<DocTypeLog> itemLogs = new ArrayList<>();
    
    @JoinTable(name = "docsTypesStatuses", joinColumns = {
        @JoinColumn(name = "DocType", referencedColumnName = "Id")}, inverseJoinColumns = {
        @JoinColumn(name = "DocStatus", referencedColumnName = "ID")})
    @ManyToMany
    private List<StatusesDoc> statusDocList;

    @Override
    public DocTypeStates getState() {
        return state;
    }
    @Override
    public void setState(DocTypeStates state) {
        this.state = state;
    }
    
    @Override
    public String getIconName() {
        return "doc16"; 
    }
    
    @Override
    public List<DocTypeLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<DocTypeLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    public NumeratorPattern getNumerator() {
        return numerator;
    }
    public void setNumerator(NumeratorPattern numerator) {
        this.numerator = numerator;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getGuide() {
        return guide;
    }
    public void setGuide(String guide) {
        this.guide = guide;
    }
    
    public List<StatusesDoc> getStatusDocList() {
        return statusDocList;
    }
    public void setStatusDocList(List<StatusesDoc> statusDocList) {
        this.statusDocList = statusDocList;
    }

    @Override
    public DocTypeGroups getOwner() {
        return owner;
    }
    @Override
    public void setOwner(DocTypeGroups owner) {
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
        if (!(object instanceof DocType)) {
            return false;
        }
        DocType other = (DocType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.DocsTypes[ id=" + id + " ]";
    }

}
