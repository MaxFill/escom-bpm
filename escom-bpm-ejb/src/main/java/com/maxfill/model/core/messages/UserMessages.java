package com.maxfill.model.core.messages;

import com.maxfill.model.Dict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.task.Task;
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
@Table(name = "userMessages")
public class UserMessages implements Serializable, Dict{
    private static final long serialVersionUID = 5227427212529784972L;
    
    @TableGenerator(
        name="UserMessagesIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="UserMessages_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="UserMessagesIdGen")
    @Column(name = "Id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "Name")
    private String name;
            
    @Basic(optional = false)
    @Size(min = 0, max = 4096)        
    @Column(name = "Content")
    private String content;
    
    @Size(max = 255)
    @Column(name = "Sender")
    private String sender;  
        
    @JoinColumn(name = "Addressee", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User addressee;
    
    @JoinColumn(name = "Document", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc document;
    
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;
    
    @JoinColumn(name = "Task", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Task task;
        
    @Basic(optional = false)
    @Column(name = "DateSent")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateSent;
    
    @Basic(optional = false)
    @Column(name = "DateReading")
    @Temporal(TemporalType.TIMESTAMP)    
    private Date dateReading;    
    
    @Column(name = "Importance")
    private Integer importance = 0;        

    public UserMessages() {
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public User getAddressee() {
        return addressee;
    }
    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getDateSent() {
        return dateSent;
    }
    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Date getDateReading() {
        return dateReading;
    }
    public void setDateReading(Date dateReading) {
        this.dateReading = dateReading;
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
        if (!(object instanceof UserMessages)) {
            return false;
        }
        UserMessages other = (UserMessages) object;
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
