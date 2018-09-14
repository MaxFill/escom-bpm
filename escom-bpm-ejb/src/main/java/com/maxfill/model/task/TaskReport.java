package com.maxfill.model.task;

import com.maxfill.model.Dict;
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
 *
 * @author maksim
 */
@Entity
@Table(name = "tasksReports")
public class TaskReport implements Serializable, Dict{
    private static final long serialVersionUID = -8028605453168798790L;
    
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

    public TaskReport() {
        tempId = COUNT.incrementAndGet();
    }
    
    public TaskReport(String content, String status, User author, Task task) {        
        this();
        this.content = content;
        this.status = status;
        this.author = author;
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

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public Integer getTempId() {
        return tempId;
    }
    public void setTempId(Integer tempId) {
        this.tempId = tempId;
    }
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
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
        final TaskReport other = (TaskReport) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskReport{" + "id=" + id + '}';
    }
        
}
