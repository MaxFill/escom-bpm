package com.maxfill.model.departments;

import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
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

/**
 * Подразделения
 * @author Maxim
 */
@Entity
@Table(name = "departments")
@DiscriminatorColumn(name="REF_TYPE")
public class Department extends BaseDict<Company, Department, Staff, DepartamentLog> {
    private static final long serialVersionUID = -8752263203249959973L;
    
        @TableGenerator(
        name="idDepartamentGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="DEPARTAMENT_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="idDepartamentGen")
    @Column(name = "Id")
    private Integer id;
     
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company owner;
    
    @OneToMany(mappedBy = "parent")
    private List<Department> departmentList;
    
    @JoinColumn(name = "Parent", referencedColumnName = "Id")
    @ManyToOne
    private Department parent;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<Staff> staffList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<DepartamentLog> itemLogs = new ArrayList<>();
    
    /**
     * Руководитель подразделения
     */
    @JoinColumn(name = "Chief", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff chief;
    
    @Basic(optional = false)
    @Size(max=50)
    @Column(name = "Code")
    private String code;
        
    public Department() {
    }

    @Override
    public List<DepartamentLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<DepartamentLog> itemLogs) {
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
    
    @Override
    public Department getParent() {
        return parent;
    }
    @Override
    public void setParent(Department parent) {
        this.parent = parent;
    }

    @Override
    public Company getOwner() {
        return owner;
    }
    @Override
    public void setOwner(Company owner) {
        this.owner = owner;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }
    public void setDepartmentList(List<Department> departmentList) {
        this.departmentList = departmentList;
    }

    public Staff getChief() {
        return chief;
    }
    public void setChief(Staff chief) {
        this.chief = chief;
    }

    public List<Staff> getStaffList() {
        return staffList;
    }
    public void setStaffList(List<Staff> staffList) {
        this.staffList = staffList;
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
        if (!(object instanceof Department)) {
            return false;
        }
        Department other = (Department) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.docs.Departments[ id=" + id + " ]";
    }
    
}
