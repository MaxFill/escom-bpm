package com.maxfill.model.basedict.numeratorPattern.counter;

import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Stateless
public class CounterFacade extends BaseLazyFacade<Counter>{

    public CounterFacade() {
        super(Counter.class);
    }
    
    /**
     * Возвращает список нумераторов по имени 
     * @param name
     * @return 
     */ 
    public List<Counter> findCounterByName(String name){
        em.getEntityManagerFactory().getCache().evict(Counter.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Counter> cq = builder.createQuery(Counter.class);
        Root<Counter> c = cq.from(Counter.class);        
        Predicate crit1 = builder.equal(c.get("name"), name);
        cq.select(c).where(builder.and(crit1));
        cq.select(c);
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }
    
    
}
