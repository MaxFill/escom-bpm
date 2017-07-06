package com.maxfill.model.users;

import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.model.users.groups.UserGroups;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/* Пользователи */
@Entity
@Table(name = "users")
@DiscriminatorColumn(name="REF_TYPE")
public class User extends BaseDict<UserGroups, User, User, UserLog>{    
    private static final long serialVersionUID = 9082349804115998647L;

    @TableGenerator(
        name="usersIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="USERS_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="usersIdGen")
    @Column(name = "Id")
    private Integer id;
    
    @Column(name = "Login")
    private String login;
    
    @Column(name = "Password")
    private String password;

    /* Имя */
    @Column(name = "FirstName")
    private String firstName = "";

    /* Фамилия */
    @Column(name = "SecondName")
    private String secondName = "";

    /* Отчество */
    @Column(name = "LastName")
    private String lastName = "";
    
    @Column(name = "Phone")
    private String phone;
    
    @Column(name = "Email")
    @Size(max = 50)
    private String email;
    
    @Column(name = "Gender")
    private Short gender;
    
    @Column(name = "LDAPname")
    private String LDAPname;
    
    @Lob
    @Column(name = "Settings", length = 1024)
    private byte[] userSettings; 
     
    @Column(name = "EmailSign", length = 2048)
    private String emailSign;
    
    @Column(name = "DuplicateMessagesEmail")
    private boolean duplicateMessagesEmail = true;
            
    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "userId")
    private List<FavoriteObj> favoriteObjList;        
    
    @JoinTable(name = "usersInGroup", joinColumns = {
        @JoinColumn(name = "UserId", referencedColumnName = "Id")}, inverseJoinColumns = {
        @JoinColumn(name = "GroupId", referencedColumnName = "Id")})
    @ManyToMany
    private List<UserGroups> usersGroupsList = new ArrayList<>();
               
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "employee")
    private List<Staff> staffsList;            
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<UserLog> itemLogs = new ArrayList<>();
        
    //возвращает сокращённое ФИО сотрудника
    @XmlTransient
    public String getShortFIO(){       
        String f = "";
        String l = "";
        String s = "";
        if (!lastName.isEmpty()){
            l = lastName.substring(0, 1);
        }
        if (!secondName.isEmpty()){
            f = secondName;
        }
        if (!firstName.isEmpty()){
            s = firstName.substring(0, 1);
        }
        
        return String.format("%s %s.%s.", f,s,l); 
    }
    
    @Override
    public List<UserLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<UserLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    @Override
    public String getIconName() {
        return "user";
    }
    
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Short getGender() {
        return gender;
    }
    public void setGender(Short gender) {
        this.gender = gender;
    }

    public List<Staff> getStaffsList() {
        return staffsList;
    }
    public void setStaffsList(List<Staff> staffsList) {
        this.staffsList = staffsList;
    }

    public List<UserGroups> getUsersGroupsList() {
        return usersGroupsList;
    }
    public void setUsersGroupsList(List<UserGroups> usersGroupsList) {
        this.usersGroupsList = usersGroupsList;
    }   

    public byte[] getUserSettings() {
        return userSettings;
    }
    public void setUserSettings(byte[] userSettings) {
        this.userSettings = userSettings;
    }  

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getLDAPname() {
        return LDAPname;
    }
    public void setLDAPname(String LDAPname) {
        this.LDAPname = LDAPname;
    }

    public String getEmailSign() {
        return emailSign;
    }
    public void setEmailSign(String emailSign) {
        this.emailSign = emailSign;
    }
    
    public List<FavoriteObj> getFavoriteObjList() {
        return favoriteObjList;
    }
    public void setFavoriteObjList(List<FavoriteObj> favoriteObjList) {
        this.favoriteObjList = favoriteObjList;
    }  

    public boolean isDuplicateMessagesEmail() {
        return duplicateMessagesEmail;
    }
    public void setDuplicateMessagesEmail(boolean duplicateMessagesEmail) {
        this.duplicateMessagesEmail = duplicateMessagesEmail;
    }
        
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
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
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.users.Users[ id=" + id + " ]";
    }

}
