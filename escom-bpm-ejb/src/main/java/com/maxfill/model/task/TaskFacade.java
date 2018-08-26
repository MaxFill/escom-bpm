package com.maxfill.model.task;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.users.User;
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
        return createTask(taskName, owner, author, planDate, null, null);
    }   
    public Task createTask(String taskName, Staff owner, User author, Date planDate, Scheme scheme, String taskLinkUID){
        Task task = createItem(author, null, owner, new HashMap<>());        
        task.setName(taskName);
        task.setDeadLineType("data");
        task.setTaskLinkUID(taskLinkUID);
        task.setPlanExecDate(planDate);
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
        task.doSetSingleRole(DictRoles.ROLE_EXECUTOR, user);
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
        task.setIconName(result.getIconName());
        task.setFactExecDate(new Date());        
        task.getState().setCurrentState(stateFacade.getCompletedState());
        addLogEvent(task, DictLogEvents.TASK_FINISHED, user);
    }
    
    /* *** ПРОЧЕЕ *** */
    
    @Override
    public Class<Task> getItemClass() {
        return Task.class;
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
    public String getFRM_NAME() {
        return DictObjectName.TASK.toLowerCase();
    }
}
