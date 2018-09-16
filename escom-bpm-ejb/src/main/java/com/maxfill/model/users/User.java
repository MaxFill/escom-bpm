package com.maxfill.model.users;

import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.model.users.assistants.Assistant;
import com.maxfill.model.users.groups.UserGroups;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.*;
import static javax.persistence.GenerationType.TABLE;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Сущность "Пользователь"
 */
@Entity
@Table(name = "users")
@DiscriminatorColumn(name="REF_TYPE")
public class User extends BaseDict<UserGroups, User, User, UserLog, UserStates>{    
    private static final long serialVersionUID = 9082349804115998647L;

    @TableGenerator(
        name="usersIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="USERS_ID", allocationSize = 1)
        
    @Id
    @Basic(optional = false)
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

    @Column(name = "MobilePhone")
    private String mobilePhone;
    
    @Column(name = "Email")
    @Size(max = 50)
    private String email;
    
    @Column(name = "Gender")
    private Short gender;
    
    @Column(name = "LDAPname")
    private String LDAPname;

    @JoinColumn(name = "Inbox", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Folder inbox;

    @Lob
    @Column(name = "Settings", length = 1024)
    private byte[] userSettings; 
     
    @Column(name = "EmailSign", length = 2048)
    private String emailSign;
    
    @Column(name = "Locale")
    private String locale;
    
    @Column(name = "DuplicateMessagesEmail")
    private boolean duplicateMessagesEmail = true;

    @Column(name = "DoubleFactorAuth")
    private boolean doubleFactorAuth;

    @Column(name = "NeedChangePwl")
    private boolean needChangePwl = true;

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "userId")
    private List<FavoriteObj> favoriteObjList;        
    
    @JoinTable(name = "usersInGroup", 
            joinColumns = {@JoinColumn(name = "UserId", referencedColumnName = "Id")}, 
            inverseJoinColumns = {@JoinColumn(name = "GroupId", referencedColumnName = "Id")}
    )
    @ManyToMany
    private List<UserGroups> usersGroupsList = new ArrayList<>();
              
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", orphanRemoval=true)
    private List<Assistant> assistants = new ArrayList<>();               
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Assistant> chiefs = new ArrayList<>();
     
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<UserLog> itemLogs = new ArrayList<>();
        
    /* Штатная единица */
    @OneToOne(optional = false)
    @JoinColumn(name = "Staff", referencedColumnName = "Id")
    private Staff staff;
     
    /* Состояние */
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private UserStates state;
    
    @Transient
    private String pwl; 
    
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    @Transient
    @XmlTransient
    private Integer tempId;

    public User() {
        tempId = COUNT.incrementAndGet();
    }
    
    /* GETS & SETS */    

    @Override
    public String getFullName() {
        if (staff != null){
            return staff.getNameEndElipse();
        } else {
            return super.getFullName(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
        
    @Override
    public UserStates getState() {
        return state;
    }
    @Override
    public void setState(UserStates state) {
        this.state = state;
    }
        
    /** 
     * Формирует сокращённое ФИО сотрудника
     * @return 
     */ 
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

    /** 
     * Формирует сокращённое ФИО сотрудника
     * @return 
     */ 
    @XmlTransient
    public String getFullFIO(){
        StringBuilder sb = new StringBuilder();
        if (secondName != null){
            sb.append(secondName).append((" "));
        }
        if (firstName != null){
            sb.append(firstName).append((" "));
        }
        if (lastName != null){
            sb.append(lastName);
        }
        return sb.toString();
    }
    
    public Integer getTempId() {
        return tempId;
    }
    public void setTempId(Integer tempId) {
        this.tempId = tempId;
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

    public boolean isNeedChangePwl() {
        return needChangePwl;
    }
    public void setNeedChangePwl(boolean needChangePwl) {
        this.needChangePwl = needChangePwl;
    }

    public Staff getStaff() {
        return staff;
    }
    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public List<Assistant> getChiefs() {
        return chiefs;
    }
    public void setChiefs(List<Assistant> chiefs) {
        this.chiefs = chiefs;
    }
    
    public Folder getInbox() {
        return inbox;
    }
    public void setInbox(Folder inbox) {
        this.inbox = inbox;
    }

    public String getPwl() {
        return pwl;
    }
    public void setPwl(String pwl) {
        this.pwl = pwl;
    }

    @Override
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

    public List<UserGroups> getUsersGroupsList() {
        return usersGroupsList;
    }
    public void setUsersGroupsList(List<UserGroups> usersGroupsList) {
        this.usersGroupsList = usersGroupsList;
    }

    public List<Assistant> getAssistants() {
        return assistants;
    }
    public void setAssistants(List<Assistant> assistants) {
        this.assistants = assistants;
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

    public String getMobilePhone() {
        return mobilePhone;
    }
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    @Override
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

    public boolean isDoubleFactorAuth() {
        return doubleFactorAuth;
    }
    public void setDoubleFactorAuth(boolean doubleFactorAuth) {
        this.doubleFactorAuth = doubleFactorAuth;
    }

    @Override
    public List<User> getDetailItems() {
        return null;
    }
    
    @Override
    public List<User> getChildItems() {
        return null;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
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
        return "User[ id=" + id + " ] [" + getName() + "]";
    }

}
