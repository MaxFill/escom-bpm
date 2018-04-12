package com.maxfill.model.users.sessions;

import com.maxfill.model.users.User;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpSession;

/**
 *
 * @author mfilatov
 */
public class UsersSessions implements Serializable{    
    private static final long serialVersionUID = -536636104437077961L;
    private static final AtomicInteger NUMBER_ID = new AtomicInteger(0);
    
    private final Integer id;
    private User user;
    private Date dateConnect;
    private String ipAdress;
    private HttpSession httpSession;

    public UsersSessions() {
        id = NUMBER_ID.incrementAndGet();
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateConnect() {
        return dateConnect;
    }
    public void setDateConnect(Date dateConnect) {
        this.dateConnect = dateConnect;
    }

    public String getIpAdress() {
        return ipAdress;
    }
    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }
    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public Integer getId() {
        return id;
    }
        
}
