
package com.maxfill.services.ldap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Пользователи, для загрузки из LDAP
 * @author mfilatov
 */
public class LdapUsers implements Serializable{    
    private static final long serialVersionUID = -3436066172829571761L;
    
    private static final AtomicInteger NUMBER_ID = new AtomicInteger(0);
    private Integer id;
    private String name;
    private String sAMAccountName;
    private String login;
    private String primaryGroupName;
    private String post;
    private String departament;
    private String company;
    private String distinguishedName;
    private String userPrincipalName;
    private String mail;
    private String phone;
    private List<String> groups = new ArrayList<>();
    
    public LdapUsers() {
        id = NUMBER_ID.incrementAndGet();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }
    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getsAMAccountName() {
        return sAMAccountName;
    }
    public void setsAMAccountName(String sAMAccountName) {
        this.sAMAccountName = sAMAccountName;
    }

    public String getPost() {
        return post;
    }
    public void setPost(String post) {
        this.post = post;
    }

    public String getDepartament() {
        return departament;
    }
    public void setDepartament(String departament) {
        this.departament = departament;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }
    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrimaryGroupName() {
        return primaryGroupName;
    }
    public void setPrimaryGroupName(String primaryGroupName) {
        this.primaryGroupName = primaryGroupName;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
    
}
