package com.maxfill.facade;

import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.metadates.Metadates_;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Stateless
public class MetadatesFacade extends BaseFacade<Metadates> {
    
    public MetadatesFacade() {
        super(Metadates.class);
    }

    @Override
    public List<Metadates> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(Metadates.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Metadates> cq = builder.createQuery(Metadates.class);
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }

    @Override
    public void remove(Metadates entity){
        entity = getEntityManager().getReference(entity.getClass(), entity.getId());
        getEntityManager().remove(entity);
    }

    /**
     * Возвращает объект метаданных по его имени
     * @param objectName
     * @return
     */
    public Metadates findByObjectName(String objectName) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Metadates> cq = builder.createQuery(Metadates.class);
        Root<Metadates> root = cq.from(Metadates.class);
        Predicate crit1 = builder.equal(root.get(Metadates_.objectName), objectName);
        cq.select(root).where(builder.and(crit1));
        TypedQuery<Metadates> query = getEntityManager().createQuery(cq);
        return query.getSingleResult();
    }
}
