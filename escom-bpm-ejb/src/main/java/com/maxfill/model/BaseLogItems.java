package com.maxfill.model;

import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Базовый класс сущности таблиц логов объектов
 * @author mfilatov
 * @param <M> main класс для лога
 */
@MappedSuperclass
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public abstract class BaseLogItems<M extends BaseDict> implements Serializable{
    private static final long serialVersionUID = -3128180500478351775L;

    @Transient
    @XmlTransient
    private Integer id;
    
    @XmlTransient
    @Size(min = 1, max = 256)
    @Column(name = "Event")
    private String event;

    @XmlTransient
    @JoinColumn(name = "Item", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private M item;
        
    @XmlTransient
    @Column(name = "DateEvent")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEvent;
    
    @XmlTransient
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User userId;
    
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

    public M getItem() {
        return item;
    }
    public void setItem(M item) {
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

}
