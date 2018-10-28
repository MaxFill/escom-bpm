package com.maxfill.model.basedict.userGroups;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.*;

import static javax.persistence.GenerationType.TABLE;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Класс сущности "Группы пользователей"
 */
@Entity
@Table(name = "usersGroups")
@DiscriminatorColumn(name="REF_TYPE")
public class UserGroups extends BaseDict<UserGroups, UserGroups, User, UserGroupsLog, UserGroupsStates>{
    private static final long serialVersionUID = 9082349235115998647L;
    
    @TableGenerator(
        name="groupUsIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="GROUP_US_ID", allocationSize = 1)
     
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="groupUsIdGen")
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Parent", referencedColumnName = "Id")
    private UserGroups parent;

    @Column(name = "TypeActualize")
    private Integer typeActualize = 0;

    @Column(name = "RoleFieldName")
    private String roleFieldName;       

    @JoinTable(name = "usersInGroup", 
            joinColumns = {@JoinColumn(name = "GroupId", referencedColumnName = "Id")}, 
            inverseJoinColumns = {@JoinColumn(name = "UserId", referencedColumnName = "Id")}
    )
    @ManyToMany(fetch = FetchType.EAGER)
    private List<User> usersList = new ArrayList<>();
    
    @OneToMany
    @JoinColumn(name = "parent")
    private List<UserGroups> childItems;
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private UserGroupsStates state;
        
    @Transient
    @XmlTransient
    private Integer tempId;
        
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    public UserGroups() {
        tempId = COUNT.incrementAndGet();
    }

    @Override
    public List<UserGroups> getChildItems() {
        return childItems;
    }
    @Override
    public void setChildItems(List<UserGroups> childItems) {
        this.childItems = childItems;
    }
    
    public Integer getTempId() {
        return tempId;
    }
    public void setTempId(Integer tempId) {
        this.tempId = tempId;
    }

    @Override
    public UserGroups getParent() {
        return parent;
    }
    @Override
    public void setParent(UserGroups parent) {
        this.parent = parent;
    }

    @Override
    public UserGroupsStates getState() {
        return state;
    }
    @Override
    public void setState(UserGroupsStates state) {
        this.state = state;
    }

    public List<User> getUsersList() {
        return usersList;
    }
    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }
    
    @Override
    public List<User> getDetailItems() {
        return usersList;
    }
    
    @Override
    public void setDetailItems(List<User> users) {
        usersList = users;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTypeActualize() {
        return typeActualize;
    }
    public void setTypeActualize(Integer typeActualize) {
        this.typeActualize = typeActualize;
    }

    public String getRoleFieldName() {
        return roleFieldName;
    }
    public void setRoleFieldName(String roleFieldName) {
        this.roleFieldName = roleFieldName;
    }
    
    /* *** *** */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserGroups)) {
            return false;
        }
        UserGroups other = (UserGroups) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UsersGroups[ id=" + id + " ] " + getName();
    }

    }
