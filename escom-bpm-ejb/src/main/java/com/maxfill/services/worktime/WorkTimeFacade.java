package com.maxfill.services.worktime;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.basedict.staff.Staff;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Календарь рабочего времени"
 * @author maksim
 */
@Stateless
public class WorkTimeFacade extends BaseFacade<WorkTimeCalendar>{
    
    public WorkTimeFacade() {
        super(WorkTimeCalendar.class);
    }
    
    /**
     * Поиск даты в календаре рабочего времени
     * @param date
     * @return 
     */
    public List<WorkTimeCalendar> findDate(String date){
        em.getEntityManagerFactory().getCache().evict(WorkTimeCalendar.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<WorkTimeCalendar> cq = builder.createQuery(WorkTimeCalendar.class);
        Root<WorkTimeCalendar> root = cq.from(WorkTimeCalendar.class);
        Predicate crit1 = builder.equal(root.get(WorkTimeCalendar_.dateCalendar), date);
        Predicate crit2 = builder.isNull(root.get("staff"));
        cq.select(root).where(builder.and(crit1, crit2));
        Query q = em.createQuery(cq);
        return q.getResultList();
    }
    
    /**
     * Поиск даты в календаре рабочего времени для указанной штатной единицы
     * @param date
     * @param staff
     * @return 
     */
    public List<WorkTimeCalendar> findDateByStaff(String date, Staff staff){
        em.getEntityManagerFactory().getCache().evict(WorkTimeCalendar.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<WorkTimeCalendar> cq = builder.createQuery(WorkTimeCalendar.class);
        Root<WorkTimeCalendar> root = cq.from(WorkTimeCalendar.class);
        Predicate crit1 = builder.equal(root.get(WorkTimeCalendar_.dateCalendar), date);
        Predicate crit2 = builder.equal(root.get("staff"), staff);
        cq.select(root).where(builder.and(crit1, crit2));
        Query q = em.createQuery(cq);
        return q.getResultList();
    }
    
    /**
     * Удаление из календаря записей, для указаной штатной единицы
     * @param staff
     * @return 
     */
    public int removeWorkTimeByStaff(Staff staff){
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaDelete<WorkTimeCalendar> cd = builder.createCriteriaDelete(itemClass);
        Root root = cd.from(itemClass);
        Predicate crit1 = builder.equal(root.get(WorkTimeCalendar_.staff), staff);
        cd.where(crit1);
        Query query = em.createQuery(cd);
        return query.executeUpdate();
    }
}