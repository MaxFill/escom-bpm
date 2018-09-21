package com.maxfill.services;

import com.maxfill.services.common.history.ServicesEvents;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/*  Сущность "Системная служба (сервис)" */
@Entity
@Table(name = "services")
public class Services implements Serializable {
    private static final long serialVersionUID = -4878024303594842658L;
    
    @TableGenerator(
        name="ServicesIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Services_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="ServicesIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Size(max = 50)
    @Column(name = "Name")
    private String name;
    
    @Lob
    @Column(name = "Settings", length = 1024)
    private byte[] settings;

    @Lob
    @Column(name = "Sheduler", length = 1024)
    private byte[] sheduler;
       
    @Column(name = "Started")
    private Boolean started = false;
    
    @Column(name = "DateNextStart")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateNextStart;
      
    @Lob
    @Column(name = "TimeHandle")
    private byte[] timeHandle;
        
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serviceId", fetch = FetchType.LAZY, orphanRemoval=true)
    private List<ServicesEvents> servicesEventsList;
        
    public Services() {
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
    
    public List<ServicesEvents> getServicesEventsList() {
        return servicesEventsList;
    }
    public void setServicesEventsList(List<ServicesEvents> servicesEventsList) {
        this.servicesEventsList = servicesEventsList;
    }

    public Boolean getStarted() {
        return started;
    }
    public void setStarted(Boolean started) {
        this.started = started;
    }

    public byte[] getSettings() {
        return settings;
    }
    public void setSettings(byte[] settings) {
        this.settings = settings;
    }

    public byte[] getSheduler() {
        return sheduler;
    }
    public void setSheduler(byte[] sheduler) {
        this.sheduler = sheduler;
    }

    public Date getDateNextStart() {
        return dateNextStart;
    }
    public void setDateNextStart(Date dateNextStart) {
        this.dateNextStart = dateNextStart;
    }

    public byte[] getTimeHandle() {
        return timeHandle;
    }
    public void setTimeHandle(byte[] timeHandle) {
        this.timeHandle = timeHandle;
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
        if (!(object instanceof Services)) {
            return false;
        }
        Services other = (Services) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.system.services.Services[ id=" + id + " ]";
    }
    
}
