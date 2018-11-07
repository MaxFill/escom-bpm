package com.maxfill.model.basedict.doc;

import com.maxfill.dictionary.SysParams;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.attaches.Attaches_;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.docTypeGroups.DocTypeGroups;
import com.maxfill.model.basedict.user.User;
import com.maxfill.services.mail.MailSettings;
import com.maxfill.services.numerators.doc.DocNumeratorService;
import com.maxfill.services.searche.SearcheService;
import org.apache.commons.lang3.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
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
public class DocFacade extends BaseDictWithRolesFacade<Doc, Folder, DocLog, DocStates> {
    private static final String HTML_HEAD = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head>";
    @EJB
    private AttacheService attacheService;
    @EJB
    private SearcheService searcheService;
    @EJB
    private FoldersFacade folderFacade;
    @EJB
    private DocNumeratorService docNumeratorService;
    
    public DocFacade() {
        super(Doc.class, DocLog.class, DocStates.class);
    }

    @Override
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<Doc> root, List<Predicate> criteries, Doc doc){
        if (doc.getDocType() != null){
            criteries.add(builder.equal(root.get("docType"), doc.getDocType()));
        }
        super.dublicateCheckAddCriteria(builder, root, criteries, doc);
    }        
    
     /* Дополнения при выполнении поиска через форму поиска */
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> addParams){
        if (addParams.containsKey("docTypes")){
            List<Integer> docTypeIds = (List<Integer>) addParams.get("docTypes");
            if (!docTypeIds.isEmpty()) { 
                Join join = root.join("docType");
                Predicate predicate = join.get("id").in(docTypeIds);
                predicates.add(predicate);
            } 
        }
    }
    
    /**
     * Переопределяет доступ к методу, т.к. основной private
     * @param item
     * @param user
     * @return 
     */
    @Override
    public Boolean preloadCheckRightView(BaseDict item, User user) {
        return super.preloadCheckRightView(item, user);
    }
        
    /* Возвращает документы, заблокированные пользователем */
    @Override
    public List<Doc> loadLockDocuments(User currentUser){
        em.getEntityManagerFactory().getCache().evict(Attaches.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Attaches> cq = builder.createQuery(Attaches.class);
        Root<Attaches> root = cq.from(Attaches.class);
        Join docJoin = root.join(Attaches_.doc);
        Predicate crit1 = builder.equal(root.get(Attaches_.lockAuthor), currentUser);
        cq.select(docJoin);
        cq.where(builder.and(crit1));
        Query query = em.createQuery(cq);                        
        return (List<Doc>) query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    /* Возвращает документы с указанным Контрагентом  */
    public List<Doc> findDocsByPartner(Partner partner){
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.equal(c.get("partner"), partner);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = em.createQuery(cq);       
        return q.getResultList();   
    }
    
    /* Возвращает документы с указанным Видом документа  */
    public List<Doc> findDocsByDocTyper(DocType docType){
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.equal(c.get("docType"), docType);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = em.createQuery(cq);       
        return q.getResultList();   
    }
    
    /* Возвращает список связанных документов */
    public List<Doc> findLinkedDocs(Doc doc){
        return find(doc.getId()).getDocsLinks();
    }
    
    /* Подсчёт кол-ва документов по типам */
    public List<Tuple> countDocByDocTypeGroups(List<DocTypeGroups> docTypeGroups, Date startPeriod, Date endPeriod, List<DocTypeGroups> groups){
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery= builder.createTupleQuery();
        Root docs = criteriaQuery.from(Doc.class);
        Expression<Integer> docId = docs.get(Doc_.id);
        Expression<String> typeName = docs.get(Doc_.docType).get("name");
        criteriaQuery.multiselect(typeName, builder.count(docId));
        criteriaQuery.groupBy(docs.get(Doc_.docType));
        Predicate crit1 = builder.equal(docs.get("deleted"), false);
        Predicate crit2 = builder.greaterThanOrEqualTo(docs.get("dateCreate"), startPeriod);
        Predicate crit3 = builder.lessThanOrEqualTo(docs.get("dateCreate"), endPeriod);
        Predicate crit4 = docs.get(Doc_.docType).get("owner").in(groups);
        criteriaQuery.where(builder.and(crit1, crit2, crit3, crit4));
        criteriaQuery.orderBy(builder.asc(docs.get("docType").get("name")));
        Query query = em.createQuery(criteriaQuery);
        List<Tuple> result = query.getResultList();        
        return result;
    }
    
    /* Возвращает документы нулевого уровня  */ 
    public List<Doc> findRootDocs(){        
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);        
        Predicate crit1 = builder.isNull(c.get("owner"));
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = em.createQuery(cq);       
        return q.getResultList(); 
    }    
    
    /* Ищет документы с указанным номером  */
    public boolean checkRegNumber(String regNumber, Doc excludeDoc){
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Doc> criteriaQuery = builder.createQuery(Doc.class);
        Root<Doc> root = criteriaQuery.from(Doc.class);
        
        Predicate[] predicates = new Predicate[3];
        predicates[0] = builder.equal(root.get("regNumber"), regNumber);
        predicates[1] = builder.equal(root.get("docType"), excludeDoc.getDocType());
        predicates[2] = builder.notEqual(root.get("id"), excludeDoc.getId());

        criteriaQuery.select(root).where(builder.and(predicates)); 
        TypedQuery<Doc> query = em.createQuery(criteriaQuery);
        return query.getResultList().isEmpty();
    }
    
    /* Удаление документов из папки  */ 
    public void deleteDocFromFolder(Folder folder){
        attacheService.deleteAttacheByFolder(folder);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Doc> delete = cb.createCriteriaDelete(Doc.class);
        Root e = delete.from(Doc.class);
        delete.where(cb.equal(e.get("owner"), folder));
        em.createQuery(delete).executeUpdate();
    }    
        
    /* Установка состояния редактирования документа */
    public void doSetEditState(Doc doc, User user){        
        doc.doSetSingleRole(DictRoles.ROLE_EDITOR, user.getId());
        changeState(doc, DictStates.STATE_EDITED);        
    }
    
    /* Снятие состояния редактирования документа */
    public void doRemoveEditState(Doc doc){
        clearRole(doc, DictRoles.ROLE_EDITOR);        
        returnToPrevState(doc);
    }

    /**
     * Получение прав доступа к документу
     * @param item
     * @param user
     * @return 
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
        super.edit(doc);
        searcheService.updateFullTextIndex(doc);
    }

    @Override
    public void create(Doc doc) {
        super.create(doc);        
        searcheService.updateFullTextIndex(doc);
    }

    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params) {
        Folder folder = doc.getOwner();
        doSetDefaultDocType(doc, folder);
        doSetDefaultCompany(doc, folder);
        doSetDefaultPartner(doc, folder);           
        doc.setItemDate(new Date());
        DocType docType = doc.getDocType();
        if (docType != null && docType.getNumerator() != null && DictNumerator.TYPE_AUTO.equals(docType.getNumerator().getTypeCode())){ 
            docNumeratorService.registratedDoc(doc, new HashSet<>());
        }
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
        super.setSpecAtrForNewItem(doc, params);
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
    
    /**
     * Cоздание документа в папке пользователя вызов из сервлета!
     * Документ записывается в базу!
     * @param name
     * @param author
     * @param userFolder
     * @param attache
     * @return 
     */
    public Doc createDocInUserFolder(String name, User author, Folder userFolder, Attaches attache){
        Map<String, Object> params = new HashMap<>();
        params.put("attache", attache);
        params.put("name", name);
        Doc doc = createItem(author, null, userFolder, params);
        create(doc);
        makeRightItem(doc, author);
        return doc;
    }
    public Doc createDocInUserFolder(String name, User author, Folder userFolder){
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        Doc doc = createItem(author, null, userFolder, params);
        create(doc);
        return doc;
    }

    /**
     * Создание документа из e-mail сообщения
     * @param message
     * @param detailInfo
     * @param settings
     * @return 
     */
    public boolean createDocFromEmail(Message message, StringBuilder detailInfo, MailSettings settings) {
        try {
            Address senders[] = message.getFrom();
            String sender = ((InternetAddress)senders[0]).getAddress();

            List<User> users = userFacade.findUserByEmail(sender);

            if (users.isEmpty()) {
                detailInfo.append("Sender ").append(sender).append(" not found in users!").append(SysParams.LINE_SEPARATOR);
                return settings.getDeleteIfUnknownSender();
            }

            User author = users.get(0);
            Folder folder = author.getInbox();

            if (folder == null) {
                detailInfo.append("User ").append(author.getLogin()).append(" ").append(author.getShortFIO()).append(" not have folder!").append(SysParams.LINE_SEPARATOR);
                return false;
            }

            String subject = message.getSubject();
            Doc doc = createDocInUserFolder(subject, author, folder);

            Map <String, Object> params = new HashMap <>();

            if (message.isMimeType("text/*")){
                String content = (String) message.getContent();
                params.put("contentType", "text/html");
                params.put("fileName", "e-mail messsage");
                params.put("fileExt", "txt");
                params.put("size", Long.valueOf(content.length()));
                params.put("author", author);
                Attaches attache = attacheService.uploadAtache(params, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name())));
                doc.getAttachesList().add(attache);
                return true;
            }

            if (message.isMimeType("multipart/*") || message.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) message.getContent();
                for(int i = 0; i < mp.getCount(); i++) {
                    readBodyPart(mp.getBodyPart(i), doc, author);
                }
                return true;
            }

        } catch(IOException | MessagingException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Обработка BodyPart сообщения
     */
    private void readBodyPart(BodyPart bodyPart, Doc doc, User author) throws MessagingException, IOException{
        Map <String, Object> params = new HashMap <>();
        if (bodyPart.getContent() instanceof Multipart) {
            Multipart mp = (Multipart) bodyPart.getContent();
            for(int i = 0; i < mp.getCount(); i++) {
                BodyPart part = mp.getBodyPart(i);
                if(part.isMimeType("multipart/alternative")) {
                    readBodyPart(part, doc, author);
                } else if(part.isMimeType("text/html")) {
                    loadHtmlMsg(part, doc, author);
                }
            }
        } else
            if (bodyPart.getContent() instanceof InputStream){
                if(Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || StringUtils.isNotBlank(bodyPart.getFileName())) {
                    String decoded = MimeUtility.decodeText(bodyPart.getFileName());
                    String filename = Normalizer.normalize(decoded, Normalizer.Form.NFC);
                    params.put("contentType", bodyPart.getContentType());
                    params.put("fileName", filename);
                    params.put("size", Long.valueOf(bodyPart.getSize()));
                    params.put("author", author);
                    loadMailAttache(params, bodyPart.getInputStream(), doc);
                }
            } else
                if (bodyPart.getContent() instanceof String){
                    if(bodyPart.isMimeType("text/html")) {
                        loadHtmlMsg(bodyPart, doc, author);
                    }
                }
    }

    private void loadMailAttache(Map <String, Object> params, InputStream inputStream, Doc doc) throws MessagingException, IOException{
        Attaches attache = attacheService.uploadAtache(params, inputStream);
        if (attache != null) {
            attache.setDoc(doc);
            attache.setNumber(doc.getNextVersionNumber());
            doc.getAttachesList().add(attache);
        }
    }

    private void loadHtmlMsg(BodyPart part,  Doc doc, User author) throws MessagingException, IOException{
        Map <String, Object> params = new HashMap <>();
        String ct = (String) part.getContent();
        String html = ct.replace("<HTML>", HTML_HEAD);
        params.put("contentType", part.getContentType());
        params.put("fileName", "e-mail message");
        params.put("fileExt", "html");
        params.put("size", Long.valueOf(part.getSize()));
        params.put("author", author);
        loadMailAttache(params, new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8.name())), doc);
    }
    
    /* Удаление документа  */
    @Override
    public void remove(Doc doc){
        searcheService.deleteFullTextIndex(doc);
        attacheService.deleteAttaches(doc.getAttachesList());        
        super.remove(doc);
    }           

    public Long findCountCompanyLinks(Company company){
        em.getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Doc> root = cq.from(Doc.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Doc_.company), company));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }    
        
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS; 
    }

    /**
     * Замена документа на другой
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(Doc oldItem, Doc newItem) {
        int count = 0;
        return count;
    }
    
    @Override
    protected String getItemFormPath(){
        return "/docs/doc-explorer.xhtml";
    }    
}