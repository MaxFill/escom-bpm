
package com.maxfill.model.numPuttern.counter;

import com.maxfill.facade.BaseFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Генератор порядковых номеров для нумераторов
 * @author mfilatov
 */
@Stateless
public class CounterFacade extends BaseFacade<Counter> {

    public CounterFacade() {
        super(Counter.class);
    }
    
    /**
     * Возвращает список нумераторов по имени 
     * @param name
     * @return 
     */ 
    public List<Counter> findCounterByName(String name){
        getEntityManager().getEntityManagerFactory().getCache().evict(Counter.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Counter> cq = builder.createQuery(Counter.class);
        Root<Counter> c = cq.from(Counter.class);        
        Predicate crit1 = builder.equal(c.get("name"), name);
        cq.select(c).where(builder.and(crit1));
        cq.select(c);
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
}
