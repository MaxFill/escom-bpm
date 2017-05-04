/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.model.partners.contacts;

import com.maxfill.model.partners.Partner;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Maxim
 */
@Entity
@Table(name = "partnersContacts")
@NamedQueries({
    @NamedQuery(name = "PartnersContacts.findAll", query = "SELECT p FROM PartnersContacts p")})
public class PartnersContacts implements Serializable {
    private static final long serialVersionUID = 4750167357883109074L;    
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private Integer id;
    
    @Size(max = 50)
    @Column(name = "Name")
    private String name;
   
    @JoinColumn(name = "Partner", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Partner partner;

    public PartnersContacts() {
    }

    public PartnersContacts(Integer id) {
        this.id = id;
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

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
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
        if (!(object instanceof PartnersContacts)) {
            return false;
        }
        PartnersContacts other = (PartnersContacts) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.partners.PartnersContacts[ id=" + id + " ]";
    }
    
}
