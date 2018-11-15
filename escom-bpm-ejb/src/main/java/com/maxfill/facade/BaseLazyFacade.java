package com.maxfill.facade;

import com.maxfill.model.Dict;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.user.User;
import org.apache.commons.lang3.StringUtils;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * Абстрактный фасад для работы с ленивой загрузки списков данных из таблиц БД
 * @param <T>
 */
public abstract class BaseLazyFacade<T extends Dict> extends BaseFacade<T>{

    public BaseLazyFacade(Class<T> itemClass){
        super(itemClass);
    }
    
    /**
     * Возвращает число записей журнала в заданном диаппазоне
     * @param filters
     * @return
     */
    public int countItemsByFilters(Map<String,Object> filters){
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<T> root = cq.from(itemClass);
        cq.select(builder.count(root)).where(builder.and(makePredicates(builder, root, filters)));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Отбор заданного числа записей объектов по критериям с сортировкой
     * @param firstPosition
     * @param numberOfRecords
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    public List<T> findItemsByFilters(int firstPosition, int numberOfRecords, String sortField, String sortOrder, Map<String,Object> filters) {
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
        Query query = em.createQuery(cq);
        query.setFirstResult(firstPosition);
        query.setMaxResults(numberOfRecords);        
        return query.getResultList();
    }
    
    /**
     * Отбор записей объектов по критериям с сортировкой
     * @param sortField
     * @param sortOrder
     * @param filters
     * @param currentUser
     * @return
     */
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
        Query query = em.createQuery(cq);
        List<T> result = query.getResultList();
        return result;
    }
    
    /**
     * Очистка журнала за указанный период времени
     * @param filters
     * @return 
     */
    public int deleteItems(Map<String,Object> filters) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaDelete<T> cd = builder.createCriteriaDelete(itemClass);
        Root root = cd.from(itemClass);
        cd.where(makePredicates(builder, root, filters));
        Query query = em.createQuery(cd);
        return query.executeUpdate();
    }

    /**
     * Формирование условий для запросов
     * @param builder
     * @param root
     * @param filters
     * @return
     */
    protected Predicate[] makePredicates(CriteriaBuilder builder, Root root, Map<String,Object> filters) {
        List<Predicate> criteries = new ArrayList<>();

        if(filters != null) {
            for(Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                String filterProperty = it.next();
                Object filterValue = filters.get(filterProperty);
                Predicate predicate = null;
                if (filterValue instanceof Map){
                        Map <String, Date> dateFilters = (Map) filterValue;                    
                        if (dateFilters.containsKey("startDate")){
                            predicate = builder.greaterThanOrEqualTo(root.get(filterProperty), dateFilters.get("startDate"));
                            criteries.add(predicate);
                        }
                        if (dateFilters.containsKey("endDate")){
                            predicate = builder.lessThanOrEqualTo(root.get(filterProperty), dateFilters.get("endDate"));
                            criteries.add(predicate);
                        }
                        predicate = null;
                    } else {                    
                        if(filterValue != null) {                       
                            switch (filterProperty){
                                case "states":{
                                    List<State> states = (List<State>)filterValue;
                                    if (!states.isEmpty()){
                                        predicate = root.get("state").get("currentState").in(states);                                    
                                    }
                                    break;
                                }
                                case "procResults":{
                                    List<String> procResults = (List<String>)filterValue;
                                    if (!procResults.isEmpty()){
                                        predicate = root.get("result").in(procResults);                                    
                                    }
                                    break;
                                }
                                case "regNumber":{
                                    predicate = builder.like(root.<String>get(filterProperty),"%"+filterValue+"%");                                
                                    break;
                                }
                                case "name":{
                                    predicate = builder.like(root.<String>get(filterProperty),"%"+filterValue+"%");                                
                                    break;
                                }
                                default:{
                                    predicate = builder.equal(root.get(filterProperty), filterValue);
                                }
                            }                 
                        } else {
                            predicate = builder.isNull(root.get(filterProperty));
                        }
                    }
                if (predicate != null){
                    criteries.add(predicate);
                }
            }
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return predicates;
    }
}