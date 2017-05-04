
package com.maxfill.model.numPuttern;

import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictNumerator;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Шаблоны нумераторов
 * @author mfilatov
 */
@Entity
@Table(name = "numeratorPattern")
@NamedQueries({
    @NamedQuery(name = "NumeratorPattern.findAll", query = "SELECT n FROM NumeratorPattern n")})
@DiscriminatorColumn(name="REF_TYPE")
public class NumeratorPattern extends BaseDict<NumeratorPattern, NumeratorPattern, NumeratorPattern, NumeratorPatternLog> {
    private static final long serialVersionUID = 7646428401886042406L;

    @TableGenerator(
        name="numeratorPatternIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="NUM_PATTERN_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    @GeneratedValue(strategy=TABLE, generator="numeratorPatternIdGen")
    private Integer id;
    
    /**
     * Шаблон формирования рег. номера
     */
    @Basic(optional = false)
    @NotNull
    @Size(max=100)
    @Column(name = "Pattern")
    private String pattern;
    
    /**
     * Тип нумератора (см. DictNumerator.java)
     */
    @Basic(optional = false)
    @NotNull
    @Size(max=16)
    @Column(name = "TypeCode")
    private String typeCode = DictNumerator.TYPE_MANUAL;
     
    /**
    * Длина номера 
    */
    @Basic(optional = false)
    @NotNull
    @Column(name = "LeadingZeros")
    private Integer leadingZeros = 0;
            
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsResetNewYear")
    private Boolean resetNewYear = true;
            
    @OneToMany(mappedBy = "numerator")
    private List<DocType> docTypeList;    
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<NumeratorPatternLog> itemLogs = new ArrayList<>();
    
    public NumeratorPattern() {
    }

    @Override
    public List<NumeratorPatternLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<NumeratorPatternLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    public String getPattern() {
        return pattern;
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<DocType> getDocTypeList() {
        return docTypeList;
    }
    public void setDocTypeList(List<DocType> docTypeList) {
        this.docTypeList = docTypeList;
    }

    public String getTypeCode() {
        return typeCode;
    }
    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getLeadingZeros() {
        return leadingZeros;
    }
    public void setLeadingZeros(Integer leadingZeros) {
        this.leadingZeros = leadingZeros;
    }

    public Boolean getResetNewYear() {
        return resetNewYear;
    }
    public void setResetNewYear(Boolean resetNewYear) {
        this.resetNewYear = resetNewYear;
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
        if (!(object instanceof NumeratorPattern)) {
            return false;
        }
        NumeratorPattern other = (NumeratorPattern) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.system.numPuttern.NumeratorPattern[ id=" + id + " ]";
    }
    
}
