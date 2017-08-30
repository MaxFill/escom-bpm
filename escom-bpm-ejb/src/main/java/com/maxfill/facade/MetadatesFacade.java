
package com.maxfill.facade;

import com.maxfill.model.metadates.Metadates;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

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
}
