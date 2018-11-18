package com.maxfill.model.basedict.remark;

import com.maxfill.dictionary.SysParams;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.Process;
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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;

/**
 * Сущность "Замечание"
 * @author maksim
 */
@Entity
@Table(name = "remarks")
@DiscriminatorColumn(name = "REF_TYPE")
public class Remark extends BaseDict<Doc, Remark, Remark, RemarkLog, RemarkStates> {    
    private static final long serialVersionUID = -6292099538231215250L;
    
    @TableGenerator(
            name = "IdRemarkGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "REMARK_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "IdRemarkGen")
    @Column(name = "Id")
    private Integer id;

    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc owner;

    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private RemarkStates state;
    
    @Basic(optional = false)
    @Column(name = "Checked")
    private boolean checked;    //проверено
    
    @Basic(optional = false)
    @Column(name = "Content", length = 1024)
    private String content;
    
    @Transient
    @XmlTransient
    private final Integer tempId;
    
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    public Remark() {
        tempId = COUNT.incrementAndGet();
    }

    @Override
    public String getNameEndElipse() {
        return StringUtils.abbreviate(getContent(), SysParams.LENGHT_NAME_ELIPSE);   
    }
    
    
    /* GETS & SETS */        

    @Override
    public String getIconName() {
        if (checked){
            return "/resources/icon/doc_check20.png";
        } else 
            return "/resources/icon/importante.png";
    }        
    
    public Integer getTempId() {
        return tempId;
    }
    
    @Override
    public Doc getOwner() {
        return owner;
    }    
    @Override
    public void setOwner(Doc owner) {
        this.owner = owner;
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
    }
    
    @Override
    public RemarkStates getState() {
        return state;
    }
    @Override
    public void setState(RemarkStates state) {
        this.state = state;
    }    

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }      

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }    
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final Remark other = (Remark) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Remark{" + "id=" + id + '}';
    }    
    
}
