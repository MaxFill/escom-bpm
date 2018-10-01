package com.maxfill.model.core.rights;

import com.maxfill.model.Dict;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.metadates.Metadates;
import java.io.Serializable;
import java.io.StringWriter;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "access")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Right implements Serializable, Dict{
    private static final long serialVersionUID = -6841901267921264389L;
      
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "Id")
    @XmlElement(name = "Id")
    private Integer id;     
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsRead")
    @XmlElement(name = "Read")
    private boolean read = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsUpdate")
    @XmlElement(name = "Update")
    private boolean update = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsCreate")
    @XmlElement(name = "Create")
    private boolean create = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsDelete")
    @XmlElement(name = "Delete")
    private boolean delete = true;
        
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsExecute")
    @XmlElement(name = "Execute")
    private boolean execute = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsChangeRight")
    @XmlElement(name = "ChangeRight")
    private boolean changeRight = true;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsAddChild")
    @XmlElement(name = "IsAddChild")
    private boolean addChild = true;

    @Basic(optional = false)
    @NotNull
    @Column(name = "IsAddDetail")
    @XmlElement(name = "IsAddDetail")
    private boolean addDetail = true;

    @Basic(optional = false)
    @NotNull
    @Column(name = "ObjId")
    @XmlElement(name = "ObjId")
    private Integer objId;
    
    /**
    * 0 - группа, 1 - пользователь
    */
    @Basic(optional = false)
    @NotNull
    @Column(name = "ObjType")
    @XmlElement(name = "ObjType")
    private Integer objType;
    
    /**
     * Объект метаданных. Ссылка используется для хранения записей дефолтных прав объекта метаданных
     */
    @JoinColumn(name = "ObjLink", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    @XmlTransient
    private Metadates objLink;
    
    /**
     * Состояние
     */
    @JoinColumn(name = "State", referencedColumnName = "ID")
    @ManyToOne 
    @NotNull
    @XmlElement(name = "State")
    private State state;   
    
    @Transient
    @XmlTransient
    private String name;
    
    @Transient
    @XmlTransient
    private String icon;
    
    public Right() {
    }

    public Right(Integer type, Integer objId, String name, State state, Metadates metadateObj) {
        this.objType = type; //0 - группа, 1 - пользователь
        this.objId = objId;  //id объекта, которому предоставляется право
        this.name = name;
        this.state = state;  //состояние, для которого создается право
        this.objLink = metadateObj; //объект метаданных, для которого создаётся право
    }

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
        
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isChangeRight() {
        return changeRight;
    }
    public void setChangeRight(boolean changeRight) {
        this.changeRight = changeRight;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public boolean isUpdate() {
        return update;
    }
    public void setUpdate(boolean update) {
        this.update = update;
    }
    
    public boolean isCreate() {
        return create;
    }
    public void setCreate(boolean create) {
        this.create = create;
    }
    
    public boolean isDelete() {
        return delete;
    }
    public void setDelete(boolean delete) {
        this.delete = delete;
    }
    
    public boolean isExecute() {
        return execute;
    }
    public void setExecute(boolean execute) {
        this.execute = execute;
    }

    public boolean isAddChild() {
        return addChild;
    }
    public void setAddChild(boolean addChild) {
        this.addChild = addChild;
    }

    public boolean isAddDetail() {
        return addDetail;
    }
    public void setAddDetail(boolean addDetail) {
        this.addDetail = addDetail;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getObjId() {
        return objId;
    }
    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public Integer getObjType() {
        return objType;
    }
    public void setObjType(Integer objType) {
        this.objType = objType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Right)) {
            return false;
        }
        Right other = (Right) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}

