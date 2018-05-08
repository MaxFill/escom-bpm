package com.maxfill.facade;

import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.process.schemes.task.TaskStates;
import com.maxfill.model.process.schemes.task.Task_;
import com.maxfill.model.staffs.Staff;
import java.util.List;
import javax.ejb.EJB;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Поручения"
 */
@Stateless
public class TaskFacade extends BaseLazyLoadFacade<Task>{

    @EJB
    private MetadatesFacade metadatesFacade; 
    
    public TaskFacade() {
        super(Task.class);
    }

    public Task createTask(String taskName, Staff owner){
        return createTask(taskName, owner, null, null);
    }   
    public Task createTask(String taskName, Staff owner, Scheme scheme, String taskLinkUID){
        Task task = new Task();
        TaskStates taskStates = new TaskStates();        
        taskStates.setCurrentState(getMetadatesObj().getStateForNewObj());
        task.setState(taskStates);
        task.setName(taskName);
        task.setOwner(owner);
        task.setScheme(scheme);
        task.setTaskLinkUID(taskLinkUID);
        return task; 
    }
    
    public Task findByLinkUID(String linkUID){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> c = cq.from(Task.class);        
        Predicate crit1 = builder.equal(c.get(Task_.taskLinkUID), linkUID);        
        cq.select(c).where(builder.and(crit1));
        TypedQuery<Task> q = getEntityManager().createQuery(cq);       
        return q.getResultList().stream().findFirst().orElse(null); 
    }
    
    public List<Task> findTaskByStaff(Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> c = cq.from(Task.class);        
        Predicate crit1 = builder.equal(c.get(Task_.owner), staff);        
        cq.select(c).where(builder.and(crit1));
        TypedQuery<Task> q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    public Metadates getMetadatesObj() {
        return metadatesFacade.find(22);
    }   
}
