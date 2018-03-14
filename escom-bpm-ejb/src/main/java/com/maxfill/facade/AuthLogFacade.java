package com.maxfill.facade;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.model.authlog.Authlog;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/* Фасад таблицы журнала аутентификации пользователей Authlog */
@Stateless
public class AuthLogFacade extends BaseFacade<Authlog>{
    public AuthLogFacade() {
        super(Authlog.class);
    }

    public void addAuthEnter(String login, HttpServletRequest request, boolean isSmsSend){
        addEvent(login, request, DictLogEvents.ENTER_EVENT, isSmsSend);
    }

    public void addAuthExit(String login, HttpServletRequest request){
        addEvent(login, request, DictLogEvents.EXIT_EVENT, false);
    }

    private void addEvent(String login, HttpServletRequest request, String event, boolean isSmsSend){
        String ip = request.getRemoteAddr();
        if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
                String ipAddress = inetAddress.getHostAddress();
                ip = ipAddress;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        Authlog authlog = new Authlog();
        authlog.setLogin(login);
        authlog.setIpAdress(ip);
        authlog.setDateEvent(new Date());
        authlog.setEventName(event);
        authlog.setSendSMS(isSmsSend);
        create(authlog);
    }
}
