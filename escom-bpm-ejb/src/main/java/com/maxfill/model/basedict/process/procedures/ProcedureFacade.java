package com.maxfill.model.basedict.process.procedures;

import com.maxfill.facade.BaseFacade;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Процедуры процессов"
 */
@Stateless
public class ProcedureFacade extends BaseFacade<Procedure>{
    
    public ProcedureFacade() {
        super(Procedure.class);
    }
    
    public List<Procedure> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(Procedure.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Procedure> cq = builder.createQuery(Procedure.class);
        Root<Procedure> root = cq.from(Procedure.class);        
        cq.select(root);
        cq.orderBy(builder.asc(root.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
}
