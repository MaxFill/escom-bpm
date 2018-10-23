package com.maxfill.model.basedict.process.reports;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.basedict.doc.Doc;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author maksim
 */
@Stateless
public class ProcReportFacade extends BaseFacade<ProcReport>{

    public ProcReportFacade() {
        super(ProcReport.class);
    }
    
    /**
     * Отбор записей отчётов, относящихся к документу и роли
     * @param doc
     * @return 
     */
    public List<ProcReport> findReportByDoc(Doc doc, String roleName){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get(ProcReport_.doc), doc);
        Predicate crit2 = builder.equal(root.get(ProcReport_.roleName), roleName);
        cq.select(root).where(builder.and(crit1, crit2));        
        Query q = em.createQuery(cq);
        return q.getResultList();
    }
}
