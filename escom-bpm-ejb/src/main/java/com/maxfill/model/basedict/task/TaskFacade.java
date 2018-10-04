package com.maxfill.model.basedict.task;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.core.states.State_;
import com.maxfill.services.worktime.WorkTimeService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * Фасад для сущности "Поручения"
 */
@Stateless
public class TaskFacade extends BaseDictWithRolesFacade<Task, Staff, TaskLog, TaskStates>{
    
    @EJB
    private WorkTimeService workTimeService;
    
    public TaskFacade() {
        super(Task.class, TaskLog.class, TaskStates.class);
    }

    public Task createTask(String taskName, Staff owner, User author, Date planDate){        
        Task task = createItem(author, null, owner, new HashMap<>());
        task.setName(taskName);
        task.setDeadLineType("data");        
        task.setPlanExecDate(planDate);        
        task.setAvaibleResultsJSON("[1]");
        return task;
    } 
    
    /**
     * Создание задачи из процесса
     * @param owner
     * @param author
     * @param process
     * @param scheme
     * @param taskLinkUID
     * @return 
     */
    public Task createTaskInProc(Staff owner, User author, Process process, Scheme scheme, String taskLinkUID){
        Task task = createItem(author, null, owner, new HashMap<>());        
        task.setName(process.getOwner().getDefaultTaskName());  
        task.setTaskLinkUID(taskLinkUID);
        if (process.getOwner().getDefaultDeltaDeadLine() != null && process.getOwner().getDefaultDeltaDeadLine() > 0){
            task.setDeltaDeadLine(process.getOwner().getDefaultDeltaDeadLine());
            task.setDeadLineType("delta");
        } else {
            task.setDeadLineType("data");
            task.setPlanExecDate(process.getPlanExecDate());
        }
        if (scheme != null){
            task.setScheme(scheme);
            task.setAvaibleResultsJSON(scheme.getProcess().getOwner().getAvaibleResultsJSON());   
        } else {
            task.setAvaibleResultsJSON("[1]");
        }
        return task; 
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
        List<Task> results = q.getResultList();
        return results;
    }           
    
    /**
     * Формирование даты планового срока исполнения. Учитывается рабочее время
     * @param task 
     * @param locale 
     */
    public void makeDatePlan(Task task, Locale locale){
        Integer deltasec = task.getDeltaDeadLine();
        Date startDate = task.getBeginDate();
        Date planExecDate = workTimeService.calcWorkDay(startDate, deltasec, task.getOwner(), locale);
        task.setPlanExecDate(planExecDate);
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
                        Date today = DateUtils.addTime(DateUtils.today(), task.getReminderTime());
                        if (today.after(new Date())){
                            nextReminder = today;
                            isChange = true;
                        } else {
                            nextReminder = DateUtils.addDays(DateUtils.today(), 1);                        
                            nextReminder = DateUtils.addTime(nextReminder, task.getReminderTime());
                        isChange = true;
                        }
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
            //Task source = find(task.getId());
            task.setNextReminder(nextReminder);
            //edit(source);
        }
    }

    /**
     * Проверка на наличие дубликата задачи. Отключена
     * @param item
     * @return 
     */
    @Override
    public Tuple findDublicateExcludeItem(Task item){
        return new Tuple(false, null);
    }
    
    /**
     * Установка роли Исполнитель значением указаного пользователя
     * @param task
     * @param user 
     */
    public void inicializeExecutor(Task task, User user){
        task.doSetSingleRole(DictRoles.ROLE_EXECUTOR, user.getId());
        doSaveRoleToJson(task);
    }
     
    /**
     * Установка признаков что задача выполнена
     * @param task
     * @param result 
     * @param user 
     */
    public void taskDone(Task task, Result result, User user){
        task.setResult(result.getName());
        //task.setIconName("");
        task.setFactExecDate(new Date());        
        task.getState().setCurrentState(stateFacade.getCompletedState());
        addLogEvent(task, DictLogEvents.TASK_FINISHED, user);
    }
    
    /**
     * Вычисляет число задач указанного пользователя, находящихся у него на исполнении
     * @param staff
     * @return 
     */
    public Long getCountExecTasksByUser(Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root root = cq.from(Task.class);
        List<Predicate> crit = new ArrayList<>();
        crit.add(builder.equal(root.get("deleted"), false));
        crit.add(builder.equal(root.get("actual"), true));
        crit.add(builder.equal(root.get(Task_.owner), staff));
        crit.add(builder.equal(root.get("state").get("currentState").get("id"), DictStates.STATE_RUNNING));
        Predicate[] predicates = new Predicate[crit.size()];
        predicates = crit.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    /* *** ПРОЧЕЕ *** */  
    
    public Long findCountStaffLinks(Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Task> root = cq.from(Task.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Task_.owner), staff));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
    }

    @Override
    public int replaceItem(Task oldItem, Task newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_TASK;
    }

    @Override
    protected String getItemFormPath(){
        return "/processes/monitor.xhtml";
    }  
}
