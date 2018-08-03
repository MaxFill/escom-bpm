package com.maxfill.model.process.conditions;

import com.maxfill.facade.BaseFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Условие процесса"
 * @author maksim
 */
@Stateless
public class ConditionFacade extends BaseFacade<Condition>{
    
    public ConditionFacade() {
        super(Condition.class);
    }
    
    @Override
    public List<Condition> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(Condition.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Condition> cq = builder.createQuery(Condition.class);
        Root<Condition> root = cq.from(Condition.class);        
        cq.select(root);
        cq.orderBy(builder.asc(root.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
}

