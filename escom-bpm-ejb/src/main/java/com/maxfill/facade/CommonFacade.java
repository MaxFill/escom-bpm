package com.maxfill.facade;

import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.core.states.State;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.WordUtils;

/**
 * Общий для всех сущностей фасад
 */
@Stateless
public class CommonFacade {
    @PersistenceContext(unitName = "com.maxfill.escombpm2PU")
    private EntityManager entityManager;
    
    /* Возвращает число объектов у которых состояние равно указанному */
    public Integer countItemsByState(Metadates item, State state){        
        Class itemClass = getItemClass(item);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get("state").get("currentState"), state);
        cq.select(builder.count(root));
        cq.where(builder.and(crit1));
        Query query = entityManager.createQuery(cq);
        return ((Long) query.getSingleResult()).intValue();
    }
    
    private Class getItemClass(Metadates item){
        try {
            StringBuilder sb = new StringBuilder("com.maxfill.model.basedict.");
            sb.append(WordUtils.uncapitalize(item.getObjectName()));
            sb.append(".");
            sb.append(item.getObjectName());
            return Class.forName(sb.toString());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CommonFacade.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException();
        }
    }
}