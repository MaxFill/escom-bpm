package com.maxfill.facade;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.model.authlog.Authlog_;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.*;
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

    /**
     * Отбирает события атентификации за определённый период времени
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Authlog> findEventsByPeriod(Date startDate, Date endDate) {
        getEntityManager().getEntityManagerFactory().getCache().evict(Authlog.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Authlog> cq = builder.createQuery(Authlog.class);
        Root<Authlog> root = cq.from(Authlog.class);
        Predicate predicate = builder.between(root.get(Authlog_.dateEvent), startDate, endDate);
        cq.select(root).where(builder.and(predicate));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Очистка журнала за указанный период времени
     * @param startDate
     * @param endDate
     */
    public int clearEvents(Date startDate, Date endDate) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Authlog> cd = builder.createCriteriaDelete(Authlog.class);
        Root root = cd.from(Authlog.class);
        Predicate predicate = builder.between(root.get(Authlog_.dateEvent), startDate, endDate);
        cd.where(predicate);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
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
