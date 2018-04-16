package com.maxfill.facade;

import com.maxfill.facade.base.BaseFacade;
import com.maxfill.model.states.State;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Состояния обектов"
 */
@Stateless
public class StateFacade extends BaseFacade{

    public StateFacade() {
        super(State.class);
    }
    
    @Override
    public State find(Object id){
        return getEntityManager().find(State.class, id);
    }
    
    public List<State> findStateDocList(){
        getEntityManager().getEntityManagerFactory().getCache().evict(State.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<State> cq = builder.createQuery(State.class);
        Root<State> c = cq.from(State.class);        
        //Predicate crit1 = builder.isNull(c.get("folders"));
        //cq.select(c).where(builder.and(crit1));
        cq.select(c);
        //cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }

    @Override
    public List<State> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(State.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<State> cq = builder.createQuery(State.class);
        Root<State> c = cq.from(State.class);        
        Predicate crit1 = builder.equal(c.get("isActual"), true);
        cq.select(c).where(builder.and(crit1));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
}
