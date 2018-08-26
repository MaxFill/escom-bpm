package com.maxfill.services.worktime;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.staffs.Staff;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
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
        getEntityManager().getEntityManagerFactory().getCache().evict(WorkTimeCalendar.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<WorkTimeCalendar> cq = builder.createQuery(WorkTimeCalendar.class);
        Root<WorkTimeCalendar> root = cq.from(WorkTimeCalendar.class);
        Predicate crit1 = builder.equal(root.get(WorkTimeCalendar_.dateCalendar), date);
        Predicate crit2 = builder.isNull(root.get("staff"));
        cq.select(root).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
    
    /**
     * Поиск даты в календаре рабочего времени для указанной штатной единицы
     * @param date
     * @param staff
     * @return 
     */
    public List<WorkTimeCalendar> findDateByStaff(String date, Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(WorkTimeCalendar.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<WorkTimeCalendar> cq = builder.createQuery(WorkTimeCalendar.class);
        Root<WorkTimeCalendar> root = cq.from(WorkTimeCalendar.class);
        Predicate crit1 = builder.equal(root.get(WorkTimeCalendar_.dateCalendar), date);
        Predicate crit2 = builder.equal(root.get("staff"), staff);
        cq.select(root).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
}
