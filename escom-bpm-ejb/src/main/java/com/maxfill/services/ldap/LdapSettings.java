package com.maxfill.services.ldap;

import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс параметров службы интеграции с LDAP
 * @author mfilatov
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class LdapSettings implements Serializable{
    private static final long serialVersionUID = 2025544315308424754L;
    
    @XmlElement(name = "LdapSearchBase")
    private String ldapSearchBase = "CN=Users, DC=my-escom, DC=ru";
    
    @XmlElement(name = "LdapSearcheGroup")
    private String ldapSearcheGroup = "CN=ESCOM_GROUP";
    
    @XmlElement(name = "LdapUsername")
    private String ldapUsername = "my-escom\\escom";
    
    @XmlElement(name = "LdapAdServer")
    private String ldapAdServer = "ldap://192.168.0.100:389";
    
    @XmlElement(name = "LdapPassword")
    private String ldapPassword = "";

    @XmlElement(name = "UpdateUsers")
    private Boolean updateUsers = false;
    
    public LdapSettings() {
    }    
    
    public String getLdapSearchBase() {
        return ldapSearchBase;
    }

    public void setLdapSearchBase(String ldapSearchBase) {
        this.ldapSearchBase = ldapSearchBase;
    }

    public String getLdapUsername() {
        return ldapUsername;
    }

    public void setLdapUsername(String ldapUsername) {
        this.ldapUsername = ldapUsername;
    }

    public String getLdapAdServer() {
        return ldapAdServer;
    }

    public void setLdapAdServer(String ldapAdServer) {
        this.ldapAdServer = ldapAdServer;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }

    public String getLdapSearcheGroup() {
        return ldapSearcheGroup;
    }

    public void setLdapSearcheGroup(String ldapSearcheGroup) {
        this.ldapSearcheGroup = ldapSearcheGroup;
    }

    public Boolean getUpdateUsers() {
        return updateUsers;
    }

    public void setUpdateUsers(Boolean updateUsers) {
        this.updateUsers = updateUsers;
    }
    
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
