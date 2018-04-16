package com.maxfill.facade.base;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

public abstract class BaseLazyLoadFacade<T> extends BaseFacade<T>{

    public BaseLazyLoadFacade(Class<T> itemClass){
        super(itemClass);
    }

    /**
     * Возвращает число записей журнала в заданном диаппазоне
     * @param startDate
     * @param endDate
     * @param filters
     * @return
     */
    public int countEvents(Date startDate, Date endDate, Map<String,Object> filters){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery();
        Root<T> root = cq.from(entityClass);
        cq.select(builder.count(root)).where(builder.and(makePredicates(builder, root, startDate, endDate, filters)));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    /**
     * Загрузка событий атентификации за определённый период времени
     * @param startDate
     * @param endDate
     * @param firstPosition
     * @param numberOfRecords
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    public List<T> findItemsByPeriod(Date startDate, Date endDate, int firstPosition, int numberOfRecords, String sortField, String sortOrder, Map<String,Object> filters) {
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = builder.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root).where(builder.and(makePredicates(builder, root, startDate, endDate, filters)));
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
     * Очистка журнала за указанный период времени
     * @param startDate
     * @param endDate
     * @param filters
     */
    public int clearEvents(Date startDate, Date endDate, Map<String,Object> filters) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<T> cd = builder.createCriteriaDelete(entityClass);
        Root root = cd.from(entityClass);
        cd.where(makePredicates(builder, root, startDate, endDate, filters));
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }

    /**
     * Формирование условий для запросов
     * @param builder
     * @param root
     * @param startDate
     * @param endDate
     * @param filters
     * @return
     */
    private Predicate[] makePredicates(CriteriaBuilder builder, Root root, Date startDate, Date endDate, Map<String,Object> filters) {
        List<Predicate> criteries = new ArrayList<>();
        if (startDate != null && endDate != null) {
            criteries.add(builder.between(root.get(getFieldDateCrit()), startDate, endDate));
        }

        if(filters != null) {
            for(Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                String filterProperty = it.next();
                Object filterValue = filters.get(filterProperty);
                criteries.add(builder.equal(root.get(filterProperty), filterValue));
            }
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        return predicates;
    }

    protected abstract SingularAttribute<T, Date> getFieldDateCrit();
}
