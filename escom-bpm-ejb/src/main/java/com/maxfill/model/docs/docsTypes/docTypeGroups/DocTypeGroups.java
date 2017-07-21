
package com.maxfill.model.docs.docsTypes.docTypeGroups;

import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocType;
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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/* Группы видов документов */
@Entity
@Table(name = "docTypeGroups")
@DiscriminatorColumn(name="REF_TYPE")
public class DocTypeGroups extends BaseDict<DocTypeGroups, DocTypeGroups, DocType, DocTypeGroupsLog, DocTypeGroupsStates> {
private static final long serialVersionUID = -2116686297842684933L;

    @TableGenerator(
        name="DocTypeGroupIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="DOC_TYPE_GROUP_ID", allocationSize  = 1)

    @Id
    @GeneratedValue(strategy=TABLE, generator="DocTypeGroupIdGen")
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    private Integer id;

    /**
     * Связь с подчинёнными объектами (Виды документов)
    */
    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "owner")
    private List<DocType> docTypes = new ArrayList<>();
       
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<DocTypeGroupsLog> itemLogs = new ArrayList<>();
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private DocTypeGroupsStates state;
        
    public DocTypeGroups() {
    } 

    @Override
    public DocTypeGroupsStates getState() {
        return state;
    }
    @Override
    public void setState(DocTypeGroupsStates state) {
        this.state = state;
    }
    
    @Override
    public List<DocTypeGroupsLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<DocTypeGroupsLog> itemLogs) {
        this.itemLogs = itemLogs;
    }
        
    public List<DocType> getDocTypes() {
        return docTypes;
    }
    public void setDocTypes(List<DocType> docTypes) {
        this.docTypes = docTypes;
    }

    @Override
    public List<DocType> getDetailItems() {
        return docTypes;
    }
    @Override
    public void setDetailItems(List<DocType> detailItems){
        this.docTypes = detailItems;
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
        if (!(object instanceof DocTypeGroups)) {
            return false;
        }
        DocTypeGroups other = (DocTypeGroups) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.docs.docsTypes.docTypeGroups.DocTypeGroups[ id=" + id + " ]";
    }
    
}
