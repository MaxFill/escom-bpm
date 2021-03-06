package com.maxfill.model;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.user.User;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
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
public abstract class BaseLogItems<M extends BaseDict> implements Serializable, Dict{
    private static final long serialVersionUID = -3128180500478351775L;

    @Transient
    @XmlTransient
    private Integer id;
    
    @XmlTransient
    @Column(name = "Event", length = 1024)
    private String event;

    @XmlTransient
    @Column(name = "Params", length = 1024)
    private String params;
    
    @XmlTransient
    @JoinColumn(name = "Item", referencedColumnName = "ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private M item;
        
    @XmlTransient
    @Column(name = "DateEvent")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEvent;
    
    @XmlTransient
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User userId;
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
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

    public String getParams() {
        return params;
    }
    public void setParams(String params) {
        this.params = params;
    }
    
}
