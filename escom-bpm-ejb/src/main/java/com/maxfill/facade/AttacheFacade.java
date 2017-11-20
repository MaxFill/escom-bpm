
package com.maxfill.facade;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.attaches.Attaches_;
import com.maxfill.model.docs.Doc;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Вложения */
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
 
    /* Находит текущую версию вложения для документа */
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
    
    public Integer countLockAttachesByDoc(Doc doc){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq= builder.createQuery();
        Root<Attaches> root = cq.from(Attaches.class);       
        Predicate crit1 = builder.equal(root.get(Attaches_.doc), doc);
        Predicate crit2 = builder.isNotNull(root.get(Attaches_.lockDate));
        cq.select(builder.count(root));
        cq.where(builder.and(crit1, crit2));        
        Query query = getEntityManager().createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }
    
    public List<Attaches> findAttachesByDoc(Doc doc){
        getEntityManager().getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> c = cq.from(Attaches.class);        
        Predicate crit1 = builder.equal(c.get("doc"), doc);
        cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    public Attaches copyAttache(Attaches sourceAttache){
        Attaches newAttache = new Attaches();        
        newAttache.setName(sourceAttache.getName());
        newAttache.setExtension(sourceAttache.getExtension());
        newAttache.setType(sourceAttache.getType());
        newAttache.setSize(sourceAttache.getSize());
        newAttache.setAuthor(sourceAttache.getAuthor());
        newAttache.setDateCreate(new Date());
        return newAttache;
    }
    
    public void addAttacheInDoc(Doc doc, Attaches attache){
        Integer version = doc.getNextVersionNumber();
        attache.setNumber(version);
        attache.setDoc(doc);
        attache.setCurrent(Boolean.TRUE);
        List<Attaches> attaches = doc.getAttachesList();
        attaches.stream()
                .filter(attacheVersion -> attacheVersion.getCurrent())
                .forEach(attacheVersion -> attacheVersion.setCurrent(false));
        doc.getAttachesList().add(attache);                
    }
}
