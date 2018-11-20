package com.maxfill.model.basedict.process.reports;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.Doc;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для работы с сущностью "Отчёты процесса"
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
     * @param roleName
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
            
    /**
     * Отбор записей отчётов, относящихся к вложению
     * @param attache
     * @return 
     */
    public List<ProcReport> findReportByAttache(Attaches attache){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get(ProcReport_.version), attache);        
        cq.select(root).where(builder.and(crit1)); 
        Query q = em.createQuery(cq);
        return q.getResultList();
    }
    
}