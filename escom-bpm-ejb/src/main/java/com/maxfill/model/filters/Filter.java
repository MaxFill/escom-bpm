package com.maxfill.model.filters;

import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
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
import javax.xml.bind.annotation.XmlTransient;

/* Фильтры журнала объектов  */
@Entity
@Table(name = "filters")
@DiscriminatorColumn(name="REF_TYPE")
public class Filter extends BaseDict<Filter,Filter,Filter,FilterLog, FiltersStates> {
    private static final long serialVersionUID = 6183682742885585999L;
    
    @TableGenerator(
        name="FiltersIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Filters_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="FiltersIdGen")
    @Column(name = "Id")
    private Integer id;    
    
    @OneToMany
    @JoinColumn(name = "parent")
    private List<Filter> childItems;
        
    @Column(name = "Icon")
    private String icon;
    
    @JoinColumn(name = "Metadates", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Metadates metadates;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<FilterLog> itemLogs = new ArrayList<>();
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private FiltersStates state;
        
    public Filter() {
    }

    @Override
    public FiltersStates getState() {
        return state;
    }
    @Override
    public void setState(FiltersStates state) {
        this.state = state;
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
    public List<Filter> getChildItems() {
        return childItems;
    }
    @Override
    public void setChildItems(List<Filter> childItems) {
        this.childItems = childItems;
    }
    
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Metadates getMetadates() {
        return metadates;
    }
    public void setMetadates(Metadates metadates) {
        this.metadates = metadates;
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
        if (!(object instanceof Filter)) {
            return false;
        }
        Filter other = (Filter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.filters.Filters[ id=" + id + " ]";
    }
    
}
