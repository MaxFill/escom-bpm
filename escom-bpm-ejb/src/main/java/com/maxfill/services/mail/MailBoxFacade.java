package com.maxfill.services.mail;

import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Stateless
public class MailBoxFacade extends BaseLazyFacade<Mailbox>{
    
    public MailBoxFacade() {
        super(Mailbox.class);
    }

    public List<Mailbox> findAll() {
        em.getEntityManagerFactory().getCache().evict(Mailbox.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Mailbox> cq = builder.createQuery(Mailbox.class);
        Root<Mailbox> c = cq.from(Mailbox.class);        
        Predicate crit1 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1));
        Query q = em.createQuery(cq);       
        return q.getResultList(); 
    }
    
    public void createMailBox(String subject, String adresses, String sender, String content, String authorName){
        try {
            Mailbox mailbox = new Mailbox();
            mailbox.setSubject(subject);
            mailbox.setAddresses(adresses);
            mailbox.setSender(sender);
            byte[] compressXML = EscomUtils.compress(content);
            mailbox.setMsgContent(compressXML);
            mailbox.setActual(true);
            mailbox.setCopies("");
            mailbox.setDateCreate(new Date());
            mailbox.setAuthorName(authorName);
            create(mailbox);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}