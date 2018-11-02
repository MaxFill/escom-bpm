package com.maxfill.model.attaches;

import com.maxfill.model.Dict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.user.User;
import com.maxfill.utils.EscomUtils;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/* Вложения */
@Entity
@Table(name = "attaches", indexes = {@Index(name="Attaches_GUID_INDEX", columnList = "Guide", unique = true)})
public class Attaches implements Serializable, Dict {
    private static final long serialVersionUID = -4633936978772232516L;

    @TableGenerator(
        name="attacheIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="ATTACHE_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="attacheIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)
    @Column(name = "Number")
    private Integer number;
            
    @Basic(optional = false)
    @Column(name = "Name", length = 1024)
    private String name;
    
    @Basic(optional = false)
    @Column(name = "Size")
    private Long size;
    
    @Column(name = "DateCreate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;
    
    @Basic(optional = false)
    @Column(name = "Type")
    private String type;
    
    @Size(min = 1, max = 10)
    @Column(name = "Extension")
    private String extension;
        
    @Column(name = "Guide")
    private String guid;    
    
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;  
        
    @JoinColumn(name = "LockAuthor", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User lockAuthor;
    
    @Column(name = "LockDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockDate;
    
    @Column(name = "PlanUnLockDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date planUnlockDate;
    
    @Column(name = "CountRemainingCycles")
    private Integer countRemainingCycles;
    
    @Lob
    @Column(name = "TimeHandle")
    private byte[] timeHandle;
        
    @JoinColumn(name = "Doc", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc doc;
    
    @Basic(optional = false)
    @Column(name = "IsCurrent")
    private Boolean current = true;
            
    @Transient
    @XmlTransient
    private Integer tempId;

    private static final AtomicInteger COUNT = new AtomicInteger(0);
           
    public Attaches() {
        tempId = COUNT.incrementAndGet();
        guid = EscomUtils.generateGUID();
    }

    /**
     * Возвращает имя файла без расширения
     * @param sb
     * @return 
     */
    public String getShortName(StringBuilder sb){        
        sb.append(guid.substring(0, 2))
            .append(File.separator)
            .append(guid.substring(2, 4))
            .append(File.separator)
            .append(guid);
        return sb.toString();
    }
    
    /* Возвращает полное имя файла на сервере  */
    public String getFullName(){
        StringBuilder sb = new StringBuilder();
        getShortName(sb);
        sb.append(".").append(getExtension());
        return sb.toString();
    }
    
    /* Возвращает полное имя pdf файла на сервере  */
    public String getFullNamePDF(){
        StringBuilder sb = new StringBuilder();
        getShortName(sb);
        sb.append(".pdf").toString();
        return sb.toString();
    }
        
    public String getNamePDF(){
        return FilenameUtils.removeExtension(name)+".pdf";
    }
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (StringUtils.isNotBlank(name) && name.length() > 1024){
            name = name.substring(0, 1023);
        }
        this.name = name;
    }

    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Doc getDoc() {
        return doc;
    }
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Integer getTempId() {
        return tempId;
    }
    public void setTempId(Integer tempId) {
        this.tempId = tempId;
    }

    public Boolean getCurrent() {
        return current;
    }
    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }

    public User getLockAuthor() {
        return lockAuthor;
    }
    public void setLockAuthor(User lockAuthor) {
        this.lockAuthor = lockAuthor;
    }

    public Date getLockDate() {
        return lockDate;
    }
    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public Date getPlanUnlockDate() {
        return planUnlockDate;
    }
    public void setPlanUnlockDate(Date planUnlockDate) {
        this.planUnlockDate = planUnlockDate;
    }

    public byte[] getTimeHandle() {
        return timeHandle;
    }
    public void setTimeHandle(byte[] timeHandle) {
        this.timeHandle = timeHandle;
    }

    public Integer getCountRemainingCycles() {
        return countRemainingCycles;
    }
    public void setCountRemainingCycles(Integer countRemainingCycles) {
        this.countRemainingCycles = countRemainingCycles;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Attaches)) {
            return false;
        }
        Attaches other = (Attaches) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.docs.attaches.Attaches[ id=" + id + " ]";
    }
    
}
