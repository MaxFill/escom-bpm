package com.maxfill.model.basedict.staff;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.post.Post;
import com.maxfill.model.basedict.user.User;
import org.apache.commons.lang.StringUtils;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
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

    @Basic(optional = false)
    @Column(name = "InheritsWorkTime")
    private boolean inheritsWorkTime = true;    //рабочий график наследуется от компании
    
    @Basic(optional = false)    
    @Column(name = "WorkTime")
    private Integer workTimeHour = 8; //кол-во рабочих часов в дне
    
    @Basic(optional = false)    
    @Column(name = "WorkTimeMinute")
    private Integer workTimeMinute = 0; 
        
    @Basic(optional = false)    
    @Column(name = "BeginTime")
    private Integer beginTime = 28800; //начало рабочего дня
    
    @Basic(optional = false)
    @Column(name = "IsFired")
    private boolean isFired;

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private StaffStates state;
        
    public Staff() {
    }
    
    /**
     * Формирует полное наименование штатной единицы: Должность ФИО Подразделение Компания
     * @return
     */
    @Override
    public String getFullName(){
        StringBuilder sb = new StringBuilder();
        if (employee != null && StringUtils.isNotBlank(employee.getName())){
            sb.append(employee.getShortFIO()).append(" ");
        }
        if (post != null && StringUtils.isNotBlank(post.getName())){
            sb.append(post.getName()).append(" ");
        }
        if (owner != null && StringUtils.isNotBlank(owner.getName())){
            sb.append(owner.getFullName())
                    .append(" ")        
                    .append(owner.getOwner() != null ? owner.getOwner().getName() : "");
        } else
            if (company != null){
                sb.append(company.getName());
            }
        return sb.toString();
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

    /* Формирует краткое наименование штатной единицы в формате Должность И.О. Фамилия */
    public String getStaffFIO() {
        StringBuilder sb = new StringBuilder();  
         if (post != null & StringUtils.isNotBlank(post.getName())){
            sb.append(post.getName()).append(" ");
        }
        if (employee != null && StringUtils.isNotBlank(employee.getName())){
            sb.append(employee.getOfficialFIO());
        } 
        return  sb.toString() ;
    }

    @Override
    public String getNameEndElipse(){
        return StringUtils.abbreviate(getName(), 40);
    } 
    
    @Override
    public String getEmployeeFIO(){
        return employee != null ? employee.getShortFIO() : "vacancy";
    }       
        
    /* Возвращает email штатной единицы (из user)  */
    @Override
    public String getEmail() {
        if (employee != null) {
            return employee.getEmail();
        } else {
            return null;
        }
    }
    
    @Override
    public String getPostName(){
        return post != null ? post.getName() : "";
    }

    public String getPhone(){
        if (employee == null) return "";
        return employee.getPhone();
    }
    
    /* GETS & SETS */
    
    @Override
    public String getIconName() {
        return "16_inspector";
    }

    @Override
    public StaffStates getState() {
        return state;
    }
    @Override
    public void setState(StaffStates state) {
        this.state = state;
    }

    public Integer getWorkTimeMinute() {
        return workTimeMinute;
    }
    public void setWorkTimeMinute(Integer workTimeMinute) {
        this.workTimeMinute = workTimeMinute;
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

    public boolean isInheritsWorkTime() {
        return inheritsWorkTime;
    }
    public void setInheritsWorkTime(boolean inheritsWorkTime) {
        this.inheritsWorkTime = inheritsWorkTime;
    }

    public Integer getWorkTimeHour() {
        return workTimeHour;
    }
    public void setWorkTimeHour(Integer workTimeHour) {
        this.workTimeHour = workTimeHour;
    }

    public Integer getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Integer beginTime) {
        this.beginTime = beginTime;
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
        return "Staff[ id=" + id + " ] [" + getName() + "]";
    }

}
