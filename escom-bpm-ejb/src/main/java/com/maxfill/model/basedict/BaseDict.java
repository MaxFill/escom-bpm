package com.maxfill.model.basedict;

import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.user.User;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.Dict;
import com.maxfill.model.core.states.BaseStateItem;
import com.maxfill.utils.ItemUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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
    private String name = "";        
        
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
    protected String iconName;
        
    @Transient
    @XmlTransient           
    protected String iconTree = "ui-icon-folder-collapsed";    
    
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

    public String getCompanyName() {
        return "";
    }
    
    public String getPartnerName(){
        return "";
    }
    
    public String getTypeName(){
        return "";
    }
    
    private void makeFullRegNumber(StringBuilder sb, BaseDict item){        
        if (item.getParent() != null){
            makeFullRegNumber(sb, item.getParent());
            sb.append(".");           
        }
        if (item.getRegNumber() != null){
            sb.append(item.getRegNumber());
        }
    }     
    
    /**
     * Возвращает элемент верхнего уровня в ветке
     * @return 
     */
    public BaseDict getRoot(){
        return getRoot(this);
    }
    
    private BaseDict getRoot(BaseDict item){
        if (item.getParent() != null) {
            return getRoot(item.getParent());
        }
        return item;
    }
    
    /**
     * Возвращает короткое имя объекта
     * @return 
     */
    public String getShortName(){
        return getNameEndElipse();
    }
    
    /* Формирует строку ограниченной длинны из названия обекта, заканчивающуюся точками */
    public String getNameEndElipse(){
        return StringUtils.abbreviate(getName(), SysParams.LENGHT_NAME_ELIPSE);        
    }   
    
    /* *** GETS & SETS *** */
    
    public String getFullRegNumber(){
        StringBuilder sb = new StringBuilder("");
        makeFullRegNumber(sb, this);
        return sb.toString();
    };
    
    public String getRegNumber(){
        return "";
    }
    public void setRegNumber(String number){        
    }
    
    public Date getItemDate(){
        return null;
    }
    public void setItemDate(Date regDate){        
    }
    public String getStateName(){
        return getState() != null ? getState().getCurrentState().getName() : "";
    }
    
    public String getAuthorName(){
        return author != null ? author.getName() : "";
    }
    
    public String getLogin(){
        return "";
    }
    
    public String getEmail(){
        return "";
    }
    
    public String getEmployeeFIO(){
        return "";
    }
    
    public String getPostName(){
        return "";
    }
    
    public Date getPlanExecDate(){
        return null;
    }
    
    public Date getBeginDate(){
        return null;
    }
    
    public Date getEndDate(){
        return null;
    }
    
    public String getCode(){
        return "";
    }
    
    public String getFullName() {
        return name;
    }

    public String getCuratorName(){
        return "";
    }

    public String getStatusName(){
       return ""; 
    }
    public void setStatusName(String statusName){        
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

    public String getIconTree() {
        return iconTree;
    }
    public void setIconTree(String iconTree) {
        this.iconTree = iconTree;
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

    public String getRoleJson() {
        return null;
    }
    public void setRoleJson(String roleJson) {
    } 
     
    /**
     * Возвращает строку для отображения в заголовке карточки
     * @return
     */
    public String getCaption(){
        if (getNameEndElipse() == null) return "";
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
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
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

    /* *** РОЛИ *** */
    
    public void clearRole(String roleName){
        doSetMultyRole(roleName.toUpperCase(), new HashSet<>());
    }
    
    /* установка (перезапись) одиночной роли */
    public void doSetSingleRole(String roleName, User user){
        if (user == null) return;
        doSetSingleRole(roleName, user.getId());
    }
    public void doSetSingleRole(String roleName, Integer userId){
        if (userId == null) return;
        
        Set<Integer> usersIds = new HashSet<>();
        usersIds.add(userId);

        doSetMultyRole(roleName.toUpperCase(), usersIds);
    }
    
    /* установка (перезапись) списковой роли */
    public void doSetMultyRole(String roleName, Set<Integer> usersId){
        getRoles().put(roleName.toUpperCase(), usersId);
    }

    /* добавление исполнителя в роль */
    public void addUserInRole(String roleName, User user){
        if (user == null) return;
        doAddInRole(roleName, user.getId());
    }
    
    public void doAddInRole(String roleName, Integer userId){
        Set<Integer> usersIds;
        roleName = roleName.toUpperCase();
        
        if (getRoles().containsKey(roleName)){
            usersIds = getRoles().get(roleName);
        } else {
            usersIds = new HashSet<>();
        }
        usersIds.add(userId);
        getRoles().put(roleName, usersIds);
    }
    
    /* *** *** */
    
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