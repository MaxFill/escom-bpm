
package com.maxfill.model.docs.docStatuses;

import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/* Список статусов со своими значениями в документе. Подчинённая таблица к документам */
@Entity
@Table(name = "docsStatus")
public class DocStatuses implements Serializable {
    private static final long serialVersionUID = -6573535287409441895L;
    
    @TableGenerator(
        name="DocsStatusIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="DocsStatus_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="DocsStatusIdGen")
    @NotNull
    @Column(name = "Id")
    private Integer id;
        
    @Basic(optional = false)
    @NotNull
    @Column(name = "Value")
    private Boolean value;
    
    @Column(name = "DateStatus")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStatus;
    
    @JoinColumn(name = "Doc", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc doc;
    
    @JoinColumn(name = "Status", referencedColumnName = "ID")
    @ManyToOne
    private StatusesDoc status;
    
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;

    public DocStatuses() { 
    }

    public DocStatuses(Doc doc, StatusesDoc status) {
        this.doc = doc;
        this.status = status;
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getValue() {
        return value;
    }
    public void setValue(Boolean value) {
        this.value = value;
    }

    public Date getDateStatus() {
        return dateStatus;
    }
    public void setDateStatus(Date dateStatus) {
        this.dateStatus = dateStatus;
    }

    public Doc getDoc() {
        return doc;
    }
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public StatusesDoc getStatus() {
        return status;
    }
    public void setStatus(StatusesDoc status) {
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
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
        if (!(object instanceof DocStatuses)) {
            return false;
        }
        DocStatuses other = (DocStatuses) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {        
        return ItemUtils.getBandleLabel(status.getName()) + "=" + this.getValue();
    }
    
}