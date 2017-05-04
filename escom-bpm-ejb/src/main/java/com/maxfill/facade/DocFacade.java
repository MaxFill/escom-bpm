
package com.maxfill.facade;

import com.maxfill.model.docs.DocModel;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocLog;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.Folders;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.User;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * Документы
 * @author mfilatov
 */
@Stateless
public class DocFacade extends BaseDictFacade<Doc, Folders, DocLog>{
    @EJB
    private AttacheService attacheService;
            
    public DocFacade() {
        super(Doc.class, DocLog.class);
    }
          
    @Override
    public void pasteItem(Doc pasteItem, BaseDict target, Set<String> errors){        
        pasteItem.setOwner((Folders)target);
        doPaste(pasteItem, errors);
    }
    
    @Override
    public String getFRM_NAME() {
        return Doc.class.getSimpleName().toLowerCase();
    }    
    
    /**
     * Возвращает документы с указанным Менеджером
     * @param manager
     * @return Список документов
     */ 
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
    
    /**
     * Возвращает документы с указанным Контрагентом
     * @param partner
     * @return 
     */
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
    
    /**
     * Возвращает документы с указанным Видом документа
     * @param docType
     * @return 
     */
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
    
    /**
     * Возвращает документы нулевого уровня
     * @return Список документов
     */ 
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
    
    /**
     * Ищет документы с указанным номером
     * @param regNumber
     * @param excludeDoc
     * @return 
     */
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
    
    /**
     * Удаление документов из папки
     * @param folder
     */ 
    public void deleteDocFromFolder(Folders folder){
        attacheService.deleteAttacheByFolder(folder);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Doc> delete = cb.createCriteriaDelete(Doc.class);
        Root e = delete.from(Doc.class);
        delete.where(cb.equal(e.get("owner"), folder));
        getEntityManager().createQuery(delete).executeUpdate();
    }
    
    /* Установка специфичных атрибутов документа при его создании */
    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params){
        Folders folder = doc.getOwner();
        if (folder != null){
            DocType docType = folder.getDocTypeDefault();
            doc.setDocType(docType);
        }
        doc.setDateDoc(new Date());
        if (doc.getOwner().getId() == null){ //сброс owner если документ создаётся в корне архиа!
            doc.setOwner(null);
        }
        if (params != null && !params.isEmpty()){
            Attaches attache = (Attaches)params.get("attache");
            if (attache != null){
                Short version = doc.getNextVersionNumber();            
                attache.setNumber(version);
                attache.setDoc(doc);
                String fileName = attache.getName();
                doc.setName(fileName);
                doc.getAttachesList().add(attache);
            }
        }
    }
    
    /**
     * Удаление документа
     * @param doc 
     */
    @Override
    public void remove(Doc doc){
       attacheService.deleteAttaches(doc.getAttachesList());
       super.remove(doc);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel baseDataModel) {
        DocModel model = (DocModel) baseDataModel;
        String numberSearche = model.getNumberSearche().trim();
        if (StringUtils.isNotBlank(numberSearche)){
            //Join<Doc, Post> postJoin = root.join(Staff_.post);
            predicates.add(builder.like(root.<String>get("number"), numberSearche));            
        }
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS; 
    }

    @Override
    public Map<String, Integer> replaceItem(Doc oldItem, Doc newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}