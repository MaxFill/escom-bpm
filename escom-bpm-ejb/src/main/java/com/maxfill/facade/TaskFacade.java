package com.maxfill.facade;

import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.process.schemes.task.Task_;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Поручения"
 */
@Stateless
public class TaskFacade extends BaseLazyLoadFacade<Task>{

    public TaskFacade() {
        super(Task.class);
    }

    public Task findByLinkUID(String linkUID){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> c = cq.from(Task.class);        
        Predicate crit1 = builder.equal(c.get(Task_.taskLinkUID), linkUID);        
        cq.select(c).where(builder.and(crit1));
        TypedQuery<Task> q = getEntityManager().createQuery(cq);       
        return q.getSingleResult(); 
    }
}
