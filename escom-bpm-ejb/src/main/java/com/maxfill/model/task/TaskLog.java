package com.maxfill.model.task;

import com.maxfill.model.BaseLogItems;
import com.maxfill.model.process.Process;
import com.maxfill.model.users.User;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "taskLog")
public class TaskLog implements Serializable{
    private static final long serialVersionUID = -9197466421481526458L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @XmlTransient
    @Size(min = 1, max = 256)
    @Column(name = "Event")
    private String event;

    @XmlTransient
    @JoinColumn(name = "Item", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Task item;

    @XmlTransient
    @Column(name = "DateEvent")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEvent;

    @XmlTransient
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User userId;

    public TaskLog() {
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }

    public Task getItem() {
        return item;
    }
    public void setItem(Task item) {
        this.item = item;
    }

    public Date getDateEvent() {
        return dateEvent;
    }
    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    public User getUserId() {
        return userId;
    }
    public void setUserId(User userId) {
        this.userId = userId;
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
        if (!(object instanceof TaskLog)) {
            return false;
        }
        TaskLog other = (TaskLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskLog[ id=" + id + " ]";
    }
    
}
