package com.maxfill.facade;

import com.maxfill.services.mail.Mailbox;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * EJB bean для почтового ящика
 * @author Maxim
 */
@Stateless
public class MailBoxFacade extends BaseFacade<Mailbox> {

    public MailBoxFacade() {
        super(Mailbox.class);
    }

    @Override
    public List<Mailbox> findAll() {
        getEntityManager().getEntityManagerFactory().getCache().evict(Mailbox.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Mailbox> cq = builder.createQuery(Mailbox.class);
        Root<Mailbox> c = cq.from(Mailbox.class);        
        Predicate crit1 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }

    @Override
    public void remove(Mailbox message) {
        message = getEntityManager().getReference(Mailbox.class, message.getId());
        getEntityManager().remove(message);
    }
}