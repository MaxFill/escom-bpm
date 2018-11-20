package com.maxfill.facade;

import com.maxfill.model.Dict;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/* Абстрактный фасад  */
public abstract class BaseFacade<T extends Dict> {
    protected final Class<T> itemClass;
    protected static final Logger LOGGER = Logger.getLogger(BaseFacade.class.getName());
    
    @PersistenceContext(unitName = "com.maxfill.escombpm2PU")
    protected EntityManager em;
    
    /*
     @Resource (lookup = "java:jboss/ee/concurrency/executor/default")
     private ManagedExecutorService executorService;
    */    
        
    public BaseFacade(Class<T> itemClass) {
        this.itemClass = itemClass;
    }

    public Class<T> getItemClass(){
        return itemClass;
    }        
    
    public T clone(Object id){
        return em.find(itemClass, id);
    }
    
    synchronized public void create(T entity) {
        /*
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
        if(constraintViolations.size() > 0){
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while(iterator.hasNext()){
                ConstraintViolation<T> cv = iterator.next();
                System.err.println("PERSIST_VALIDATE_ERR:"+cv.getRootBeanClass().getName()+"."+cv.getPropertyPath() + " " +cv.getMessage());
            }
        } else {
            em.persist(entity);
        }
        */
        em.persist(entity);
    }

    synchronized public void edit(T entity) {
        em.merge(entity);
    }

    synchronized public void remove(T entity) {
        entity = em.getReference(itemClass, entity.getId());
        em.remove(entity);
    }

    public int count() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(itemClass);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }    
    
    public T find(Object id) {        
        em.getEntityManagerFactory().getCache().evict(itemClass); 
        return (T) em.find(itemClass, id);       
    }    

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(itemClass));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    /**
     * Отбирает объекты по их идентификаторам
     * @param ids
     * @param currentUser
     * @return 
     */
    public List<T> findByIds(Collection<Integer> ids, User currentUser){
        if (ids.isEmpty()) return new ArrayList<>(); 
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root root = cq.from(itemClass);
        cq.select(root).where(builder.and(root.get("id").in(ids)));
        Query query = em.createQuery(cq);       
        return query.getResultList();
    }    
    
    /* Возвращает все дочерние элементы для parent */
    public List<T> findAllChilds(T parent){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get("parent"), parent);
        cq.select(c).where(builder.and(crit1));
        cq.orderBy(orderBuilder(builder, c));
        Query q = em.createQuery(cq);
        return q.getResultList();
    }

    /**
     * Определяет дефолтный порядок сортировки данных
     * @param builder
     * @param root
     * @return
     */
    protected List<Order> orderBuilder(CriteriaBuilder builder, Root root){
        List<Order> orderList = new ArrayList<>();
        orderList.add(builder.asc(root.get("name")));
        return orderList;
    }
    
    /**
     * Поиск объекта по его имени в поле name
     * @param name
     * @return 
     */
    public List<T> findByName(String name){
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(itemClass);
        Root<T> root = cq.from(itemClass);
        Predicate crit1 = builder.equal(root.get("name"), name);
        cq.select(root).where(builder.and(crit1));        
        Query q = em.createQuery(cq);
        return q.getResultList();
    }
}