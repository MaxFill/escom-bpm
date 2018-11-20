package com.maxfill.model.core.eventfeed;

import com.maxfill.model.Dict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.user.User;
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
import javax.validation.constraints.Size;

@Entity
@Table(name = "eventFeed")
public class EventFeed implements Serializable, Dict{
    private static final long serialVersionUID = 4556855173389434173L;
    
    @TableGenerator(
        name="EventFeedIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="EventFeed_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="EventFeedIdGen")
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)    
    @Size(min = 0, max = 250)
    @Column(name = "Name", length = 255)
    private String name;
       
    @Basic(optional = false)    
    @Size(min = 0, max = 250)
    @Column(name = "IconName")
    private String iconName;
    
    @Basic(optional = false)
    @Size(min = 0, max = 4090)
    @Column(name = "Content", length = 4096 )
    private String content;
        
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;
    
    @JoinColumn(name = "Document", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc document;
    
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;    
        
    @Basic(optional = false)
    @Column(name = "DateEvent")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateEvent;        
    
    @Column(name = "Importance")
    private Integer importance = 0;        

    public EventFeed() {
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getDateEvent() {
        return dateEvent;
    }
    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    public Integer getImportance() {
        return importance;
    }
    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public Doc getDocument() {
        return document;
    }
    public void setDocument(Doc document) {
        this.document = document;
    }

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
    }       

    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
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
        if (!(object instanceof EventFeed)) {
            return false;
        }
        EventFeed other = (EventFeed) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.system.UserMessages[ id=" + id + " ]";
    }
}
