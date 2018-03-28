package com.maxfill.model.metadates;

import com.maxfill.model.filters.Filter;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.rights.Right;
import com.maxfill.model.states.State;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "metadates")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Metadates implements Serializable{
    private static final long serialVersionUID = -3191404107074233285L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    @XmlElement(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "ObjectName")
    private String objectName;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsDetailLogging")
    private boolean isDetailLogging;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "BundleName")
    private String bundleName;
      
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "BundleJurnalName")
    private String bundleJurnalName;    
        
    @Basic(optional = false)
    @NotNull
    @Column(name = "Icon")
    private String iconObject;
        
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "objLink")
    private List<Right> rightList;
    
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "metadateObj")
    private List<MetadatesStates> metadatesStates; //list satates object for available moved
        
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "Metadates")
    private List<Filter> filters = new ArrayList<>();
    
    @XmlTransient
    @JoinTable(name = "objectsStates", joinColumns = {
        @JoinColumn(name = "ObjId", referencedColumnName = "Id")}, inverseJoinColumns = {
        @JoinColumn(name = "StateId", referencedColumnName = "ID")})
    @ManyToMany
    private List<State> statesList; //list states available for object
          
    @JoinColumn(name = "StateForNewObj", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    @NotNull
    private State stateForNewObj;
    
    @JoinColumn(name = "NumPattern", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private NumeratorPattern numPattern;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "metadateObj")
    private List<FavoriteObj> favoriteObjList;    
        
    public Metadates() {
    }

    public String getIconObject() {
        return iconObject;
    }

    public List<MetadatesStates> getMetadatesStates() {
        return metadatesStates;
    }
    public void setMetadatesStates(List<MetadatesStates> metadatesStates) {
        this.metadatesStates = metadatesStates;
    }    
    
    public String getObjectName() {
        return objectName;
    }    
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public boolean getIsDetailLogging() {
        return isDetailLogging;
    }
    public void setIsDetailLogging(boolean isDetailLogging) {
        this.isDetailLogging = isDetailLogging;
    }

    public List<Right> getRightList() {
        return rightList;
    }
    public void setRightList(List<Right> rightList) {
        this.rightList = rightList;
    }

    public List<State> getStatesList() {
        return statesList;
    }
    public void setStatesList(List<State> statesList) {
        this.statesList = statesList;
    }

    public NumeratorPattern getNumPattern() {
        return numPattern;
    }
    public void setNumPattern(NumeratorPattern numPattern) {
        this.numPattern = numPattern;
    }

    public String getBundleName() {
        return bundleName;
    }
    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public State getStateForNewObj() {
        return stateForNewObj;
    }
    public void setStateForNewObj(State stateForNewObj) {
        this.stateForNewObj = stateForNewObj;
    }

    public List<Filter> getFilters() {
        return filters;
    }
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public String getBundleJurnalName() {
        return bundleJurnalName;
    }
    public void setBundleJurnalName(String bundleJurnalName) {
        this.bundleJurnalName = bundleJurnalName;
    }

    public List<FavoriteObj> getFavoriteObjList() {
        return favoriteObjList;
    }
    public void setFavoriteObjList(List<FavoriteObj> favoriteObjList) {
        this.favoriteObjList = favoriteObjList;
    }

    public Integer getId() {
        return id;
    }
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
        if (!(object instanceof Metadates)) {
            return false;
        }
        Metadates other = (Metadates) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.system.metadata.Metadates[ id=" + id + " ]";
    }
}
