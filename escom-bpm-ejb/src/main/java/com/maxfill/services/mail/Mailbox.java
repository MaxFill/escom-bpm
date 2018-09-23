package com.maxfill.services.mail;

import com.maxfill.model.Dict;
import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность
 */
@Entity
@Table(name = "mailbox")
@NamedQueries({
    @NamedQuery(name = "Mailbox.findAll", query = "SELECT m FROM Mailbox m")})
public class Mailbox implements Serializable, Dict {
    private static final long serialVersionUID = 7057744852693399572L;

    @TableGenerator(
        name="MailboxIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Mailbox_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="MailboxIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Column(name = "Subject")
    private String subject;
    
    @Column(name = "DateCreate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;
    
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;
    
    @Column(name = "AuthorName")
    private String authorName;
    
    @Size(max = 50)
    @Column(name = "Sender")
    private String sender;
    
    @Size(max = 1024)
    @Column(name = "Addresses")
    private String addresses;
    
    @Lob
    @Column(name = "MsgContent", length = 2048)
    private byte[] msgContent;
    
    @Size(max = 1024)
    @Column(name = "CopyAddres")
    private String copies;
    
    @Lob
    @Column(name = "Attaches", length = 2048)
    private byte[] attaches;
    
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsActual")
    private boolean actual;
        
    public Mailbox() {
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public String getAddresses() {
        return addresses;
    }
    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getCopies() {
        return copies;
    }
    public void setCopies(String copies) {
        this.copies = copies;
    }

    public boolean isActual() {
        return actual;
    }
    public void setActual(boolean actual) {
        this.actual = actual;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public byte[] getMsgContent() {
        return msgContent;
    }
    public void setMsgContent(byte[] msgContent) {
        this.msgContent = msgContent;
    }

    public byte[] getAttaches() {
        return attaches;
    }
    public void setAttaches(byte[] attaches) {
        this.attaches = attaches;
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
        if (!(object instanceof Mailbox)) {
            return false;
        }
        Mailbox other = (Mailbox) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.system.services.mail.Mailbox[ id=" + id + " ]";
    }
}
