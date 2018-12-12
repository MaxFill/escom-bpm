package com.maxfill.model.core.print;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author maksim
 */
public class JurnalReport implements Serializable{    
    private static final long serialVersionUID = -3489397035152627844L;
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    private final Integer id;
    
    private String name;
    private String regNumber;
    private String status;
    private String curatorName;
    private String authorName;
    private String typeName;
    private String companyName;
    
    private Date dateCreate;
    private Date datePlan;
    private Date itemDate;
    
    public JurnalReport() {
        id = COUNT.incrementAndGet();
    }

    /* GETS & SETS */

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
        
    public String getCuratorName() {
        return curatorName;
    }
    public void setCuratorName(String curatorName) {
        this.curatorName = curatorName;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public Date getItemDate() {
        return itemDate;
    }
    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }
    
    public Date getDatePlan() {
        return datePlan;
    }
    public void setDatePlan(Date datePlan) {
        this.datePlan = datePlan;
    }    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getRegNumber() {
        return regNumber;
    }
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }    
    
    /* *** *** */
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JurnalReport other = (JurnalReport) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Jurnal{" + "id=" + id + ", name=" + name + '}';
    }
        
}
