
package com.maxfill.services.common.history;

import com.maxfill.services.Services;
import com.maxfill.utils.DateUtils;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Журнал истории событий служб
 * @author Maxim
 */
@Entity
@Table(name = "services_events")
public class ServicesEvents implements Serializable {
    private static final long serialVersionUID = -4436787147305389769L;
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @Column(name = "DateStart")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStart;
    
    @Basic(optional = false)
    @Column(name = "DateFinish")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinish;
    
    @Basic(optional = false)
    @Size(min = 1, max = 50)
    @Column(name = "Result")    
    private String result;
    
    @Basic(optional = false)
    @Size(min = 1, max = 2147483647)
    @Column(name = "Details")
    private String details;
    
    @JoinColumn(name = "ServiceId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Services serviceId;

    @XmlTransient
    public Date getDuration(){
        return DateUtils.differenceDate(dateStart, dateFinish);
    }
    
    public ServicesEvents() {
    }

    public ServicesEvents(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateFinish() {
        return dateFinish;
    }

    public void setDateFinish(Date dateFinish) {
        this.dateFinish = dateFinish;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Services getServiceId() {
        return serviceId;
    }

    public void setServiceId(Services serviceId) {
        this.serviceId = serviceId;
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
        if (!(object instanceof ServicesEvents)) {
            return false;
        }
        ServicesEvents other = (ServicesEvents) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.system.services.history.ServicesEvents[ id=" + id + " ]";
    }
    
}
