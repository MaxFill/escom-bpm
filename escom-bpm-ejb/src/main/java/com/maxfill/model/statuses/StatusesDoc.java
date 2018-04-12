package com.maxfill.model.statuses;

import com.maxfill.model.BaseDict;
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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Класс сущности "Статусы документов"
 */
@Entity
@Table(name = "statusesDoc")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
@DiscriminatorColumn(name="REF_TYPE")
public class StatusesDoc extends BaseDict<StatusesDoc, StatusesDoc, StatusesDoc, StatusesDocLog, StatusesDocStates> {
    private static final long serialVersionUID = -8577581379121348639L;

    @TableGenerator(
        name="StatusDocIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="StatusDoc_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="StatusDocIdGen")
    @Column(name = "ID")
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<StatusesDocLog> itemLogs = new ArrayList<>();            
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private StatusesDocStates state;

    @Override
    public StatusesDocStates getState() {
        return state;
    }
    @Override
    public void setState(StatusesDocStates state) {
        this.state = state;
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
    public List<StatusesDocLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<StatusesDocLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StatusesDoc)) {
            return false;
        }
        StatusesDoc other = (StatusesDoc) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.docs.docsStatus.StatusDoc[ id=" + id + " ]";
    }
    
}
