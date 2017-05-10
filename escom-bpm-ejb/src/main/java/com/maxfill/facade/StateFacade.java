
package com.maxfill.facade;

import com.maxfill.model.states.StateLog;
import com.maxfill.model.states.State;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author mfilatov
 */
@Stateless
public class StateFacade extends BaseDictFacade<State, State, StateLog> {

    public StateFacade() {
        super(State.class, StateLog.class);
    }
    
    @Override
    public String getFRM_NAME() {
        return DictObjectName.STATE.toLowerCase();
    }
    
    /* Возвращает базовый список состояний для документов */
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
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_STATES;
    }

    @Override
    public Map<String, Integer> replaceItem(State oldItem, State newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
