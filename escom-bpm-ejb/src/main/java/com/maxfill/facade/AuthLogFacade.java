package com.maxfill.facade;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.model.authlog.Authlog_;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/* Фасад таблицы журнала аутентификации пользователей Authlog */
@Stateless
public class AuthLogFacade extends BaseFacade<Authlog>{
    public AuthLogFacade() {
        super(Authlog.class);
    }

    /**
     * Загрузка событий атентификации за определённый период времени
     * @param startDate
     * @param endDate
     * @param firstPosition
     * @param numberOfRecords
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    public List<Authlog> findEventsByPeriod(Date startDate, Date endDate, int firstPosition, int numberOfRecords, String sortField, String sortOrder, Map<String,Object> filters) {
        getEntityManager().getEntityManagerFactory().getCache().evict(Authlog.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Authlog> cq = builder.createQuery(Authlog.class);
        Root<Authlog> root = cq.from(Authlog.class);
        cq.select(root).where(builder.and(makePredicates(builder, root, startDate, endDate, filters)));
        if (StringUtils.isNotBlank(sortField)){ //если задано по какому полю сортировать
            if (StringUtils.isBlank(sortOrder) || !sortOrder.equals("DESCENDING")) {
                cq.orderBy(builder.asc(root.get(sortField)));
            } else {
                cq.orderBy(builder.desc(root.get(sortField)));
            }
        }
        Query query = getEntityManager().createQuery(cq);
        query.setFirstResult(firstPosition);
        query.setMaxResults(numberOfRecords);
        return query.getResultList();
    }

    /**
     * Возвращает число записей журнала в заданном диаппазоне
     * @param startDate
     * @param endDate
     * @param filters
     * @return
     */
    public int countEvents(Date startDate, Date endDate, Map<String,Object> filters){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<Authlog> root = cq.from(Authlog.class);
        cq.select(builder.count(root)).where(builder.and(makePredicates(builder, root, startDate, endDate, filters)));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Формирование условий для запросов
     * @param builder
     * @param root
     * @param startDate
     * @param endDate
     * @param filters
     * @return
     */
    private Predicate[] makePredicates(CriteriaBuilder builder, Root root, Date startDate, Date endDate, Map<String,Object> filters) {
        List <Predicate> criteries = new ArrayList <>();
        criteries.add(builder.between(root.get(Authlog_.dateEvent), startDate, endDate));

        if(filters != null) {
            for(Iterator <String> it = filters.keySet().iterator(); it.hasNext(); ) {
                String filterProperty = it.next();
                Object filterValue = filters.get(filterProperty);
                criteries.add(builder.equal(root.get(filterProperty), filterValue));
             }
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return predicates;
     }

    /**
     * Очистка журнала за указанный период времени
     * @param startDate
     * @param endDate
     * @param filters
     */
    public int clearEvents(Date startDate, Date endDate, Map<String,Object> filters) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Authlog> cd = builder.createCriteriaDelete(Authlog.class);
        Root root = cd.from(Authlog.class);
        cd.where(makePredicates(builder, root, startDate, endDate, filters));
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
