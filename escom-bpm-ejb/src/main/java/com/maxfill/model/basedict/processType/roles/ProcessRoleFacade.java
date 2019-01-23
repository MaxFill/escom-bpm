package com.maxfill.model.basedict.processType.roles;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.basedict.userGroups.UserGroups;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для ролей
 */
@Stateless
public class ProcessRoleFacade extends BaseFacade<ProcessRole>{
    public ProcessRoleFacade() {
        super(ProcessRole.class);
    }

    /**
     * Возвращает колво записей, в которых группа пользователей используется в ролях процесса
     * @param userGroup
     * @return 
     */
    public Long findCountUserGroupLinks(UserGroups userGroup){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root root = cq.from(itemClass);
        List<Predicate> criteries = new ArrayList<>();        
        criteries.add(builder.equal(root.get("dataSource"), userGroup));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
}
