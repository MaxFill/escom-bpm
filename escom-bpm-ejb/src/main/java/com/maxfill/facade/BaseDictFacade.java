package com.maxfill.facade;

import com.maxfill.Configuration;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.BaseLogTable;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.services.numerator.NumeratorService;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictRights;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.xml.bind.JAXB;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Абстрактный фасад для справочников
 * @author Maxim
 * @param <T>   //класс объекта
 * @param <O>   //класс владельца объекта
 * @param <L>   //класс таблицы лога
 */
public abstract class BaseDictFacade<T extends BaseDict, O extends BaseDict, L extends BaseLogTable> extends BaseFacade<T>{
    private final Class<T> itemClass; 
    private final Class<L> logClass;   
    
    @EJB
    private MetadatesFacade metadatesFacade; 
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected RightFacade rightFacade;    
    @EJB
    protected Configuration configuration;    
    
    /**
     * Выполняет замену объекта
     * @param oldItem
     * @param newItem
     * @return 
     */
    public abstract Map<String, Integer> replaceItem(T oldItem, T newItem);        
      
    public BaseDictFacade(Class<T> itemClass, Class<L> logClass) {
        super(itemClass);
        this.itemClass = itemClass;
        this.logClass = logClass;
    }  
    
    /**
     * Возвращает подчинённые объекты для владельца
     * @param owner
     * @return 
     */
    public List<T> findDetailItems(O owner){
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
    
    /**
     * Отбор объектов, созданных пользователем
     * @param user
     * @return 
     */
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
    
    /**
     * Отбор объектов по их владельцу
     * @param owner
     * @return 
     */
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
            
    /**
     * Отбор объектов находящихся в корзине
     * @return 
     */
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
 
    /**
     * Отбор не актуальных объектов
     * @return 
     */
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

    public abstract void pasteItem(T item, BaseDict recipient, Set<String> errors);
    public boolean isNeedCopyOnPaste(){
        return true;
    }
    
    /* ВСТАВКА: */
    protected void doPaste(T pasteItem, Set<String> errors){        
        prepCreate(pasteItem, pasteItem.getParent(), (O)pasteItem.getOwner(), errors, null);
        if (errors.isEmpty()){
            create(pasteItem);
        }
    }
    
    /* КОПИРОВАНИЕ: копирование объекта */
    public T doCopy(T sourceItem, User author){
        T newItem = createItem(null, author);
        try {
            BeanUtils.copyProperties(newItem, sourceItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return newItem;
    }
    
    /* СОЗДАНИЕ: подготовка к созданию нового объекта  */
    public void prepCreate(T newItem, BaseDict parent, BaseDict owner, Set<String> errors, Map<String, Object> params) {
        boolean isAllowedEditOwner = true;
        
        User user = newItem.getAuthor();
        
        if (owner != null) {
            actualizeRightItem(owner, user);
            isAllowedEditOwner = isHaveRightEdit(owner); //можно ли редактировать owner?
        }
        if (isAllowedEditOwner) {
            makeRightItem(newItem, user);
            if (isHaveRightCreate(newItem)) {
                newItem.setParent(parent);
                setSpecAtrForNewItem(newItem, params);
                addLogEvent(newItem, DictLogEvents.CREATE_EVENT, user);
            } else {
                String objName = ItemUtils.getBandleLabel(getMetadatesObj().getBundleName());
                String error = MessageFormat.format(ItemUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
                errors.add(error);
            }
        } else {
            String objName = ItemUtils.getBandleLabel(getMetadatesObj().getBundleName());
            String error = MessageFormat.format(ItemUtils.getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
    }
    
    /* СОЗДАНИЕ: подготовка к созданию нового объекта с открытием карточки. */
    public T createItemAndOpenCard(BaseDict parent, BaseDict owner, Map<String, Object> params, Set<String> errors){
        User author = (User)params.get("user");
        T newItem = createItem((O)owner, author);
        prepCreate(newItem, parent, owner, errors, params);               
        return newItem;
    } 
    
    /* СОЗДАНИЕ: cоздание объекта */
    public T createItem(BaseDict owner, User author) {
        try {
            State state = getMetadatesObj().getStateForNewObj();
            T item = itemClass.newInstance();
            item.setAuthor(author);
            item.setActual(true);
            item.setDeleted(false);
            item.setInherits(true);            
            item.setState(state);
            detectParentOwner(item, owner);
            return item;
        } catch (IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    protected void detectParentOwner(T item, BaseDict owner){
        item.setOwner(owner);
    }
             
    /* РЕДАКТИРОВАНИЕ: подготовка к редактированию объекта на карточке  */
    public BaseDict prepEditItem(BaseDict item, User user) {
        BaseDict cloneItem = (BaseDict)find(item.getId());   //получаем копию объекта для редактирования 
        makeRightItem(cloneItem, user);
        if (!isHaveRightEdit(cloneItem)){
            return null;
        }
        return cloneItem;
    }
    
    /* ПРОСМОТР: подготовка к просмотру объекта на карточке */
    public BaseDict prepViewItem(BaseDict item, User user) {
        BaseDict cloneItem = (BaseDict) find(item.getId());   //получаем копию объекта для просмотра 
        makeRightItem(cloneItem, user);        
        if (!isHaveRightView(cloneItem)){ 
            return null;
        }
        return cloneItem;
    }
    
    /* *** ПРАВА ДОСТУПА *** */ 
    
    /* ПРАВА ДОСТУПА: Установка прав дочерних объектов их владельцу  */
    public void settingRightForChild(T ownerItem, Rights newRight) {
        if (ownerItem != null && newRight != null) {
            ownerItem.setRightForChild(newRight);
            ownerItem.setXmlAccessChild(newRight.toString());
        }
    }
    
    /* ПРАВА ДОСТУПА: акутализация прав доступа объекта */
    public void actualizeRightItem(BaseDict item, User user){
        BaseDict freshItem = (BaseDict)find(item.getId()); //актуализируем объект, т.к. он может быть удалён!
        if (freshItem == null){
            item.setRightItem(null); //обнулим права у объекта так как он скорее всего удалён ...
            item.setRightMask(null);
            return;
        }
        Rights freshRights = getRightItem(freshItem, getDefaultRights());
        settingRightItem(item, freshRights, user);
    }    
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на просмотр объекта */
    public boolean isHaveRightView(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);         
    }     
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на создание объекта  */
    public boolean isHaveRightCreate(BaseDict item) {        
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CREATE);
    } 
    
    /* ПРАВА ДОСТУПА: формирование битовой маски доступа */
    private Integer makeAccessMask(Right right, Integer accessMask) {
        if (right.isRead()) {
            accessMask = accessMask | 8;
        }
        if (right.isUpdate()) {
            accessMask = accessMask | 16;
        }
        if (right.isCreate()) {
            accessMask = accessMask | 32;
        }
        if (right.isDelete()) {
            accessMask = accessMask | 64;
        }
        if (right.isChangeRight()) {
            accessMask = accessMask | 128;
        }
        return accessMask;
    }
    
    /* ПРАВА ДОСТУПА: проверяет вхождение текущего пользователя в группу */
    private boolean checkUserInGroup(Integer groupId, User user) {
        for (UserGroups group : user.getUsersGroupsList()) {
            if (group.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на редактирование объекта  */
    public Boolean isHaveRightEdit(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_EDIT);                 
    }
    
    /* ПРАВА ДОСТУПА: проверка маски доступа к объекту */
    public boolean checkMaskAccess(Integer mask, Integer right) {
        if (mask == null) {
            return false;
        }
        return (mask & right) == right;
    }
    
    /* ПРАВА ДОСТУПА: формирование прав для объекта  */
    public Rights makeRightItem(BaseDict item, User user) {
        return makeRightItem(item, getDefaultRights(), user);
    }
    
    /* ПРАВА ДОСТУПА: формирование прав для объекта  */
    public Rights makeRightItem(BaseDict item, Rights sourceRights, User user) {
        Rights rights = getRightItem(item, sourceRights);
        settingRightItem(item, rights, user); 
        return rights;
    }
    
    /* ПРАВА ДОСТУПА: Установка прав объекту для текущего пользователя в текущем состоянии объекта с актуализацией маски доступа  */
    public void settingRightItem(BaseDict item, Rights newRight, User user) {
        if (item != null) {
            item.setRightItem(newRight);
            Integer mask = getAccessMask(item.getState(), newRight, user);
            item.setRightMask(mask);
            item.setAccess(newRight.toString()); //сохраняем права в виде XML
        }
    }        
    
    /* ПРАВА ДОСТУПА: возвращает маску доступа пользователя  */
    public Integer getAccessMask(State state, Rights sourcesRight, User user) {
        Integer userId = user.getId();
        Integer accessMask = 0;
        for (Right right : sourcesRight.getRights()) {  //распарсиваем права 
            if (right.getState().equals(state)) {
                switch (right.getObjType()) {
                    case 1: {    //права указаны для пользователя
                        if (right.getObjId().equals(userId)) { //это текущий пользователь, добавляем ему права
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case 0: {    //права указаны для группы
                        if (checkUserInGroup(right.getObjId(), user)) {
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                }
                if (accessMask == 248) {
                    break; //дальше проверять права не нужно, т.к. установлены максимальные права
                }
            }
        }
        return accessMask;
    }    
    
    /* ПРАВА ДОСТУПА: получение прав объекта  */
    private Rights getRightItem(BaseDict item, Rights defaultRight) {
        Rights rights;
        if (item == null) {
            rights = defaultRight; //дефолтные права справочника
        } else if (!item.isInherits()) {
            rights = getActualRightItem(item);
        } else if (item.getOwner() != null) {
            rights = getRightItemFromOwner(item.getOwner(), defaultRight); //получаем права от владельца
        } else if (item.getParent() != null) {
            rights = getRightItemFromParent(item.getParent(), defaultRight); //получаем права от родителя
        } else {
            rights = defaultRight;
        }
        return rights;
    }
    
    /* ПРАВА ДОСТУПА: получение актуальных прав объекта  */
    private Rights getActualRightItem(BaseDict item) {
        Rights actualRight = (Rights) JAXB.unmarshal(new StringReader(item.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        return actualRight;
    }
    
    /* ПРАВА ДОСТУПА: получение (рекурсивное) прав объекта от его владельца,
     * если такого владельца не найдено, то возвращаются дефолтные права
     * справочника */
    public Rights getRightItemFromOwner(BaseDict ownerItem) {
        return getRightItemFromOwner(ownerItem, getDefaultRights());
    }

    /* ПРАВА ДОСТУПА: получение актуальных прав объекта от его владельца  */
    private Rights getRightItemFromOwner(BaseDict ownerItem, Rights defaultRight) {
        if (ownerItem == null) {
            return defaultRight;
        } else if (ownerItem.isInherits()) { //если владелец наследует права
            return getRightItemFromOwner(ownerItem.getParent(), defaultRight); //то идём в следующего Родителя !
        } else {// если права не наследуются
            return getActualRightItemFromOwner(ownerItem); //то получаем права
        }
    }
    
    /* ПРАВА ДОСТУПА: получение актуальных прав объекта от его владельца  */
    private Rights getActualRightItemFromOwner(BaseDict ownerItem) {
        //ownerItem = (O) getOwnerBean().getItemFacade().find(ownerItem); //получаем свежую копию владельца из базы
        //TODO Тут вероятно нужно через вызов абстрактного метода актуализировать данные по правам т.к. в XmlAccessChild ни хрена нет!
        Rights actualRight = (Rights) JAXB.unmarshal(new StringReader(ownerItem.getXmlAccessChild()), Rights.class);
        return actualRight;
    }
    
    /* ПРАВА ДОСТУПА: получение (рекурсивное) прав объекта от его родителя если такого родителя
    не найдено, то возвращаются права справочника */
    public Rights getRightItemFromParent(BaseDict item) {
        return getRightItemFromParent(item, getDefaultRights());
    }

    /* ПРАВА ДОСТУПА: */
    public Rights getRightItemFromParent(BaseDict item, Rights defaultRight) {
        if (item == null) {
            return defaultRight; //дефолтные права справочника
        } else if (item.getParent() != null) { //если есть родитель                
            BaseDict nextParent = item.getParent();
            if (nextParent.isInherits()) {
                return getRightItemFromParent(nextParent, defaultRight); //раз права наследуется, то идём в следующего родителя
            } else {
                return getRightItem(nextParent, defaultRight); //права не наследуется, получаем реальные права родителя этой папки
            }
        } else {
            return defaultRight; //не нашли родителя, который не наследует прав, поэтому получаем дефолтные
        }
    }
              
    
    /* ПРАВА ДОСТУПА: установка прав при загрузке объекта*/
    public Boolean preloadCheckRightView(BaseDict item, User user){
        Rights rights = getRightItem(item, getDefaultRights());
        settingRightItem(item, rights, user);
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);
    }
    
    /* *** ЛОГИРОВАНИЕ ИЗМЕНЕНИЙ *** */

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
    
    /**
     * Создание записи лога
     * @param item
     * @param dateEvent
     * @param msgEvent
     * @param user
     * @return 
     */
    public L createLogEvent(T item, Date dateEvent, String msgEvent, User user){
        try {
            L logEvent = logClass.newInstance();
            logEvent.setEvent(msgEvent);
            logEvent.setDateEvent(dateEvent);
            logEvent.setUserId(user);
            logEvent.setItem(item);
            return logEvent;
        } catch (IllegalAccessException | InstantiationException ex) {
            Logger.getLogger(BaseDictFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public boolean addItemToGroup(T item, BaseDict targetGroup){ 
        return false;
    }    
        
    /* СОЗДАНИЕ: установка специфичных атрибутов при создании объекта  */ 
    public void setSpecAtrForNewItem(T item, Map<String, Object> params) {}
    
    @Override
    public void remove(T entity){
        entity = getEntityManager().getReference(itemClass, entity.getId());
        getEntityManager().remove(entity);
    }
        
    public List<T> getByParameters(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, BaseDataModel model) {
        CriteriaQuery<T> criteriaQuery = selectQueryByParameters(paramEQ, paramLIKE, paramIN, paramDATE, itemClass, model);
        TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
        return query.getResultList();
    }

    protected <EC> CriteriaQuery<EC> selectQueryByParameters(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Class<EC> entityClass, BaseDataModel model) {
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<EC> criteriaQuery = builder.createQuery(entityClass);
        Root<EC> root = criteriaQuery.from(entityClass);
        
        List<Predicate> criteries = new ArrayList<>(); 
        criteries.add(builder.equal(root.get("deleted"), false));
        
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
            criteries.add(builder.between(root.get(parameter.getKey()), dateStart, DateUtils.addDays(dateEnd, 1)));
        }
        
        addJoinPredicatesAndOrders(root, criteries, builder, model);
        
        criteriaQuery.orderBy(builder.asc(root.get("name")));
        
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return criteriaQuery.select(root).where(builder.and(predicates));        
    }
    
    protected abstract void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates,  CriteriaBuilder builder, BaseDataModel model);    
    
    public Metadates getMetadatesObj() {
        return metadatesFacade.find(getMetadatesObjId());
    }    
        
    public Rights getDefaultRights(){
        return rightFacade.getObjectDefaultRights(getMetadatesObj());
    }
        
    protected abstract Integer getMetadatesObjId();
    public abstract String getFRM_NAME();
    
    /**
     * Отбор объектов, изменённых пользователем
     * @param user 
     * @return  
     */
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

    /**
     * Получение списка объектов по их Id
     * @param ids
     * @return 
     */
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
    
}
