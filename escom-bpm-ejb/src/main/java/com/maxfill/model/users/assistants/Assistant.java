package com.maxfill.model.users.assistants;

import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность "Заместитель"
 */
@Entity
@Table(name = "assistants")
@DiscriminatorColumn(name="REF_TYPE")
public class Assistant extends BaseDict<User, Assistant, Assistant, AssistantLog, AssistantStates>{    
    private static final long serialVersionUID = -8842864273225553855L;
    
    @TableGenerator(
        name="AssistantIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="Assistant_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="AssistantIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @JoinColumn(name = "User", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User user;        
            
    /* Вид замещения */
    @Column(name="SubstitutionType")
    private String substitutionType = "always"; 
     
    /* Начало срока замещения */
    @Column(name = "BeginDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginDate;

    /* Конец срока замещения */
    @Column(name = "EndDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private AssistantStates state;
       
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    @Transient
    @XmlTransient
    private Integer tempId;
    
    public Assistant() {
        tempId = COUNT.incrementAndGet();
    }

    @Override
    public String getFullName() {
        if (user != null){
            return user.getFullName();
        } else {
            return super.getFullName(); 
        }    
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
    
    @Override
    public Date getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public String getSubstitutionType() {
        return substitutionType;
    }
    public void setSubstitutionType(String substitutionType) {
        this.substitutionType = substitutionType;
    }
    
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public AssistantStates getState() {
        return state;
    }
    @Override
    public void setState(AssistantStates state) {
        this.state = state;
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
        hash = 19 * hash + Objects.hashCode(this.id);
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
        final Assistant other = (Assistant) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Assistant{" + "id=" + id + '}';
    }
        
}
