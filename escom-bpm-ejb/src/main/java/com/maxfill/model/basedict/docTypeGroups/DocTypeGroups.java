package com.maxfill.model.basedict.docTypeGroups;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.docType.DocType;
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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/* Сущность "Группы видов документов" */
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
    @Column(name = "Id")
    private Integer id;
    
    @OneToMany
    @JoinColumn(name = "parent")
    private List<DocTypeGroups> childItems;
        
    @OneToMany
    @JoinColumn(name = "owner")
    private List<DocType> detailItems = new ArrayList<>();
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
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
    public List<DocType> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List<DocType> detailItems){
        this.detailItems = detailItems;
    }

    @Override
    public List<DocTypeGroups> getChildItems() {
        return childItems;
    }
    @Override
    public void setChildItems(List<DocTypeGroups> childItems) {
        this.childItems = childItems;
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
        return "DocTypeGroups[ id=" + id + " ] ["+ getName() + "]";
    }
    
}
