package com.maxfill.facade;

import com.google.gson.Gson;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.TaskStates;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task_;
import com.maxfill.model.task.result.Result;
import com.maxfill.utils.DateUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.EJB;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import liquibase.util.StringUtils;

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
    
    public List<Task> findTaskByStaffStates(Staff staff, List<State> states){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> root = cq.from(Task.class); 
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get(Task_.owner), staff));        
        criteries.add(root.get("state").get("currentState").in(states));        
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(root).where(builder.and(predicates));
        TypedQuery<Task> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
        
    public Metadates getMetadatesObj() {
        return metadatesFacade.find(22);
    }   
    
    /**
     * Формирование даты следующего напоминания для задачи 
     * @param task 
     */
    public void makeNextReminder(Task task){
        switch (task.getReminderType()){
            case "singl":{
                task.setNextReminder(null);
                break;
            }
            case "repeat":{
                makeReminder(task);
                break;
            }
        }        
    }  
    
    /**
     * Формирование даты напоминания для задачи 
     * @param task 
     */
    public void makeReminder(Task task){
        boolean isChange = false;
        Date nextReminder = null;
        switch (task.getReminderType()){
            case "no":{                
                break;
            }
            case "singl":{                
                nextReminder = DateUtils.addSeconds(task.getPlanExecDate(), -1 * task.getDeltaReminder()); //вычисляем дату напоминания                    
                isChange = true;                 
                break;
            }
            case "repeat":{
                switch (task.getReminderRepeatType()){
                    case "everyday":{
                        nextReminder = DateUtils.addDays(DateUtils.today(), 1);
                        nextReminder = DateUtils.addSeconds(nextReminder, task.getDeltaReminder());
                        isChange = true;
                        break;
                    }
                    case "everyweek":{
                        if (StringUtils.isNotEmpty(task.getReminderDays())){
                            String[] days = task.getReminderDays().split(",");
                            nextReminder = DateUtils.getNextDateByDays(days, DateUtils.today());
                            isChange = true;
                        }
                        break; 
                    }
                }                    
                break;
            }
        }
        if (isChange){
            Task source = find(task.getId());
            source.setNextReminder(nextReminder);
            edit(source);
        }
    }
}
