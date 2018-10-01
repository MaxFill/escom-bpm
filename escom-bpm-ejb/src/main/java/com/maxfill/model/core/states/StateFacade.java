package com.maxfill.model.core.states;

import com.maxfill.dictionary.DictStates;
import com.maxfill.facade.BaseFacade;
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
public class StateFacade extends BaseFacade<State>{

    public StateFacade() {
        super(State.class);
    }
    
    @Override
    public State find(Object id){
        return getEntityManager().find(State.class, id);
    }    

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
    
    public State getRunningState(){
        return find(DictStates.STATE_RUNNING);
    }
    
    public State getDraftState(){
        return find(DictStates.STATE_DRAFT);
    }
    
    public State getCompletedState(){
        return find(DictStates.STATE_COMPLETED);
    }
    
    public State getArhivalState(){
        return find(DictStates.STATE_ARHIVAL);
    }
    
    public State getValidState(){
        return find (DictStates.STATE_VALID);
    }
    
    public State getCanceledState(){
        return find(DictStates.STATE_CANCELLED);
    }
    
    public State getConfirmedState(){
        return find(DictStates.STATE_CONFIRMED);
    }
    
    public State getIssuedState(){
        return find(DictStates.STATE_ISSUED);
    }
}
