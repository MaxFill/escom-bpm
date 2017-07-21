package com.maxfill.model.companies;

import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/* Компания, организация */
@Entity
@Table(name = "companies")
@DiscriminatorColumn(name="REF_TYPE")
public class Company extends BaseDict<Company, Company, Department, CompanyLog, CompanyStates> {    
    private static final long serialVersionUID = 8479016605948929175L;

    @TableGenerator(
        name="idCompanyGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="COMPANY_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="idCompanyGen")
    @Column(name = "Id")
    private Integer id;    
    
    /* Используется в шаблоне регистрационного номера  */
    @Basic(optional = false)
    @Size(max=10)
    @Column(name = "Code")
    private String code;      
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private CompanyStates state;
        
    @OneToMany(mappedBy = "owner")
    private List<Department> departmentsList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<CompanyLog> itemLogs = new ArrayList<>();
    
    public Company() {
    }

    @Override
    public CompanyStates getState() {
        return state;
    }
    @Override
    public void setState(CompanyStates state) {
        this.state = state;
    }  
    
    @Override
    public List<CompanyLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<CompanyLog> itemLogs) {
        this.itemLogs = itemLogs;
    }   
    
    @Override
    public String getIconName(){
        return "home";
    }        
    
    public List<Department> getDepartmentsList() {
        return departmentsList;
    }
    public void setDepartmentsList(List<Department> departmentsList) {
        this.departmentsList = departmentsList;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public List<Department> getDetailItems() {
        return departmentsList;
    }
    @Override
    public void setDetailItems(List<Department> detailItems) {
        this.departmentsList = detailItems;
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
        if (!(object instanceof Company)) {
            return false;
        }
        Company other = (Company) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.docs.Companies[ id=" + id + " ]";
    }

    }
