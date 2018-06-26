package com.maxfill.model;

import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.states.BaseStateItem;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

/* Базовый класс справочников
 * @param <O> Класс владельца
 * @param <P> Класс родителя
 * @param <D> Класс подчинённых объектов
 * @param <L> Класс таблицы лога */
@MappedSuperclass
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public abstract class BaseDict<O extends BaseDict, P extends BaseDict, D extends BaseDict, L extends BaseLogItems, T extends BaseStateItem> implements Serializable, Dict {
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
            
    @Size(max = 255)
    @Column(name = "Name")
    private String name;        
        
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "DateCreate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreate;
        
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "DateChange")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateChange;
    
    /* собственные права объекта */
    @XmlTransient
    @Lob
    @Column(name = "Access", length = 1024)
    private byte[] access;
        
    /* дефолтные права доступа для подчинённых объектов в виде строки в xml */
    @XmlTransient
    @Lob
    @Column(name = "AccessChilds", length = 1024)
    private byte[] accessChild; 
    
    /* Признак наследования дефолтных прав для дочерних объектов */
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsInheritsAccessChilds")
    private boolean inheritsAccessChilds = true;   

    /* Состояние объекта */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false)
    private T state;   
    
    /* Журнал истории объекта  */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<L> itemLogs = new ArrayList<>();
      
    /* Дочерние child объекты, полученные по ссылке на parent */
    @XmlTransient
    @Transient    
    private List<P> childItems;    
        
    /* Подчинённые detail объекты, полученные или через ссылку на owner или через связь многие-ко-многим  */
    @XmlTransient
    @Transient
    private List<D> detailItems;
        
    /* Признак наследования прав объекта. 
     * TRUE - наследуются, FALSE - не наследуются (индивидуальные права)  */
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsInherits")
    private boolean inherits = true;    
        
    /* Признак того, что запись помечена на удаление */
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsDeleted")
    private boolean deleted = false;
        
    /* Признак того, что запись актуальная */
    @XmlTransient
    @Basic(optional = false)
    @Column(name = "IsActual")
    private boolean actual = true;        
    
    @XmlTransient
    @Column(name = "Number")
    private Integer number;            
    
    @XmlTransient
    @Column(name = "IconName")
    private String iconName;
        
    @Transient
    @XmlTransient           
    protected String iconTree;    
    
    @Transient
    @XmlTransient
    private Integer rightMask;  //маска доступа
    
    @Transient
    @XmlTransient
    private Rights rightItem; //права доступа в виде объекта    
        
    /* Возврашает путь в дереве к объекту */
    @Transient
    @XmlTransient
    private String path;          
    
    /* Права доступа к подчинённому объекту  */
    @Transient
    @XmlTransient
    private Rights rightForChild; //права для дочерних объектов 
    
    /* Роли объекта */
    @Transient
    @XmlTransient 
    protected Map<String, Set<Integer>> roles;
        
    public BaseDict(){}

    public String getFullName() {
        return name;
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

    /**
     * Формирует полный путь к объекту
     * @return 
     */
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

    public String getRoleJson() {
        return null;
    }
    public void setRoleJson(String roleJson) {
    }

    /* Формирует строку ограниченной длинны из названия обекта, заканчивающуюся точками */
    public String getNameEndElipse(){
        return StringUtils.abbreviate(getName(), SysParams.LENGHT_NAME_ELIPSE);
    }

    /**
     * Возвращает строку для отображения в заголовке карточки
     * @return
     */
    public String getCaption(){
        return getNameEndElipse();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public byte[] getAccess() {
        return access;
    }
    public void setAccess(byte[] access) {
        this.access = access;
    }

    public byte[] getAccessChild() {
        return accessChild;
    }
    public void setAccessChild(byte[] accessChild) {
        this.accessChild = accessChild;
    }      

    public T getState() {
        return state;
    }
    public void setState(T state) {
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
        if (iconName == null){
            iconName = "doc16";
        }
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    
    public Map<String, Set<Integer>> getRoles() {
        if (roles == null){
            roles = new HashMap<>();
        }
        return roles;
    }
    public void setRoles(Map<String, Set<Integer>> roles) {
        this.roles = roles;
    }

    /* установка (перезапись) одиночной роли */
    public void doSetSingleRole(String roleName, User user){
        Set<Integer> usersId = new HashSet<>();
        if (user != null){
            usersId.add(user.getId());
        }
        doSetMultyRole(roleName, usersId);
    }
    
    /* установка (перезапись) списковой роли */
    public void doSetMultyRole(String roleName, Set<Integer> usersId){
        getRoles().put(roleName, usersId);
    }

    /* добавление исполнителя в роль */
    public void doAddInRole(String roleName, Integer userId){
        Set<Integer> usersIds;
        if (getRoles().containsKey(roleName)){
            usersIds = getRoles().get(roleName);
        } else {
            usersIds = new HashSet<>();
        }
        usersIds.add(userId);
        getRoles().put(roleName, usersIds);
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