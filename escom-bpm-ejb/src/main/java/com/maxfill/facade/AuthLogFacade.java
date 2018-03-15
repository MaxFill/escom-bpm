package com.maxfill.facade;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.model.authlog.Authlog;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

/* Фасад таблицы журнала аутентификации пользователей Authlog */
@Stateless
public class AuthLogFacade extends BaseFacade<Authlog>{
    public AuthLogFacade() {
        super(Authlog.class);
    }

    @Override
    public List<Authlog> findAll() {
        getEntityManager().getEntityManagerFactory().getCache().evict(Authlog.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Authlog> cq = builder.createQuery(Authlog.class);
        Root<Authlog> c = cq.from(Authlog.class);
        //Predicate crit1 = builder.equal(c.get("actual"), true);
        //cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq.select(c));
        return q.getResultList();
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
