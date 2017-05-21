package com.maxfill.model;

import com.maxfill.model.states.State;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.utils.SysParams;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/* Базовый класс справочников
 * @param <O> Класс владельца
 * @param <P> Класс родителя
 * @param <D> Класс подчинённых объектов
 * @param <L> Класс таблицы лога */
@MappedSuperclass
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public abstract class BaseDict<O extends BaseDict, P extends BaseDict, D extends BaseDict, L extends BaseLogTable> implements Serializable {
    private static final long serialVersionUID = 1844448252960314998L;
    private static final AtomicInteger NUMBER_ID = new AtomicInteger(0);
    
    @Transient
    @XmlTransient
    private Integer id;

    @XmlTransient
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private O owner;
    
    @XmlTransient
    @JoinColumn(name = "Parent", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private P parent;
    
    @XmlTransient
    @JoinColumn(name = "Author", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User author;
            
    @Size(max = 100)
    @Column(name = "Name")
    private String name;        
        
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "DateCreate")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateCreate;
        
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "DateChange")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date dateChange;
    
    @XmlTransient
    @Basic(optional = false)    
    @Column(name = "Access")
    private String access;      
        
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private State state;
      
    /* Журнал истории объекта  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<L> itemLogs = new ArrayList<>();
      
    /* Дочерние child объекты, полученные по ссылке на parent */
    @XmlTransient
    @OneToMany
    @JoinColumn(name = "parent")
    private List<P> childItems;    
        
    /* Подчинённые detail объекты, полученные или через ссылку на owner или через связь многие-ко-многим  */
    @XmlTransient
    @OneToMany
    @JoinColumn(name = "owner")
    private List<D> detailItems;
        
    /* Признак наследования прав объекта. 
     * TRUE - наследуются, FALSE - не наследуются (индивидуальные права)  */
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsInherits")
    private boolean inherits = true;    
        
    /* Признак того, что запись помечена на удаление */
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsDeleted")
    private boolean deleted;
        
    /* Признак того, что запись актуальная */
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsActual")
    private boolean actual;        
    
    @XmlTransient
    @Column(name = "Number")
    private Integer number;        
    
    @Transient
    @XmlTransient   
    protected String iconName;
    
    @Transient
    @XmlTransient           
    protected String iconTree;    
    
    @Transient
    @XmlTransient
    private Integer rightMask;  //маска доступа
    
    @Transient
    @XmlTransient
    private Rights rightItem; //права доступа в виде объекта    
        
    /* Название для установленного признака наследования  */
    @Transient
    @XmlTransient
    private String inheritsRightName;

    /* Возврашает путь в дереве к объекту */
    @Transient
    @XmlTransient
    private String path;
      
    /* Признак наследования дефолтных прав для дочерних объектов */
    @XmlTransient
    @NotNull
    @Basic(optional = false)
    @Column(name = "IsInheritsAccessChilds")
    private boolean inheritsAccessChilds;
    
    /* Дефолтные Права доступа для подчинённых объектов в виде строки в xml */
    @XmlTransient
    @Size(max = 2147483647)
    @Column(name = "AccessChilds")
    private String xmlAccessChild; 
    
    /* Права доступа к подчинённому объекту  */
    @Transient
    @XmlTransient
    private Rights rightForChild; //права для дочерних объектов 
    
    public BaseDict(){}
        
    /* Возвращает название для заголовка наследования дефолтных прав  дочерних объектов */
    public String getInheritsAccessChildName(){
        if (inheritsAccessChilds){
            return ItemUtils.getMessageLabel("RightsInheritedForChilds");
        } else {
            return ItemUtils.getMessageLabel("RightsNotInheritedForChilds");
        }
    }   
    
    public List<D> getDetailItems() {
        return detailItems;
    }
    public void setDetailItems(List<D> detailItems) {
        this.detailItems = detailItems;
    }
    
    public Rights getRightForChild() {
        return rightForChild;
    }
    public void setRightForChild(Rights rightForChild) {
        this.rightForChild = rightForChild;
    }   
    
    public String getXmlAccessChild() {
        return xmlAccessChild;
    }
    public void setXmlAccessChild(String xmlAccessChild) {
        this.xmlAccessChild = xmlAccessChild;
    }
           
    public String getIconTree(){
        return "ui-icon-home";
    }
    
    public String getItemKey(){
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("_");
        if (getId() == null){
            Integer num = NUMBER_ID.incrementAndGet();
            sb.append(num.toString());
        } else {
            sb.append(getId().toString());
        }
        return sb.toString();
    }
    
    public String getPath(){
        path = ItemUtils.makePath(this);
        return path;  
    }
    public void setPath(String path) {
        this.path = path;
    }    
    
    public List<P> getChildItems() {
        return childItems;
    }
    public void setChildItems(List<P> childItems) {
        this.childItems = childItems;
    }

    public List<L> getItemLogs() {
        if (itemLogs == null){
            itemLogs = new ArrayList<>();
        }
        return itemLogs;
    }
    public void setItemLogs(List<L> itemsLogs) {
        this.itemLogs = itemsLogs;
    }    
    
    /* Формирует строку ограниченной длинны из названия обекта, заканчивающуюся точками */
    public String getNameEndElipse(){
        return StringUtils.abbreviate(getName(), SysParams.LENGHT_NAME_ELIPSE);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getInheritsRightName(){
        inheritsRightName = ItemUtils.getMessageLabel("RightIsInherits");
        if (!isInherits()){
            inheritsRightName = ItemUtils.getMessageLabel("RightNotInherits"); 
        }
        return inheritsRightName;
    }
    
    public boolean isInherits() {
        return inherits;
    }
    public void setInherits(boolean inherits) {
        this.inherits = inherits;
    }

    public boolean isInheritsAccessChilds() {
        return inheritsAccessChilds;
    }
    public void setInheritsAccessChilds(boolean inheritsAccessDocs) {
        this.inheritsAccessChilds = inheritsAccessDocs;
    }
    
    public Rights getRightItem() {
        return rightItem;
    }
    public void setRightItem(Rights rightItem) {
        this.rightItem = rightItem;
    }

    public Integer getRightMask() { 
        return rightMask; 
    }
    public void setRightMask(Integer rightMask) { 
        this.rightMask = rightMask; 
    } 
    
    public Date getDateCreate() {
        return dateCreate;
    }
    public void setDateCreate(Date dateCreate) {        
        this.dateCreate = dateCreate;
    }
    
    public Date getDateChange() {
        return dateChange;
    }
    public void setDateChange(Date dateChange) {
        this.dateChange = dateChange;
    }
    
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public String getAccess() {
        return access;
    }
    public void setAccess(String access) {
        this.access = access;
    }    
    
    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public boolean isActual() {
        return actual;
    }
    public void setActual(boolean actual) {
        this.actual = actual;
    }    
    
    public O getOwner() {
        return owner;
    }
    public void setOwner(O owner) {
        this.owner = owner;
    }
    
    public P getParent() {
        return parent;
    }
    public void setParent(P parent) {
        this.parent = parent;
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

    public String getIconName() {
        if (owner != null){
            iconName = "doc16";
        } else {
            iconName = "folder_open";
        }
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }    
    
    /* Установка даты создания */  
    @PrePersist  
    public void setCreationDate() {  
        this.dateCreate = new Date(); 
        this.dateChange = new Date();
    }  
  
    /* Установка даты изменения */  
    @PreUpdate  
    public void setChangeDate() {  
        this.dateChange = new Date();  
    }  
}