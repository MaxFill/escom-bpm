package com.maxfill.facade;

import com.maxfill.Configuration;
import com.maxfill.RightsDef;
import com.maxfill.dictionary.DictRights;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.core.metadates.MetadatesFacade;
import com.maxfill.model.core.roles.RoleFacade;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.BaseLogItems;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.core.rights.Right;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.core.states.BaseStateItem;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.user.User;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.EJB;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.xml.bind.JAXB;
import org.apache.commons.lang3.StringUtils;

/**
 * Абстрактный фасад для справочников
 * @author Maxim
 * @param <T>   //класс объекта
 * @param <O>   //класс владельца объекта
 * @param <L>   //класс таблицы лога
 * @param <S>   //класс таблицы состояний
 */
public abstract class BaseDictFacade<T extends BaseDict, O extends BaseDict, L extends BaseLogItems, S extends BaseStateItem> extends BaseLazyFacade<T>{    
    private final Class<L> logClass; 
    private final Class<S> stateClass;

    @EJB
    private MetadatesFacade metadatesFacade;
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected Configuration configuration;          
    @EJB
    protected StateFacade stateFacade;
    @EJB
    protected RightsDef rightsDef;
    @EJB
    protected RoleFacade roleFacade;
    @EJB
    protected UserFacade userFacade;

    public BaseDictFacade(Class<T> itemClass, Class<L> logClass, Class<S> stateClass) {
        super(itemClass);        
        this.logClass = logClass;
        this.stateClass = stateClass;
    }        
    
    @Override
    public void remove(T entity){
        removeItemLogs(entity);
        super.remove(entity);
    }        

    protected String getItemFormPath(){
        return "";
    }
    
    public Tuple findDublicateExcludeItem(T item){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass); 
        List<Predicate> criteries = new ArrayList<>();
        
        dublicateCheckAddCriteria(builder, c, criteries, item);
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(c).where(builder.and(predicates));        
        TypedQuery<T> query = em.createQuery(cq);
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

    /* РАБОТА С ЕДИНИЧНЫМ ОБЪЕКТОМ */

    /**
     * Создание нового объекта в памяти
     * @param author
     * @param parent
     * @param owner
     * @param params
     * @param errors
     * @return
     */
    public T createItem(User author, T parent, O owner, Map<String, Object> params) {
        try {
            T item = itemClass.newInstance();
            item.setAuthor(author);
            item.setActual(true);
            item.setDeleted(false);
            item.setInherits(true);            
            setRoleOwner(item, author);
            doSetState(item, getMetadatesObj().getStateForNewObj());
            detectParentOwner(item, parent, owner);
            setSpecAtrForNewItem(item, params);
            return item;
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }        

    protected void detectParentOwner(T item, BaseDict parent, BaseDict owner){
        item.setOwner(owner);
        item.setParent(parent);
    } 
    
    protected void setSpecAtrForNewItem(T item, Map<String, Object> params) {
        if (params.containsKey("name")){
            item.setName((String) params.get("name"));
        }
    }
    
    /* *** СОСТОЯНИЯ *** */
    
    /**
     * Изменение состояния объекта с сохранением
     * @param item
     * @param stateId
     */
    public void changeState(T item, int stateId){
        doSetState(item, stateFacade.find(stateId));
        edit(item);
    }
    
    /* установка состояния объекта */     
    private void doSetState(T item, State currentState){
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
    
    /**
     * Установка начального состояния объекта
     * @param item 
     */
    public void setFirstState(T item){
        doSetState(item, getMetadatesObj().getStateForNewObj());
    }
            
    /**
     * Возврат в предыдущее состояние
     * @param item 
     */
    public void returnToPrevState(T item){
        BaseStateItem stateItem = item.getState();
        State previousState = stateItem.getPreviousState();
        if (previousState != null){
            stateItem.setCurrentState(previousState);
            item.setState(stateItem);
        }
        edit(item);
    }

    /* ***  ЗАМЕНА *** */
    
    /**
     * Замена объекта на другой
     * @param oldItem
     * @param newItem
     * @return
     */
    public abstract int replaceItem(T oldItem, T newItem);

    /* *** *** */

    /**
     * Отбирает все записи, кроме удалённых в корзину и не актуальных 
     * @param currentUser
     * @return 
     */ 
    public List<T> findAll(User currentUser) {                        
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);        
        Predicate crit1 = builder.equal(c.get("actual"), true);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(orderBuilder(builder, c));
        TypedQuery<T> query = em.createQuery(cq);       
        return query.getResultStream()     
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    public List<T> findActualItemsByOwner(O owner, User currentUser){
        return findActualDetailItems(owner, 0, 0, "", "", currentUser);
    }
    
    /* Возвращает актуальные подчинённые объекты для владельца  */
    public List<T> findActualDetailItems(O owner, int first, int pageSize, String sortField, String sortOrder, User currentUser){
        //Внимание! поле sortField может не быть в таблице, поэтому сортировку я отключил!
        first = 0;
        pageSize = configuration.getMaxResultCount();
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> root = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        //criteries.add(builder.isNull(root.get("parent")));
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get("actual"), true));
        
        if (owner == null){
            criteries.add(builder.isNull(root.get("owner")));
        } else {
            criteries.add(builder.equal(root.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(root).where(builder.and(predicates));        

        TypedQuery<T> query = em.createQuery(cq);       
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }

    public Long findCountActualDetails(O owner){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<T> root = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(root.get("parent")));
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get("actual"), true));
        
        if (owner == null){
            criteries.add(builder.isNull(root.get("owner")));
        } else {
            criteries.add(builder.equal(root.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
                       
        cq.select(builder.count(root)).where(builder.and(predicates));

        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
            
    /**
     * Отбирает все подчинённые объекты 
     * @param owner
     * @return
     */
    public List<BaseDict> findDetailItems(T owner){
        return null;
    }
    
    /* Возвращает корневые объекты  */
    public Stream<T> findRootItems(User currentUser){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(c.get("parent")));
        criteries.add(builder.isNull(c.get("owner")));
        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<T> query = em.createQuery(cq);
                
        return query.getResultStream()
                .filter(item -> preloadCheckRightView((BaseDict) item, currentUser));
    }

    /* Отбор объектов, созданных пользователем  */
    public List<T> findItemsCreatedByUser(User currentUser, int first, int pageSize){
        first = 0;
        pageSize = configuration.getMaxResultCount();
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get("author"), currentUser);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<T> query = em.createQuery(cq); 
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }    
    
    public Long getCountDetails(BaseDict owner){
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        Root<T> root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get("owner"), owner);
        Predicate crit2 = builder.equal(root.get("deleted"), false);
        Predicate crit3 = builder.equal(root.get("actual"), true);
        cq.select(builder.count(root)).where(builder.and(crit1, crit2, crit3));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }    
    
    /* Отбор объектов находящихся в корзине */
    public List<T> loadFromTrash(int first, int pageSize, String sortField, String sortOrder, User currentUser){
        first = 0;
        pageSize = configuration.getMaxResultCount();
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> root = cq.from(itemClass); 
        Predicate crit2 = builder.equal(root.get("deleted"), true);
        cq.select(root).where(builder.and(crit2));
        TypedQuery<T> query = em.createQuery(cq);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return filtrationTrashResult(query.getResultStream())       
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());        
    }

    /**
     * Фильтрует результат отбора данных для корзины
     * Нужно не показывать в корзине child объекты если удалён их parent
     * и не показывать detail если удалён их owner
     * @param trashes
     * @return
     */
    protected Stream<T> filtrationTrashResult(Stream<T> trashes){
        return trashes.filter(item ->
                        (item.getParent() == null || (item.getParent() != null && !item.getParent().isDeleted())) &&
                        (item.getOwner() == null || (item.getOwner() != null && !item.getOwner().isDeleted()))
                );        
    }

    /* Отбор не актуальных объектов  */
    public List<T> loadNotActualItems(int first, int pageSize, User currentUser){
        first = 0;
        pageSize = configuration.getMaxResultCount();
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);        
        Predicate crit1 = builder.equal(c.get("actual"), false);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<T> query = em.createQuery(cq); 
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }                       
    
    /* Возвращает документы, заблокированные пользователем */
    public List<T> loadLockDocuments(User editor){
        return null;
    }
        
    /* *** ЛОГИРОВАНИЕ  *** */

    /**
     * Возвращает лог объекта
     * @param item
     * @param firstPosition
     * @param numberOfRecords
     * @param sortField
     * @param sortOrder
     * @return 
     */
    public List<L> getItemLogs(T item, int firstPosition, int numberOfRecords, String sortField, String sortOrder){
        em.getEntityManagerFactory().getCache().evict(logClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<L> cq = builder.createQuery(logClass);
        Root<L> root = cq.from(logClass);
        Predicate crit1 = builder.equal(root.get("item"), item);
        cq.select(root).where(builder.and(crit1));                
        if (StringUtils.isNotBlank(sortField)){ 
            if (StringUtils.isBlank(sortOrder) || !sortOrder.equals("DESCENDING")) {
                cq.orderBy(builder.asc(root.get(sortField)));
            } else {
                cq.orderBy(builder.desc(root.get(sortField)));
            }
        }
        TypedQuery query = em.createQuery(cq);
        query.setFirstResult(firstPosition);
        query.setMaxResults(numberOfRecords);
        return query.getResultList();
    }
    
    /**
     * Возвращает число записей лога объекта
     * @param item
     * @return
     */
    public int getCountItemLogs(T item){
        em.getEntityManagerFactory().getCache().evict(logClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root root = cq.from(logClass);
        Predicate crit1 = builder.equal(root.get("item"), item);
        cq.select(builder.count(root))
            .where(builder.and(crit1));
        TypedQuery q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    /* Добавление события в журнал событий */
    public void addLogEvent(T item, String msgKey, User user) {
        addLogEvent(item, msgKey, "", user);
    }
    
    /* Создание записи лога  */
    public void addLogEvent(T item, String template, String parameter, User user) {     
        try {
            L logEvent = logClass.newInstance();
            logEvent.setEvent(template);
            logEvent.setParams(parameter);
            logEvent.setDateEvent(new Date());
            logEvent.setUserId(user);
            logEvent.setItem(item);            
            em.persist(logEvent);
        } catch (IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
    }          

    /**
     * Очистка журнала объекта
     * @param entity     
     * @return 
     */
    public int removeItemLogs(T entity){        
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaDelete cd = builder.createCriteriaDelete(logClass);
        Root<L> root = cd.from(logClass);
        Predicate crit1 = builder.equal(root.get("item"), entity);
        cd.where(builder.and(crit1));
        Query query = em.createQuery(cd);
        return query.executeUpdate();    
    }
            
    /* ПОИСК из формы поиска */
    
    @Override
    public T find(Object id) { 
        return super.find(id);
    }

    public List<T> getByParameters(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams, int first, int pageSize, User currentUser) {
        first = 0;
        pageSize = configuration.getMaxResultCount();
        CriteriaQuery<T> criteriaQuery = selectQueryByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, itemClass, addParams);
        TypedQuery<T> query = em.createQuery(criteriaQuery);        
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    protected <EC> CriteriaQuery<EC> selectQueryByParameters(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Class<EC> itemClass, Map<String, Object> addParams) {
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<EC> criteriaQuery = builder.createQuery(itemClass);
        Root<EC> root = criteriaQuery.from(itemClass);
        
        criteriaQuery.orderBy(builder.asc(root.get("name")));
        
        Predicate[] predicates = makePredicate(builder, root, states, paramEQ, paramLIKE, paramIN, paramDATE, addParams);
        return criteriaQuery.select(root).where(builder.and(predicates));        
    }
    
    private Predicate[] makePredicate(CriteriaBuilder builder, Root root, List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){
        List<Predicate> criteries = new ArrayList<>(); 
        criteries.add(builder.equal(root.get("deleted"), false));
        
        if (!states.isEmpty()) { 
            Join stateJoin = root.join("state");
            Predicate predicate = stateJoin.get("currentState").in(states);
            criteries.add(predicate);
        }
        
        for (Map.Entry<String, Object> param : paramIN.entrySet()) { 
            Predicate predicate = root.get(param.getKey()).in(param.getValue());
            criteries.add(predicate);
        }                
        
        for (Map.Entry<String, Object> parameter : paramEQ.entrySet()) {
           criteries.add(builder.equal(root.get(parameter.getKey()), parameter.getValue()));
        }
        
        for (Map.Entry<String, Date[]> parameter : paramDATE.entrySet()) {            
            Date dateStart = parameter.getValue()[0];
            Date dateEnd = parameter.getValue()[1];
            Predicate betweenCrit = builder.between(root.get(parameter.getKey()), dateStart, dateEnd);
            criteries.add(betweenCrit);
        }

        addLikePredicates(root, criteries, builder, paramLIKE);
        addJoinPredicatesAndOrders(root, criteries, builder, addParams);
                       
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return predicates;
    }
    
    protected void addLikePredicates(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> paramLIKE){
        for (Map.Entry<String, Object> parameter : paramLIKE.entrySet()) {
            predicates.add(builder.like(root.<String>get(parameter.getKey()), (String) parameter.getValue()));
        }
    }

    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates,  CriteriaBuilder builder, Map<String, Object> addParams){};

    public Metadates getMetadatesObj() {
        return metadatesFacade.find(getMetadatesObjId());
    }       
    
    /**
     * Возвращает колво записей, в которых пользователь является автором
     * @param user
     * @return 
     */
    public Long findCountUserLinks(User user){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root root = cq.from(itemClass);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get("author"), user));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    /* возвращает список изменённых пользователем документов */
    public List<T> findLastChangedItemsByUser(User user, int first, int pageSize){    
        first = 0;
        pageSize = configuration.getMaxResultCount();
        
        Date lastDate = DateUtils.addMounth(new Date(), -1);

        CriteriaBuilder builder = em.getCriteriaBuilder();
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
        cq.where(builder.and(rootItems.get("id").in(sq), builder.equal(rootItems.get("deleted"), false))); 
 
        TypedQuery<T> query = em.createQuery(cq);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }    

    /**
     * Поиск объектов по их идентификаторам
     * @param ids
     * @param currentUser
     * @return 
     */
    @Override
    public List<T> findByIds(Collection<Integer> ids, User currentUser){
        if (!ids.isEmpty()){
            em.getEntityManagerFactory().getCache().evict(itemClass);
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = builder.createQuery(itemClass);
            Root<T> c = cq.from(itemClass);
            
            List<Predicate> criteries = new ArrayList<>();

            criteries.add(c.get("id").in(ids)); 
            criteries.add(builder.equal(c.get("deleted"), false));
            //criteries.add(builder.equal(c.get("actual"), true)); отключено потому что не отображаются не актуальные в избранном
                    
            Predicate[] predicates = new Predicate[criteries.size()];
            predicates = criteries.toArray(predicates);

            cq.select(c).where(builder.and(predicates));               
            cq.orderBy(builder.asc(c.get("name")));                   
            
            TypedQuery<T> query = em.createQuery(cq);       
            return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

     /* Возвращает только актуальные дочерние элементы для parent */
    public Stream<T> findActualChilds(T parent, User currentUser){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get("parent"), parent);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(orderBuilder(builder, c));
        TypedQuery query = em.createQuery(cq);
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser));
    } 

     /**
     * Отбор записей объектов по критериям с сортировкой
     * @param sortField
     * @param sortOrder
     * @param filters
     * @param currentUser
     * @return
     */
    @Override
    public List<T> findItemsByFilters(String sortField, String sortOrder, Map<String,Object> filters, User currentUser) {
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> root = cq.from(itemClass);
        cq.select(root).where(builder.and(makePredicates(builder, root, filters)));
        if (StringUtils.isNotBlank(sortField)){ //если задано по какому полю сортировать
            if (StringUtils.isBlank(sortOrder) || !sortOrder.equals("DESCENDING")) {
                cq.orderBy(builder.asc(root.get(sortField)));
            } else {
                cq.orderBy(builder.desc(root.get(sortField)));
            }
        }
        TypedQuery<T> query = em.createQuery(cq);        
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    /* *** ПРАВА ДОСТУПА *** */

    /* ПРАВА ДОСТУПА: Установка и проверка прав объекта для пользователя при загрузке объекта */
    protected Boolean preloadCheckRightView(BaseDict item, User user) {
        Rights rights = getRightItem(item, user);
        item.setRightItem(rights);
        Integer mask = getAccessMask((T)item, rights, user, 18);
        item.setRightMask(mask);        
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);
    }

    /* ПРАВА ДОСТУПА:
     * Установка прав объекту для пользователя в текущем состоянии объекта с актуализацией маски доступа
     */
    private void settingRightItem(BaseDict item, Rights newRight, User user) {
        if (item == null || user == null || newRight == null) return;
        item.setRightItem(newRight);
        Integer mask = getAccessMask((T)item, newRight, user, 1016);
        item.setRightMask(mask);
    }

    /* ПРАВА ДОСТУПА: Типовое получение прав объекта.
     * Актуально для линейных не подчинённых объектов  */
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user);
        }

        return getDefaultRights(item);
    }

    /* ПРАВА ДОСТУПА: получение дефолтных прав объекта */
    public Rights getDefaultRights(BaseDict item){
        return rightsDef.getDefaultRights(item.getClass().getSimpleName());
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на просмотр объекта */
    public boolean isHaveRightView(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на создание объекта  */
    public boolean isHaveRightCreate(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CREATE);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на редактирование объекта  */
    public Boolean isHaveRightEdit(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_EDIT);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на создание дочерних объектов */
    public Boolean isHaveRightAddChild(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_ADD_CHILDS);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на создание подчинённых объектов */
    public Boolean isHaveRightAddDetail(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_ADD_DETAIL);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на удаление объекта  */
    public Boolean isHaveRightDelete(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_DELETE);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на изменение прав доступа к объекту  */
    public boolean isHaveRightChangeRight(T item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CHANGE_RIGHT);
    }

    /**
     * Проверяет наличе у текущего пользователя права на выполнение
     * @param item
     * @return 
     */
    public boolean isHaveRightExec(T item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_EXECUTE);
    }
    
    /* ПРАВА ДОСТУПА: возвращает маску доступа пользователя к объекту в текущем состоянии  */
    public Integer getAccessMask(T item, Rights sourcesRight, User user, int maxRight) {
        State currentState = item.getState().getCurrentState();
        Integer userId = user.getId();
        Integer accessMask = 0;        
                
        for (Right right : sourcesRight.getRights()) {  //распарсиваем права
            if (currentState.equals(right.getState())) {
                switch (right.getObjType()) {
                    case DictRights.TYPE_USER: {    //права указаны для пользователя
                        if (right.getObjId().equals(userId) || userFacade.checkAssistant(right.getObjId(), user)) { //это текущий пользователь или его зам., то добавляем ему права
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case DictRights.TYPE_GROUP: {    //права указаны для группы
                        if (checkUserInGroup(right.getObjId(), user)) {
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case DictRights.TYPE_ROLE: {    //права указаны для роли
                        if (checkUserRole(item, right.getObjId(), user)) {
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                }
                if (accessMask >= maxRight) {
                    break; //дальше проверять права не нужно, т.к. установлены максимально требуемые права
                }
            }
        }
        return accessMask;
    }

    /* ПРАВА ДОСТУПА: формирование битовой маски доступа */
    private Integer makeAccessMask(Right right, Integer accessMask) {        
        if (right.isRead()) {
            accessMask = accessMask | DictRights.RIGHT_VIEW;
        } 
        if (right.isUpdate()) {
            accessMask = accessMask | DictRights.RIGHT_EDIT;
        } 
        if (right.isCreate()) {
            accessMask = accessMask | DictRights.RIGHT_CREATE;
        } 
        if (right.isDelete()) {
            accessMask = accessMask | DictRights.RIGHT_DELETE;
        } 
        if (right.isChangeRight()) {
            accessMask = accessMask | DictRights.RIGHT_CHANGE_RIGHT;
        }
        if (right.isAddChild()) {
            accessMask = accessMask | DictRights.RIGHT_ADD_CHILDS;
        }
        if (right.isAddDetail()) {
            accessMask = accessMask | DictRights.RIGHT_ADD_DETAIL;
        }
        if (right.isExecute()){
            accessMask = accessMask | DictRights.RIGHT_EXECUTE;
        }
        return accessMask;
    }

    /* ПРАВА ДОСТУПА: проверяет вхождение текущего пользователя в группу */
    private boolean checkUserInGroup(Integer groupId, User user) {
        if (groupId == 0) return true; //группа ВСЕ

        for (UserGroups userGroup : user.getUsersGroupsList()) {
            if (userGroup.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    /* ПРАВА ДОСТУПА: формирование прав для объекта  */
    public Rights makeRightItem(BaseDict item, User user) {
        Rights rights = getRightItem(item, user);
        settingRightItem(item, rights, user);
        return rights;
    }

    /* ПРАВА ДОСТУПА: акутализация прав доступа объекта */
    public void actualizeRightItem(BaseDict item, User user){
        BaseDict freshItem = find(item.getId()); //актуализируем объект, т.к. он может быть удалён!
        if (freshItem == null){
            item.setRightItem(null); //обнулим права у объекта так как он скорее всего удалён ...
            item.setRightMask(null);
            return;
        }
        Rights freshRights = getRightItem(freshItem, user);
        settingRightItem(item, freshRights, user);
    }

    /* ПРАВА ДОСТУПА: проверка маски доступа к объекту */
    public boolean checkMaskAccess(Integer mask, Integer right) {
        if (mask == null) return false;
        Integer m = mask & right;
        return m.equals(right);
    }

    /* ПРАВА ДОСТУПА: получение актуальных прав объекта для пользователя  */
    public Rights getActualRightItem(BaseDict item, User user) {
        if (item.getRightItem() != null){
            return item.getRightItem();
        }
        Rights actualRight = null;
        byte[] compressXML = item.getAccess();
        if (compressXML != null && compressXML.length >0){
            try {
                String accessXML = EscomUtils.decompress(compressXML);
                StringReader access = new StringReader(accessXML);
                actualRight = (Rights) JAXB.unmarshal(access, Rights.class);
                //settingRightItem(item, actualRight, user);
            } catch (IOException ex) {
                Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new NullPointerException("EscomERR: Object " + item.getName() + " dont have xml right!");
        }
        return actualRight;
    }

    /* ПРАВА ДОСТУПА: дефолтные права доступа к объекту */
    public Rights getDefaultRights(){
        return rightsDef.getDefaultRights(getItemClass().getSimpleName());
    }

    public void saveRights(T item, Rights rights){
        saveAccess(item, rights.toString());
    }
    
    public void saveAccess(T item, String xml){
        try {
            byte[] compressXML = EscomUtils.compress(xml);
            item.setAccess(compressXML);
        } catch (IOException ex) {
            Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAccessChild(T item, String xml){
        try {
            byte[] compressXML = EscomUtils.compress(xml);
            item.setAccessChild(compressXML);
        } catch (IOException ex) {
            Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Добавление к объекту прав на изменение для указанного пользователя
     * @param item
     * @param user
     * @param state - состояние, для которого создаётся право
     */
    public void addItemRightForUser(T item, User user, State state){
        Rights rights = getActualRightItem(item, user);
        Right right = new Right(DictRights.TYPE_USER, user.getId(), "", state, getMetadatesObj());
        right.setAddDetail(true);
        right.setAddChild(true);
        right.setUpdate(true);
        right.setRead(true);
        right.setChangeRight(false);
        right.setDelete(false);
        rights.getRights().add(right);
        saveRights(item, rights);
        edit(item);
    }
    
    /* ПРАВА ДЛЯ ДОЧЕРНИХ ОБЪЕКТОВ */

    /*
     * Формирование прав дочерних объектов
     */
    public Rights makeRightForChilds(T item){
        Rights childRights = getRightForChild(item);
        item.setRightForChild(childRights);
        return childRights;
    }

    /* Получение прав для дочерних объектов */
    public Rights getRightForChild(BaseDict item){
        return null; //метод переопределяется в фасадах древовидных сущностей
    }

    /* Получение актуальных прав дочерних объектов от объекта */
    protected Rights getActualRightChildItem(T item) {
        Rights actualRight = null;
        byte[] compressXML = item.getAccessChild();
        if (compressXML != null && compressXML.length >0){
            try {
                String accessXML = EscomUtils.decompress(compressXML);
                StringReader access = new StringReader(accessXML);
                actualRight = (Rights) JAXB.unmarshal(access, Rights.class);
            } catch (IOException ex) {
                Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return actualRight;
    }

    /* *** РОЛИ *** */
    
    /**
     * Проверяет вхождение текущего пользователя в роль, с учётом заместителей
     * @param item
     * @param groupId
     * @param user
     * @return 
     */    
    private boolean checkUserRole(T item, Integer groupId, User user) {        
        if (DictRoles.ROLE_OWNER_ID == groupId){
            return Objects.equals(item.getAuthor(), user);
        }        
        return checkUserInRole(item, groupId, user);
    }        
    
    /**
     * Стандартная проверка вхождения пользователя в роль
     * @param item
     * @param groupId
     * @param user
     * @return 
     */
    protected boolean checkUserInRole(T item, Integer groupId, User user){
        return false;
    }    
    
    public void setRoleOwner(T item, User user){
        item.doSetSingleRole(DictRoles.ROLE_OWNER, user.getId());
    }
            
    /* *** ПРОЧИЕ *** */
    
    protected abstract Integer getMetadatesObjId(); 
        
    public String getFRM_NAME(){
        return itemClass.getSimpleName().toLowerCase();
    }
        
    public String getItemHREF(T item){
        String url = ItemUtils.getItemURL(item, getItemFormPath(), configuration.getServerAppURL());
        StringBuilder links = new StringBuilder();
        links.append("<a href=").append(url).append(">").append(item.getName()).append("</a>");        
        return links.toString();
    }
}