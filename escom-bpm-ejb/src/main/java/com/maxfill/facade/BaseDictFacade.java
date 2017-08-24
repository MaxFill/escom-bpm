package com.maxfill.facade;

import com.maxfill.Configuration;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.BaseDict;
import com.maxfill.model.BaseLogTable;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.states.BaseStateItem;
import com.maxfill.services.numerator.NumeratorService;
import com.maxfill.model.states.State;
import com.maxfill.model.users.User;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.Tuple;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * Абстрактный фасад для справочников
 * @author Maxim
 * @param <T>   //класс объекта
 * @param <O>   //класс владельца объекта
 * @param <L>   //класс таблицы лога
 * @param <S>   //класс таблицы состояний
 */
public abstract class BaseDictFacade<T extends BaseDict, O extends BaseDict, L extends BaseLogTable, S extends BaseStateItem> extends BaseFacade<T>{
    private final Class<T> itemClass; 
    private final Class<L> logClass; 
    private final Class<S> stateClass;
    
    @EJB
    private MetadatesFacade metadatesFacade; 
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected RightFacade rightFacade;    
    @EJB
    protected Configuration configuration;          
    @EJB
    private StateFacade stateFacade;
        
    public BaseDictFacade(Class<T> itemClass, Class<L> logClass, Class<S> stateClass) {
        super(itemClass);
        this.itemClass = itemClass;
        this.logClass = logClass;
        this.stateClass = stateClass;
    } 
    
    public Tuple findDublicateExcludeItem(T item){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass); 
        List<Predicate> criteries = new ArrayList<>();
        
        dublicateCheckAddCriteria(builder, c, criteries, item);
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(c).where(builder.and(predicates));        
        TypedQuery<T> query = getEntityManager().createQuery(cq);
        List<T> rezult = query.getResultList();
        if (rezult.isEmpty()){
            return new Tuple(false, null);
        } else {        
            return new Tuple(true, rezult.get(0));
        }
    }
    
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<T> root, List<Predicate> criteries, T item){
        criteries.add(builder.equal(root.get("name"), item.getName()));
        if (item.getId() != null){
            criteries.add(builder.notEqual(root.get("id"), item.getId()));
        }
        if (item.getParent() != null){
            criteries.add(builder.equal(root.get("parent"), item.getParent()));
        } else {
            criteries.add(builder.isNull(root.get("parent")));
        }
        if (item.getOwner() != null){
            criteries.add(builder.equal(root.get("owner"), item.getOwner()));
        } else {
            criteries.add(builder.isNull(root.get("owner")));
        }
    }
    
    /* СОЗДАНИЕ: cоздание объекта */
    public T createItem(User author) {
        try {
            T item = itemClass.newInstance();
            item.setAuthor(author);
            item.setActual(true);
            item.setDeleted(false);
            item.setInherits(true);
            item.doSetSingleRole(DictRoles.ROLE_OWNER, author);
            doSetState(item, getMetadatesObj().getStateForNewObj());
            return item;
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }     
    
    /* установка состояния объекта */     
    public void doSetState(T item, State currentState){
        try {
            BaseStateItem stateItem = item.getState();
            State previousState = null;
            if (stateItem == null){
                stateItem = stateClass.newInstance();
            } else {
                previousState = stateItem.getCurrentState();
            }
            stateItem.setPreviousState(previousState);
            stateItem.setCurrentState(currentState);
            item.setState(stateItem);
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void doSetStateById(T item, Integer stateId){
        doSetState(item, stateFacade.find(stateId)); 
    }
    
    public void returnToPrevState(T item){
        BaseStateItem stateItem = item.getState();
        State previousState = stateItem.getPreviousState();
        if (previousState != null){
            stateItem.setCurrentState(previousState);
            item.setState(stateItem);
        }
    }
            
    /* Возвращает актуальные подчинённые объекты для владельца  */
    public List<T> findActualDetailItems(O owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(c.get("parent")));
        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));
        if (owner == null){
            criteries.add(builder.isNull(c.get("owner")));
        } else {
            criteries.add(builder.equal(c.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Возвращает все подчинённые объекты для владельца  */
    public List<T> findAllDetailItems(O owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.equal(c.get("owner"), owner));

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Возвращает корневые объекты  */
    public List<T> findRootItems(){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(c.get("parent")));
        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));
        criteries.add(builder.isNull(c.get("owner")));

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Отбор объектов, созданных пользователем  */
    public List<T> findItemsCreatedByUser(User user){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get("author"), user);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Отбор актуальных объектов по их владельцу  */
    public List<BaseDict> findItemByOwner(O owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get("owner"), owner);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
            
    /* Отбор объектов находящихся в корзине */
    public List<T> loadFromTrash(){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass); 
        Predicate crit2 = builder.equal(c.get("deleted"), true);
        cq.select(c).where(builder.and(crit2));
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<T> q = getEntityManager().createQuery(cq);       
        
        /* не показывать в корзине child объекты если удалён их parent
            и не показывать detail если удалён их owner */

        List<T> result = q.getResultList().stream()
                .filter(item ->
                        (item.getParent() == null || (item.getParent() != null && !item.getParent().isDeleted())) &&
                        (item.getOwner() == null || (item.getOwner() != null && !item.getOwner().isDeleted()))
                       )
                .collect(Collectors.toList());
        return result;
    }
 
    /* Отбор не актуальных объектов  */
    public List<T> loadNotActualItems(){
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);        
        Predicate crit1 = builder.equal(c.get("actual"), false);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }                       
    
    /* Возвращает документы, заблокированные пользователем */
    public List<T> loadLockDocuments(User editor){
        return null;
    }
    
    /* Проверка вхождения пользователя в роль */
    public boolean checkUserInRole(T item, String roleName, User user){
        return "owner".equals(roleName.toLowerCase()) && Objects.equals(item.getAuthor(), user);
    }
    
    /* Возвращает имя испольнителя роли */
    public String getActorName(T item, String roleName){
        return null;
    }
        
    /* ЛОГИРОВАНИЕ ИЗМЕНЕНИЙ */

    /* Добавление события в журнал событий */
    public void addLogEvent(T item, String msgKey, User user) {
        addLogEvent(item, msgKey, "", user);
    }
    
    public void addLogEvent(T item, String template, String parameter, User user) {
        Object[] params = new Object[]{parameter};
        Date dateEvent = new Date();        
        String msgEvent = MessageFormat.format(template, params);
        BaseLogTable itemLog = createLogEvent(item, dateEvent, msgEvent, user);
        item.getItemLogs().add(itemLog);
    }
    
    /* Создание записи лога  */
    public L createLogEvent(T item, Date dateEvent, String msgEvent, User user){
        try {
            L logEvent = logClass.newInstance();
            logEvent.setEvent(msgEvent);
            logEvent.setDateEvent(dateEvent);
            logEvent.setUserId(user);
            logEvent.setItem(item);
            return logEvent;
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }         
    
    @Override
    public void remove(T entity){
        entity = getEntityManager().getReference(itemClass, entity.getId());
        getEntityManager().remove(entity);
    }
        
    public List<T> getByParameters(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams) {
        CriteriaQuery<T> criteriaQuery = selectQueryByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, itemClass, addParams);
        TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
        return query.getResultList();
    }

    protected <EC> CriteriaQuery<EC> selectQueryByParameters(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Class<EC> entityClass, Map<String, Object> addParams) {
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<EC> criteriaQuery = builder.createQuery(entityClass);
        Root<EC> root = criteriaQuery.from(entityClass);
        
        List<Predicate> criteries = new ArrayList<>(); 
        criteries.add(builder.equal(root.get("deleted"), false));
        
        
        if (!states.isEmpty()) { 
            Join stateJoin = root.join("state");
            Predicate predicate = stateJoin.get("currentState").in(states);
            criteries.add(predicate);
        }
        
        for (Map.Entry<String, Object> param : paramIN.entrySet()) { 
            Predicate predicate = root.get(param.getKey()).in((List<Integer>)param.getValue());
            criteries.add(predicate);
        }                
        
        for (Map.Entry<String, Object> parameter : paramEQ.entrySet()) {
           criteries.add(builder.equal(root.get(parameter.getKey()), parameter.getValue()));
        }
        
        for (Map.Entry<String, Object> parameter : paramLIKE.entrySet()) {            
            criteries.add(builder.like(root.<String>get(parameter.getKey()), (String) parameter.getValue()));
        }
        
        for (Map.Entry<String, Date[]> parameter : paramDATE.entrySet()) {            
            Date dateStart = parameter.getValue()[0];
            Date dateEnd = parameter.getValue()[1];
            criteries.add(builder.between(root.get(parameter.getKey()), dateStart, dateEnd));
        }
        
        addJoinPredicatesAndOrders(root, criteries, builder, addParams);
        
        criteriaQuery.orderBy(builder.asc(root.get("name")));
        
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return criteriaQuery.select(root).where(builder.and(predicates));        
    }
    
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates,  CriteriaBuilder builder, Map<String, Object> addParams){};
    
    public Metadates getMetadatesObj() {
        return metadatesFacade.find(getMetadatesObjId());
    }    
        
    protected abstract Integer getMetadatesObjId();
    public abstract String getFRM_NAME();
    
    /* возвращает список изменённых пользователем документов */
    public List<T> findLastChangedItemsByUser(User user){    
        Date lastDate = DateUtils.addMounth(new Date(), -1);

        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> rootItems = cq.from(itemClass);

        Subquery<L> sq = cq.subquery(logClass);
        Root<L> rootLog = sq.from(logClass);        

        Expression expItemId = rootLog.get("item").get("id");
        Expression expUser = rootLog.get("userId");
        Expression expDate = rootLog.get("dateEvent");
        Expression expMaxDate = builder.max(expDate);

        sq.select(expItemId);
        sq.where(builder.equal(expUser, user));
        sq.groupBy(expItemId);
        sq.having(builder.greaterThan(expMaxDate, lastDate));

        cq.select(rootItems);
        cq.where(rootItems.get("id").in(sq));        
 
        Query q = getEntityManager().createQuery(cq);
        List<T> results = q.getResultList();

        return results;
    }    

    public List<T> findByIds(List<Integer> ids){
        if (!ids.isEmpty()){
            getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
            CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> cq = builder.createQuery(itemClass);
            Root<T> c = cq.from(itemClass);
            
            List<Predicate> criteries = new ArrayList<>();

            criteries.add(c.get("id").in(ids)); 
            criteries.add(builder.equal(c.get("deleted"), false));
            criteries.add(builder.equal(c.get("actual"), true));
                    
            Predicate[] predicates = new Predicate[criteries.size()];
            predicates = criteries.toArray(predicates);

            cq.select(c).where(builder.and(predicates));               
            cq.orderBy(builder.asc(c.get("name")));                   
            
            Query q = getEntityManager().createQuery(cq);       
            return q.getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public abstract void replaceItem(T oldItem, T newItem);
}