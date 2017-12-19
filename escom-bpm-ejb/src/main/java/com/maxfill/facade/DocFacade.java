package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.attaches.Attaches_;
import com.maxfill.model.docs.DocStates;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.users.User;
import com.maxfill.services.searche.SearcheService;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

@Stateless
public class DocFacade extends BaseDictFacade<Doc, Folder, DocLog, DocStates>{
    @EJB
    private AttacheService attacheService;
    @EJB
    private UserFacade userFacade;
    @EJB
    private SearcheService searcheService;
    @EJB
    private FoldersFacade folderFacade;
    
    public DocFacade() {
        super(Doc.class, DocLog.class, DocStates.class);
    }

    @Override
    public Class<Doc> getItemClass() {
        return Doc.class;
    }

    @Override
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<Doc> root, List<Predicate> criteries, Doc doc){
        if (doc.getDocType() != null){
            criteries.add(builder.equal(root.get("docType"), doc.getDocType()));
        }
        super.dublicateCheckAddCriteria(builder, root, criteries, doc);
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
    
    /* Возвращает список связанных документов */
    public List<Doc> findLinkedDocs(Doc doc){
        return find(doc.getId()).getDocsLinks();
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
        criteriaQuery.groupBy(docs.get(Doc_.docType));
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
        doSetStateById(doc, DictStates.STATE_EDITED);
        doc.doSetSingleRole(DictRoles.ROLE_EDITOR, user);
        edit(doc);
    }
    
    /* Снятие состояния редактировани документа */
    public void doRemoveEditState(Doc doc){
        returnToPrevState(doc);        
        doc.doSetSingleRole(DictRoles.ROLE_EDITOR, null);
        edit(doc);
    }

    /**
     * Получение прав доступа к документу
     */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user);
        }
        if (item.getOwner() != null) {
            Rights childRight = folderFacade.getRightForChild(item.getOwner());
            if (childRight != null) {
                return childRight;
            }
        }
        return getDefaultRights(item);
    }

    @Override
    public void edit(Doc doc) {
        doSaveRoleToJson(doc);
        super.edit(doc);
        searcheService.updateFullTextIndex(doc);
    }

    @Override
    public void create(Doc doc) {
        doSaveRoleToJson(doc);
        super.create(doc);
        searcheService.addFullTextIndex(doc);
    }

    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params) {
        Folder folder = doc.getOwner();
        doSetDefaultDocType(doc, folder);
        doSetDefaultCompany(doc, folder);
        doSetDefaultPartner(doc, folder);           
    
        doc.setDateDoc(new Date());
        /*
        if (doc.getOwner().getId() == null) { 
            doc.setOwner(null);
        }
        */
        if (params != null && !params.isEmpty()) {
            Attaches attache = (Attaches) params.get("attache");
            if (attache != null) {
                Integer version = doc.getNextVersionNumber();
                attache.setNumber(version);
                attache.setDoc(doc);
                String fileName = attache.getName();
                doc.setName(fileName);
                doc.getAttachesList().add(attache);
            }
        }
    }

    private void doSetDefaultPartner(Doc doc, Folder folder){
        if (doc.getPartner() == null && folder != null){
            if (folder.isInheritPartner()){
                doSetDefaultPartner(doc, folder.getParent());
            } else {
                doc.setPartner(folder.getPartnerDefault());
            }             
        }
    }

    private void doSetDefaultCompany(Doc doc, Folder folder){
        if (doc.getCompany() == null && folder != null){
            if (folder.isInheritCompany()){
                doSetDefaultCompany(doc, folder.getParent());
            } else {
                doc.setCompany(folder.getCompanyDefault());
            }
        }
    }
    
    private void doSetDefaultDocType(Doc doc, Folder folder){
        if (doc.getDocType() == null && folder != null){
            if (folder.isInheritDocType()){
                doSetDefaultDocType(doc, folder.getParent());
            } else {
                doc.setDocType(folder.getDocTypeDefault()); 
            }
        }
    }
    
    /* создание документа в папке пользователя из сервлета */
    public void createDocInUserFolder(String name, User author, Folder userFolder, Attaches attache){
        Map<String, Object> params = new HashMap<>();
        params.put("attache", attache);
        params.put("name", name);
        createItem(author, userFolder, params);        
    }
    
    private void doSaveRoleToJson(Doc doc){
        Gson gson = new Gson();
        String attacheJson = gson.toJson(doc.getRoles());        
        doc.setRoleJson(attacheJson);
    }
    
    /* Проверка вхождения пользователя в роль документа */
    @Override
    public boolean checkUserInRole(Doc doc, String roleName, User user){        
        roleName = roleName.toLowerCase();
        Map<String, Set<Integer>> roles = doc.getRoles();
        if (roles.isEmpty() || !roles.containsKey(roleName)) return false; 
        HashSet<Integer> usersId = (HashSet<Integer>)roles.get(roleName);
        if (usersId.isEmpty()) return false;        
        return usersId.contains(user.getId());             
    }
    
    /* Возвращает имя исполнителя роли */
    @Override
    public String getActorName(Doc doc, String roleName){
        Map<String, Set<Integer>> roles = doc.getRoles();
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
    
    /* Удаление документа  */
    @Override
    public void remove(Doc doc){
        searcheService.deleteFullTextIndex(doc);
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