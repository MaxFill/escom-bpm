
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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Maxim
 */
@Entity
@Table(name = "filters")
@DiscriminatorColumn(name="REF_TYPE")
public class Filters extends BaseDict<Filters,Filters,Filters,FiltersLog> {
    private static final long serialVersionUID = 6183682742885585999L;
    
    @TableGenerator(
        name="FiltersIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Filters_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="FiltersIdGen")
    @Column(name = "Id")
    private Integer id;    
    
    @Column(name = "Icon")
    private String icon;
    
    @JoinColumn(name = "Metadates", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Metadates metadates;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<FiltersLog> itemLogs = new ArrayList<>();
    
    public Filters() {
    }

    @Override
    public List<FiltersLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<FiltersLog> itemLogs) {
        this.itemLogs = itemLogs;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
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
    public List<Filters> getDetailItems() {
        return null;
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
        if (!(object instanceof Filters)) {
            return false;
        }
        Filters other = (Filters) object;
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
