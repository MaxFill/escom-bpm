package com.maxfill.model.basedict.company;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.department.Department;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
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
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private CompanyStates state;
    
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Department> detailItems = new ArrayList<>();        

    @Basic(optional = false)    
    @Column(name = "WorkTime")
    private Integer workTime = 8; //кол-во рабочих часов в дне
    
    @Basic(optional = false)    
    @Column(name = "BeginTime")
    private Integer beginTime = 28800; //начало рабочего дня в секундах
    
    public Company() {
    }

    /* GETS & SETS */
    
    public Integer getWorkTime() {
        return workTime;
    }
    public void setWorkTime(Integer workTime) {
        this.workTime = workTime;
    }

    public Integer getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Integer beginTime) {
        this.beginTime = beginTime;
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
    public String getIconName(){
        return "home";
    }    

    @Override
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public List<Department> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List<Department> detailItems) {
        this.detailItems = detailItems;
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
        return "Company[ id=" + id + " ] [" + getName() + "]";
    }

    }
