package com.maxfill.model.core.metadates;

import com.maxfill.facade.BaseFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Stateless
public class MetadatesFacade extends BaseFacade<Metadates>{
    
    public MetadatesFacade() {
        super(Metadates.class);
    }
    
    public List<Metadates> findAll() {                        
        em.getEntityManagerFactory().getCache().evict(Metadates.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Metadates> cq = builder.createQuery(Metadates.class);
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }

    /**
     * Возвращает объект метаданных по его имени
     * @param objectName
     * @return
     */
    public Metadates findByObjectName(String objectName) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Metadates> cq = builder.createQuery(Metadates.class);
        Root<Metadates> root = cq.from(Metadates.class);
        Predicate crit1 = builder.equal(root.get(Metadates_.objectName), objectName);
        cq.select(root).where(builder.and(crit1));
        TypedQuery<Metadates> query = em.createQuery(cq);
        return query.getSingleResult();
    }
}
