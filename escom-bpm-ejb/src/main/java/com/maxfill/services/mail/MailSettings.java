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
    
    @XmlElement(name = "Port")
    private Integer port;
    
    @XmlElement(name = "AdressSender")
    private String adressSender;

    @XmlElement(name = "Encoding")
    private String encoding;

    @XmlElement(name = "UseSSL")
    private Boolean useSSL;

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

    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAdressSender() {
        return adressSender;
    }
    public void setAdressSender(String adressSender) {
        this.adressSender = adressSender;
    }

    public Boolean getUseSSL() {
        return useSSL;
    }
    public void setUseSSL(Boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
