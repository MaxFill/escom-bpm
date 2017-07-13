package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.attaches.Attaches_;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.users.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

@Stateless
public class DocFacade extends BaseDictFacade<Doc, Folder, DocLog>{
    @EJB
    private AttacheService attacheService;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private UserFacade userFacade;
    
    public DocFacade() {
        super(Doc.class, DocLog.class);
    }          
    
    @Override
    public String getFRM_NAME() {
        return Doc.class.getSimpleName().toLowerCase();
    }    
    
    /* Возвращает документы, заблокированные пользователем */
    @Override
    public List<Doc> loadLockDocuments(User editor){
        getEntityManager().getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> root = cq.from(Attaches.class);
        Join docJoin = root.join(Attaches_.doc);
        Predicate crit1 = builder.equal(root.get(Attaches_.lockAuthor), editor);
        cq.select(docJoin);
        cq.where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);        
        List r = q.getResultList();
        return (List<Doc>)r;
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
        TypedQuery<Doc> q = getEntityManager().createQuery(cq);       
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
        
    /* Установка состояния редактирования документа */
    public void doSetEditState(Doc doc, User user){
        doc.setState(stateFacade.find(DictStates.STATE_EDITED));
        doc.doSetSingleRole("editor", user);
        edit(doc);
    }
    
    /* Снятие состояния редактировани документа */
    public void doRemoveEditState(Doc doc){
        doc.setState(stateFacade.find(DictStates.STATE_VALID));
        doc.doSetSingleRole("editor", null);
        edit(doc);
    }
    
    @Override
    public void edit(Doc doc) {
        Gson gson = new Gson();
        String attacheJson = gson.toJson(doc.getRoles());        
        doc.setRoleJson(attacheJson);
        getEntityManager().merge(doc);
    }
    
    /* Проверка вхождения пользователя в роль */
    @Override
    public boolean checkUserInRole(Doc doc, String roleName, User user){        
        Map<String, Set<Integer>> roles = getRoleMap(doc);
        if (roles.isEmpty() || !roles.containsKey(roleName)) return false;
        Set<Integer> usersId = roles.get(roleName);
        if (usersId == null || usersId.isEmpty()) return false;        
        return usersId.contains(user.getId());
    }
    
    /* Возвращает имя испольнителя роли */
    @Override
    public String getActorName(Doc doc, String roleName){
        Map<String, Set<Integer>> roles = getRoleMap(doc);
        if (roles.isEmpty() || !roles.containsKey(roleName)) return null;
        Set<Integer> usersId = roles.get(roleName);
        if (usersId == null || usersId.isEmpty()) return null;
        StringBuilder names = new StringBuilder();
        usersId.stream().map((userId) -> userFacade.find(userId)).forEach((user) -> {
            if (names.length() > 0){
                names.append(", ");
            }
            names.append(user.getName());
        }); 
        return names.toString();        
    }
    
    private Map<String, Set<Integer>> getRoleMap(Doc doc){
        Map<String, Set<Integer>> roles = doc.getRoles();
        if (roles.isEmpty()){
            String roleJson = doc.getRoleJson();
            if (StringUtils.isBlank(roleJson)) return roles;
            Gson gson = new Gson();
            roles = gson.fromJson(roleJson, Map.class);
            doc.setRoles(roles);
        }
        return roles;
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