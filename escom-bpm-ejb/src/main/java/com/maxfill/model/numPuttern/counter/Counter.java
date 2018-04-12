package com.maxfill.model.numPuttern.counter;

import com.maxfill.model.companies.Company;
import com.maxfill.model.docs.docsTypes.DocType;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "numerator")
public class Counter implements Serializable {
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
    @Size(min = 1, max = 50)
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
    
    @JoinColumn(name = "Company", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company company;
    
    @JoinColumn(name = "DocType", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private DocType docType;    
    
    public Counter() {
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public DocType getDocType() {
        return docType;
    }
    public void setDocType(DocType docType) {
        this.docType = docType;
    }
    
    public Counter(Integer id) {
        this.id = id;
    }

    public Counter(Integer id, String name, int number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    public Integer getId() {
        return id;
    }

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
        return "com.maxfill.escombpm2.model.system.numerator.Numerator[ id=" + id + " ]";
    }
    
}
