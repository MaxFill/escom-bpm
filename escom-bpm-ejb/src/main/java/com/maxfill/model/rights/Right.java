
package com.maxfill.model.rights;

import com.maxfill.dictionary.DictRights;
import com.maxfill.model.states.State;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.io.StringWriter;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Права доступа
 * @author Maxim
 */
@Entity
@Table(name = "access")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Right implements Serializable{
    private static final long serialVersionUID = -6841901267921264389L;
   
    @TableGenerator(
        name="rightIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="RIGHT_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="rightIdGen")
    @Column(name = "Id")
    @XmlElement(name = "Id")
    private Integer id;     
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsRead")
    @XmlElement(name = "Read")
    private boolean read;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsUpdate")
    @XmlElement(name = "Update")
    private boolean update ;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsCreate")
    @XmlElement(name = "Create")
    private boolean create;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsDelete")
    @XmlElement(name = "Delete")
    private boolean delete;
        
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsExecute")
    @XmlElement(name = "Execute")
    private boolean execute;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsChangeRight")
    @XmlElement(name = "ChangeRight")
    private boolean changeRight;
        
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
     * Объект метаданных
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

    public Right(Integer type, Integer id, Metadates objLink, String name, State state) {
        this.objType = type; //0 - группа, 1 - пользователь
        this.objId = id;        
        this.name = name;
        this.objLink = objLink;
        this.state = state;
    }

    @XmlTransient
    //формирование имени типа для отображения на карточке
    public String getTypeName(){         
        switch (objType){
            case (DictRights.TYPE_GROUP):{  
                return ItemUtils.getBandleLabel("RightForGroup");                
            }
            case (DictRights.TYPE_USER): {
                return ItemUtils.getBandleLabel("RightForUser"); 
            }
            case (DictRights.TYPE_ROLE): {
                return ItemUtils.getBandleLabel("RightForRole"); 
            }
        }   
        return "";
    } 

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
        
    public Integer getId() {
        return id;
    }
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

    public Metadates getObjLink() {
        return objLink;
    }
    public void setObjLink(Metadates objLink) {
        this.objLink = objLink;
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

