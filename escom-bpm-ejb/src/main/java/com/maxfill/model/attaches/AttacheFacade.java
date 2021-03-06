package com.maxfill.model.attaches;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.attaches.Attaches_;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Фасад для сущности "Вложения" */
@Stateless
public class AttacheFacade extends BaseFacade<Attaches>{    
    
    public AttacheFacade() {
        super(Attaches.class);
    }
    
    /* Находит текущую версию вложения для документа */
    public Attaches findCurrentAttacheByDoc(Doc doc){
        em.getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> c = cq.from(Attaches.class);        
        Predicate crit1 = builder.equal(c.get("doc"), doc);
        Predicate crit2 = builder.equal(c.get("current"), true);
        cq.select(c).where(builder.and(crit1, crit2));
        TypedQuery<Attaches> q = em.createQuery(cq);       
        return q.getSingleResult(); 
    }
    
    public Integer countLockAttachesByDoc(Doc doc){
        em.getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq= builder.createQuery();
        Root<Attaches> root = cq.from(Attaches.class);       
        Predicate crit1 = builder.equal(root.get(Attaches_.doc), doc);
        Predicate crit2 = builder.isNotNull(root.get(Attaches_.lockDate));
        cq.select(builder.count(root));
        cq.where(builder.and(crit1, crit2));        
        Query query = em.createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }
    
    public List findAttachesByGUID(String guid){
        List results = em.createNamedQuery("Attaches.findByGUID")
            .setParameter("paramGUID", guid)
            .getResultList();        
        return results;
    }
    
    public Stream<Attaches> findAttachesByDoc(Doc doc){
        em.getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> root = cq.from(Attaches.class);        
        Predicate crit1 = builder.equal(root.get(Attaches_.doc), doc);
        cq.select(root).where(builder.and(crit1));
        Query q = em.createQuery(cq);       
        return q.getResultStream();
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
    
    /**
     * Добавить версию в документ
     * @param doc
     * @param attache 
     */
    public void addAttacheInDoc(Doc doc, Attaches attache){
        Integer version = doc.getNextVersionNumber();
        attache.setNumber(version);
        attache.setDoc(doc);        
        doc.getAttachesList().add(attache);                
    }
    
    /**
     * Сделать версию основной
     * @param doc
     * @param attache 
     */
    public void setMainAttache(Doc doc, Attaches attache){
        doc.getAttachesList().stream()
                .filter(attacheVersion -> attacheVersion.getCurrent())
                .forEach(attacheVersion -> attacheVersion.setCurrent(false));
        attache.setCurrent(Boolean.TRUE);
    }
    
    /**
     * Возвращает колво записей, в котороых пользователь является автором
     * @param user
     * @return 
     */
    public Long findCountUserLinks(User user){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root root = cq.from(itemClass);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("author"), user));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    public Long sumFilesSize(){        
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root root = cq.from(Attaches.class);        
        cq.select(builder.sum(root.get(Attaches_.size)));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
}
