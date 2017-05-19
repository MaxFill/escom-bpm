package com.maxfill.facade;

import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Абстрактный фасад
 * @author Maxim
 * @param <T>   //класс объекта
 */
public abstract class BaseFacade<T> {
    private final Class<T> entityClass;
    protected static final Logger LOGGER = Logger.getLogger(BaseFacade.class.getName());
    
    @PersistenceContext(unitName = "com.maxfill.escombpm2PU")
    private EntityManager entityManager;
    
    /*
    @Resource (lookup = "java:jboss/ee/concurrency/executor/default")
    private ManagedExecutorService executorService;
    */    
        
    public BaseFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager(){
        return entityManager;
    }
    
    public T clone(Object id){
        return getEntityManager().find(entityClass, id);
    }
    
    public void create(T entity) {
        /*
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
        if(constraintViolations.size() > 0){
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while(iterator.hasNext()){
                ConstraintViolation<T> cv = iterator.next();
                System.err.println("ESCOM_ERR:"+cv.getRootBeanClass().getName()+"."+cv.getPropertyPath() + " " +cv.getMessage());
            }
        }else{
            getEntityManager().persist(entity);
        }
*/
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(entity);
    }

    public T find(Object id) {        
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass); 
        return (T) getEntityManager().find(entityClass, id);       
    }
    
    /* Отбирает все записи кроме удалённых в корзину */
    public List<T> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> c = cq.from(entityClass);        
        Predicate crit1 = builder.equal(c.get("actual"), true);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    /**
     * Ищет объекты с наименованием itemName за исключением itemId на уровне равным parentId
     * @param itemId - исключаемый из поиска объект
     * @param parent - уровень, на котором выполняется поиск
     * @param itemName - искомое имя
     * @return true если есть нет таких объектов и false если есть такие объекты
     */
    public Tuple findByNameExcludeId(Integer itemId, T parent, String itemName){
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> c = cq.from(entityClass); 
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(c.get("name"), itemName));
        if (itemId != null){
            criteries.add(builder.notEqual(c.get("id"), itemId));
        }
        if (parent != null){
            criteries.add(builder.equal(c.get("parent"), parent));
        } else {
            criteries.add(builder.isNull(c.get("parent")));
        }
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
    
    /**
     * Возвращает дочерние объекты для указанного родителя
     * @param parent
     * @return 
     */ 
    public List<T> findChilds(T parent){
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> c = cq.from(entityClass);
        Predicate crit1 = builder.equal(c.get("parent"), parent);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }      
    
}
