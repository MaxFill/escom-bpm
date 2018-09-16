package com.maxfill.model.process.reports;

import com.maxfill.dictionary.DictResults;
import com.maxfill.model.Dict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;

/**
 * Сущность "Отчёт по исполнению процесса"
 */
@Entity
@Table(name = "processReports")
public class ProcReport implements Serializable, Dict{
    private static final long serialVersionUID = 6494874383764066822L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    
    @Basic(optional = false)    
    @Column(name = "Content", length = 1024)    
    private String content;
    
    @Basic(optional = false)    
    @Column(name = "Status")    
    private String status;
    
    @XmlTransient
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;
    
    @XmlTransient
    @JoinColumn(name = "Executor", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff executor;
    
    @XmlTransient    
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;          
    
    @JoinColumn(name = "Version", referencedColumnName = "Id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Attaches version; 
        
    @Basic(optional = false)
    @Column(name = "DateCreate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;

    @Transient
    @XmlTransient
    private Integer tempId;
    
    private static final AtomicInteger COUNT = new AtomicInteger(0);
        
    public ProcReport() {
        tempId = COUNT.incrementAndGet();
    }

    public ProcReport(User author, Staff executor, Process process) {
        this("", "NoAgreementWasMade",  author, executor, process);
    }
    
    public ProcReport(String content, String status, User author, Staff executor, Process process) {
        this();
        this.content = content;
        this.status = status;
        this.author = author;
        this.executor = executor;
        this.process = process;
    }      
    
    /* GETS & SETS */
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
    }

    public Staff getExecutor() {
        return executor;
    }
    public void setExecutor(Staff executor) {
        this.executor = executor;
    }

    public Attaches getVersion() {
        return version;
    }
    public void setVersion(Attaches version) {
        this.version = version;
    }    
    
    public Integer getTempId() {
        return tempId;
    }
        
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.executor);
        hash = 53 * hash + Objects.hashCode(this.process);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProcReport other = (ProcReport) obj;
        if (!Objects.equals(this.executor, other.executor)) {
            return false;
        }
        if (!Objects.equals(this.process, other.process)) {
            return false;
        }
        return true;
    }



    @Override
    public String toString() {
        return "ProcExeReport{" + "id=" + id + '}';
    }        
    
}
