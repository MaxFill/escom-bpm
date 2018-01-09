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

    @XmlElement(name = "DeleteAfterLoad")
    private Boolean deleteAfterLoad = true;

    @XmlElement(name = "DeleteIfUnknownSender")
    private Boolean deleteIfUnknownSender = true;

    @XmlElement(name = "ReadOnlyNewMessages")
    private Boolean readOnlyNewMessages = true;

    /* gets & sets */

    public Boolean getReadOnlyNewMessages() {
        return readOnlyNewMessages;
    }
    public void setReadOnlyNewMessages(Boolean readOnlyNewMessages) {
        this.readOnlyNewMessages = readOnlyNewMessages;
    }

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

    public Boolean getDeleteAfterLoad() {
        return deleteAfterLoad;
    }
    public void setDeleteAfterLoad(Boolean deleteAfterLoad) {
        this.deleteAfterLoad = deleteAfterLoad;
    }

    public Boolean getDeleteIfUnknownSender() {
        return deleteIfUnknownSender;
    }
    public void setDeleteIfUnknownSender(Boolean deleteIfUnknownSender) {
        this.deleteIfUnknownSender = deleteIfUnknownSender;
    }

    //трансформирует данные класса в xml строку
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}
