package com.maxfill.facade;

import com.maxfill.model.Dict;
import com.maxfill.model.states.State;
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
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<T> root = cq.from(itemClass);
        cq.select(builder.count(root)).where(builder.and(makePredicates(builder, root, filters)));
        Query q = getEntityManager().createQuery(cq);
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
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
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
        Query query = getEntityManager().createQuery(cq);
        query.setFirstResult(firstPosition);
        query.setMaxResults(numberOfRecords);        
        return query.getResultList();
    }
    
    /**
     * Отбор записей объектов по критериям с сортировкой
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    public List<T> findItemsByFilters(String sortField, String sortOrder, Map<String,Object> filters) {
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
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
        Query query = getEntityManager().createQuery(cq);
        List<T> result = query.getResultList();
        return result;
    }
    
    /**
     * Очистка журнала за указанный период времени
     * @param filters
     * @return 
     */
    public int deleteItems(Map<String,Object> filters) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<T> cd = builder.createCriteriaDelete(itemClass);
        Root root = cd.from(itemClass);
        cd.where(makePredicates(builder, root, filters));
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }

    /**
     * Формирование условий для запросов
     * @param builder
     * @param root
     * @param filters
     * @return
     */
    private Predicate[] makePredicates(CriteriaBuilder builder, Root root, Map<String,Object> filters) {
        List<Predicate> criteries = new ArrayList<>();

        if(filters != null) {
            for(Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                String filterProperty = it.next();
                Object filterValue = filters.get(filterProperty);
                if (filterValue instanceof Map){
                    Map <String, Date> dateFilters = (Map) filterValue;                    
                    if (dateFilters.containsKey("startDate")){
                        criteries.add(builder.greaterThanOrEqualTo(root.get(filterProperty), dateFilters.get("startDate")));
                    }
                    if (dateFilters.containsKey("endDate")){
                        criteries.add(builder.lessThanOrEqualTo(root.get(filterProperty), dateFilters.get("endDate")));
                    }
                } else {                    
                    if(filterValue != null) {
                        if ("states".equals(filterProperty)){
                            List<State> states = (List<State>)filterValue;
                            if (!states.isEmpty()){
                                Predicate predicate = root.get("state").get("currentState").in(states);
                                criteries.add(predicate);
                            }
                        } else {
                            criteries.add(builder.equal(root.get(filterProperty), filterValue));
                        }
                    } else {
                        criteries.add(builder.isNull(root.get(filterProperty)));
                    }
                }
            }
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return predicates;
    }
}