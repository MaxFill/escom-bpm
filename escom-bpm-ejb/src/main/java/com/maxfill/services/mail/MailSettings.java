package com.maxfill.services.mail;

import java.io.Serializable;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class MailSettings implements Serializable{    
    private static final long serialVersionUID = -2606513436608294285L;
    
    @XmlElement(name = "ServerAdress")
    private String serverAdress;
    
    @XmlElement(name = "User")
    private String user;
    
    @XmlElement(name = "Password")
    private String password;
    
    @XmlElement(name = "SmtpPort")
    private Integer smtpPort;
    
    @XmlElement(name = "AdressSender")
    private String adressSender;    
    
    public String getServerAdress() {
        return serverAdress;
    }
    public void setServerAdress(String serverAdress) {
        this.serverAdress = serverAdress;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Integer getSmtpPort() {
        return smtpPort;
    }
    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }
    public String getAdressSender() {
        return adressSender;
    }
    public void setAdressSender(String adressSender) {
        this.adressSender = adressSender;
    }
        
     //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
