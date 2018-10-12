package com.maxfill.services.ldap;

import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.post.Post;
import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Параметры службы интеграции с LDAP
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
    
    @XmlElement(name = "companyId")
    private Integer companyId;
    
    @XmlElement(name = "departmentId")
    private Integer departmentId;
    
    @XmlElement(name = "postId")
    private Integer postId;
    
    @XmlElement(name = "detailLog")
    private boolean createDetailLogFile;
            
    @XmlTransient
    private Company company;
    
    @XmlTransient
    private Department department;
    
    @XmlTransient
    private Post post;
    
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

    public Post getPost() {
        return post;
    }
    public void setPost(Post post) {
        this.post = post;
    }
    
    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getCompanyId() {
        return companyId;
    }
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isCreateDetailLogFile() {
        return createDetailLogFile;
    }
    public void setCreateDetailLogFile(boolean createDetailLogFile) {
        this.createDetailLogFile = createDetailLogFile;
    }
    
    public Integer getPostId() {
        return postId;
    }
    public void setPostId(Integer postId) {
        this.postId = postId;
    }
    
    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
