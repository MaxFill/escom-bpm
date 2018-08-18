package com.maxfill.model.process.reports;

import com.maxfill.model.Dict;
import com.maxfill.model.process.Process;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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
    @Column(name = "Content")    
    private String content;
    
    @Basic(optional = false)    
    @Column(name = "Status")    
    private String status;
    
    @XmlTransient
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;
    
    @XmlTransient
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;
      
    @XmlTransient
    @JoinColumn(name = "Task", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Task task;
    
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

    public ProcReport(String content, String status, User author, Process process, Task task) {        
        this();
        this.content = content;
        this.status = status;
        this.author = author;
        this.process = process;
        this.task = task;
        this.dateCreate = new Date();
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

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    public Integer getTempId() {
        return tempId;
    }
        
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.id);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcExeReport{" + "id=" + id + '}';
    }        
    
}
