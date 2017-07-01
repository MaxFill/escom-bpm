
package com.maxfill.facade;

import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Документы  */
@Stateless
public class DocFacade extends BaseDictFacade<Doc, Folder, DocLog>{
    @EJB
    private AttacheService attacheService;
            
    public DocFacade() {
        super(Doc.class, DocLog.class);
    }          
    
    @Override
    public String getFRM_NAME() {
        return Doc.class.getSimpleName().toLowerCase();
    }    
    
    /* Возвращает документы с указанным Менеджером  */ 
    public List<Doc> findDocsByManager(Staff manager){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.equal(c.get("manager"), manager);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Возвращает документы с указанным Контрагентом  */
    public List<Doc> findDocsByPartner(Partner partner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.equal(c.get("partner"), partner);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();   
    }
    
    /* Возвращает документы с указанным Видом документа  */
    public List<Doc> findDocsByDocTyper(DocType docType){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.equal(c.get("docType"), docType);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();   
    }
    
    /* Подсчёт кол-ва документов по типам */
    public List<Tuple> countDocByDocTypeGroups(List<DocTypeGroups> docTypeGroups, Date startPeriod, Date endPeriod, List<DocTypeGroups> groups){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery= builder.createTupleQuery();
        Root docs = criteriaQuery.from(Doc.class);
        Expression<Integer> docId = docs.get(Doc_.id);
        Expression<String> typeName = docs.get(Doc_.docType).get("name");
        criteriaQuery.multiselect(typeName, builder.count(docId));
        criteriaQuery.groupBy(docs.get("id"));
        Predicate crit1 = builder.equal(docs.get("deleted"), false);
        Predicate crit2 = builder.greaterThanOrEqualTo(docs.get("dateDoc"), startPeriod);
        Predicate crit3 = builder.lessThanOrEqualTo(docs.get("dateDoc"), endPeriod);
        Predicate crit4 = docs.get(Doc_.docType).get("owner").in(groups);
        criteriaQuery.where(builder.and(crit1, crit2, crit3, crit4));
        criteriaQuery.orderBy(builder.asc(docs.get("docType").get("name")));
        Query query = getEntityManager().createQuery(criteriaQuery);
        List<Tuple> result = query.getResultList();        
        return result;
    }
    
    /* Возвращает документы нулевого уровня  */ 
    public List<Doc> findRootDocs(){        
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.isNull(c.get("owner"));
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }    
    
    /* Ищет документы с указанным номером  */
    public boolean checkRegNumber(String regNumber, Doc excludeDoc){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> criteriaQuery = builder.createQuery(Doc.class);
        Root<Doc> root = criteriaQuery.from(Doc.class);
        
        Predicate[] predicates = new Predicate[3];
        predicates[0] = builder.equal(root.get("regNumber"), regNumber);
        predicates[1] = builder.equal(root.get("docType"), excludeDoc.getDocType());
        predicates[2] = builder.notEqual(root.get("id"), excludeDoc.getId());

        criteriaQuery.select(root).where(builder.and(predicates)); 
        TypedQuery<Doc> query = getEntityManager().createQuery(criteriaQuery);
        return query.getResultList().isEmpty();
    }
    
    /* Удаление документов из папки  */ 
    public void deleteDocFromFolder(Folder folder){
        attacheService.deleteAttacheByFolder(folder);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Doc> delete = cb.createCriteriaDelete(Doc.class);
        Root e = delete.from(Doc.class);
        delete.where(cb.equal(e.get("owner"), folder));
        getEntityManager().createQuery(delete).executeUpdate();
    }    
    
    /* Удаление документа  */
    @Override
    public void remove(Doc doc){
       attacheService.deleteAttaches(doc.getAttachesList());
       super.remove(doc);
    }           

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS; 
    }

    @Override
    public void replaceItem(Doc oldItem, Doc newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}