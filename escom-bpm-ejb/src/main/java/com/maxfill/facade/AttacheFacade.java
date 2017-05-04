
package com.maxfill.facade;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.facade.BaseFacade;
import com.maxfill.model.docs.Doc;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Вложения
 * @author mfilatov
 */
@Stateless
public class AttacheFacade extends BaseFacade<Attaches>{
    
    @Override
    public void remove(Attaches entity){
        entity = getEntityManager().getReference(Attaches.class, entity.getId());
        getEntityManager().remove(entity);
    }
    
    public AttacheFacade() {
        super(Attaches.class);
    }
 
    /**
     * Отбирает текущую версию вложения для документа
     * @param doc
     * @return 
     */
    public Attaches findCurrentAttacheByDoc(Doc doc){
        getEntityManager().getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> c = cq.from(Attaches.class);        
        Predicate crit1 = builder.equal(c.get("doc"), doc);
        Predicate crit2 = builder.equal(c.get("current"), true);
        cq.select(c).where(builder.and(crit1, crit2));
        TypedQuery<Attaches> q = getEntityManager().createQuery(cq);       
        return q.getSingleResult(); 
    }
}
