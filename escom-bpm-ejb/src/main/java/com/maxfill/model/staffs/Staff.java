package com.maxfill.model.staffs;

import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.users.User;
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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/* Штатная единица */
@Entity
@Table(name = "staffs")
@DiscriminatorColumn(name = "REF_TYPE")
public class Staff extends BaseDict<Department, Staff, Staff, StaffLog, StaffStates> {
    private static final long serialVersionUID = 7883918061335878269L;

    @TableGenerator(
            name = "idStaffGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "STAFF_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "idStaffGen")
    @Column(name = "Id")
    private Integer id;

    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Department owner;

    @JoinColumn(name = "Company", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company company;
    
    @JoinColumn(name = "Post", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Post post;

    @JoinColumn(name = "Employee", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User employee;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<StaffLog> itemLogs = new ArrayList<>();    

    @Basic(optional = false)
    @NotNull
    @Column(name = "IsFired")
    private boolean isFired;

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private StaffStates state;
        
    public Staff() {
    }

    @Override
    public StaffStates getState() {
        return state;
    }
    @Override
    public void setState(StaffStates state) {
        this.state = state;
    }
    
    /* Формирует наименование Компания + Подразделение   */
    public String getOrgUnit() {
        StringBuilder orgUnit = new StringBuilder();
        if (owner != null){
            if (owner.getOwner() != null){    //TODO нужно сделать формирование полной цепочки
                orgUnit.append(owner.getOwner().getName());
                orgUnit.append("->");
            }
            orgUnit.append(owner.getName()).append(" Id=").append(owner.getId());
        } else 
            if (company != null){
                orgUnit.append(company.getName()).append(" Id=").append(company.getId());
            }        
        return orgUnit.toString();
    }

    /* Формирует краткое наименование штатной единицы  */
    public String getStaffFIO() {
        return employee.getShortFIO() + " " + post.getName();
    }

    /* Возвращает email штатной единицы (из user)  */
    public String getEmail() {
        if (employee != null) {
            return employee.getEmail();
        } else {
            return null;
        }
    }

    @Override
    public String getIconName() {
        return "16_inspector";
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
        
    public boolean getIsFired() {
        return isFired;
    }
    public void setIsFired(boolean isFired) {
        this.isFired = isFired;
    }

    public Post getPost() {
        return post;
    }
    public void setPost(Post post) {
        this.post = post;
    }

    public User getEmployee() {
        return employee;
    }
    public void setEmployee(User employee) {
        this.employee = employee;
    }  
    
    @Override
    public Department getOwner() {
        return owner;
    }
    @Override
    public void setOwner(Department owner) {
        this.owner = owner;
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
    public List<Staff> getDetailItems() {
        return null;
    }
    
    @Override
    public List<Staff> getChildItems() {
        return null;
    }
    
    @Override
    public List<StaffLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<StaffLog> itemLogs) {
        this.itemLogs = itemLogs;
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
        if (!(object instanceof Staff)) {
            return false;
        }
        Staff other = (Staff) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Staff[ id=" + id + " ]";
    }

}
