package com.maxfill.model.basedict.numeratorPattern.counter;

import com.maxfill.model.Dict;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

/**
 * Счётчик нумератора
 */
@Entity
@Table(name = "numerator")
public class Counter implements Serializable, Dict {
    private static final long serialVersionUID = -8832941995816832839L;

    @TableGenerator(
        name="numeratorIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="NUMERATOR_ID", allocationSize = 1)
 
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="numeratorIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull    
    @Column(name = "Name")
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "Number")
    private int number;                   
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "Year")
    private int year; 
    
    @Basic(optional = false)
    @Column(name = "Company")    
    private String companyName;
    
    @Basic(optional = false)
    @Column(name = "Type")    
    private String typeName;
    
    public Counter() {
    }
    
    public Counter(Integer id) {
        this.id = id;
    }

    public Counter(Integer id, String name, int number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    /* GETS & SETS */
    
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
        
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Counter)) {
            return false;
        }
        Counter other = (Counter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Counter{" + "id=" + id + ", name=" + name + '}';
    }
    
}
