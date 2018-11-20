package com.maxfill.model.core.eventfeed;

import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.basedict.user.User;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class EventFeedFacade extends BaseLazyFacade<EventFeed>{
    
    public EventFeedFacade() {
        super(EventFeed.class);
    }

    public List<EventFeed> findLasts(){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root root = cq.from(itemClass);
        cq.select(root).orderBy(builder.desc(root.get("dateEvent")));
        Query query = em.createQuery(cq);
        query.setFirstResult(0);
        query.setMaxResults(200);
        return query.getResultList();
    }
    
    /**
     * Публикация события в ленте событий
     * @param eventName
     * @param icon
     * @param author
     */
    synchronized public void publicEventToFeed(String eventName, String icon, User author){
        EventFeed ef = new EventFeed();
        ef.setDateEvent(new Date());
        ef.setName(eventName);        
        ef.setAuthor(author);
        ef.setIconName(icon);
        create(ef);
    }
}