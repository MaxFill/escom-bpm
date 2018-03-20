package com.maxfill.model.authlog;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/* Класс сущности событий аутентификации */
@Entity
@Table(name = "authlog")
public class Authlog implements Serializable{
    private static final long serialVersionUID = 2001763513625256052L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)
    @Size(max = 255)
    @Column(name = "Login")
    @NotNull
    private String login;

    @Basic(optional = false)
    @Size(max = 255)
    @Column(name = "EventName")
    @NotNull
    private String eventName;

    @Basic(optional = false)
    @Column(name = "DateEvent")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateEvent;

    @Basic(optional = false)
    @Size(max = 32)
    @Column(name = "IP")
    private String ipAdress;

    @NotNull
    @Basic(optional = false)
    @Column(name = "SendSMS")
    private boolean sendSMS;

    public Authlog() {}

    /* gets & sets */

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getDateEvent() {
        return dateEvent;
    }
    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getIpAdress() {
        return ipAdress;
    }
    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public boolean isSendSMS() {
        return sendSMS;
    }
    public void setSendSMS(boolean sendSMS) {
        this.sendSMS = sendSMS;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Authlog authlog = (Authlog) o;

        return id != null ? id.equals(authlog.id) : authlog.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
