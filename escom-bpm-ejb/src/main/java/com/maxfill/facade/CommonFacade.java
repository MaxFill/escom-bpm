package com.maxfill.facade;

import com.maxfill.model.BaseDict;
import com.maxfill.model.states.State;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Общий для всех сущностей фасад
 */
@Stateless
public class CommonFacade {
    @PersistenceContext(unitName = "com.maxfill.escombpm2PU")
    private EntityManager entityManager;
    
    /* Возвращает число объектов у которых состояние равно указанному */
    public Integer countItemsByState(Class itemClass, State state){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get("state").get("currentState"), state);
        cq.select(builder.count(root));
        cq.where(builder.and(crit1));        
        Query query = entityManager.createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }
    
}
