package com.maxfill.model.basedict.task;

import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.services.worktime.WorkTimeService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private UserMessagesFacade messagesFacade;
    @EJB
    private StaffFacade staffFacade;
    
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
     * @param taskLinkUID
     * @return 
     */
    public Task createTaskInProc(Staff owner, User author, Process process, String taskLinkUID){
        Task task = createItem(author, null, owner, new HashMap<>());                  
        task.setTaskLinkUID(taskLinkUID);
        setDefaultTaskParams(task, process);
        if (StringUtils.isBlank(task.getAvaibleResultsJSON())){            
            task.setAvaibleResultsJSON("[1]");
        }
        return task; 
    }
    
    private void setDefaultTaskParams(Task task, Process process){
        ProcessType procType = processTypesFacade.getProcTypeForTasks(process.getOwner());
        task.setRoleInProc(procType.getDefaultTaskRole());
        task.setName(procType.getDefaultTaskName());
        task.setAvaibleResultsJSON(procType.getAvaibleResultsJSON()); 
        
        task.setDeltaDeadLine(0);
        if ("data".equals(process.getDeadLineType())){
            task.setDeadLineType("data");
            task.setPlanExecDate(process.getPlanExecDate());  
        } else {
            task.setDeadLineType("delta");
            task.setDeltaDeadLine(process.getDeltaDeadLine());
        }
         
        if (process.getScheme() != null){
            task.setScheme(process.getScheme());
        }
    }    
     
    /**
     * Отбор задач по штатной единице
     * @param staff
     * @return 
     */
    public List<Task> findTaskByStaff(Staff staff){
        em.getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> c = cq.from(Task.class);        
        Predicate crit1 = builder.equal(c.get(Task_.owner), staff);        
        cq.select(c).where(builder.and(crit1));
        TypedQuery<Task> q = em.createQuery(cq);       
        return q.getResultList();
    }
    
    /* Подсчёт кол-ва задач по статусам */
    public Tuple<Integer, Map<String, Integer>> countTaskStaffByStates(Staff staff, Date startPeriod, Date endPeriod){
        Map<String, Integer> result = new HashMap<>();
        if (staff == null || startPeriod == null || endPeriod == null){
            return new Tuple(0, result);
        }
        try {
            em.getEntityManagerFactory().getCache().evict(Task.class);
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Task> criteriaQuery = builder.createQuery(Task.class);
            Root root = criteriaQuery.from(Task.class);       
            Predicate crit1 = builder.equal(root.get("deleted"), false);
            Predicate crit2 = builder.greaterThanOrEqualTo(root.get("dateCreate"), startPeriod);
            Predicate crit3 = builder.lessThanOrEqualTo(root.get("dateCreate"), endPeriod);
            Predicate crit4 = builder.equal(root.get(Task_.owner), staff);
            criteriaQuery.where(builder.and(crit1, crit2, crit3, crit4));        
            TypedQuery<Task> query = em.createQuery(criteriaQuery);  

            AtomicInteger exeInTime = new AtomicInteger(0);     //в работе в срок
            AtomicInteger exeOverdue = new AtomicInteger(0);    //в работе просрочено
            AtomicInteger finisInTime = new AtomicInteger(0);   //завершено в срок
            AtomicInteger finishOverdue = new AtomicInteger(0); //завершено просрочено
            AtomicInteger exePlanToday = new AtomicInteger(0);  //срок истекает сегодняо
            query.getResultStream().forEach(task->{
                if (Objects.equals(task.getState().getCurrentState().getId(), DictStates.STATE_RUNNING)){
                    if (task.getPlanExecDate().before(new Date())){ //просрочено
                        exeOverdue.incrementAndGet();
                    } else 
                        if (task.getPlanExecDate().before(DateUtils.endDate(new Date()))){
                            exePlanToday.incrementAndGet();
                        } else {
                            exeInTime.incrementAndGet();
                        }
                } else 
                    if (Objects.equals(task.getState().getCurrentState().getId(), DictStates.STATE_COMPLETED)){
                        if (task.getPlanExecDate().after(task.getFactExecDate())){ //в срок
                            finisInTime.incrementAndGet();
                        } else {
                            finishOverdue.incrementAndGet();
                        }
                    }
            });        
            result.put("ExeInTime", exeInTime.intValue());
            result.put("ExeOverdue", exeOverdue.intValue());
            result.put("FinishInTime", finisInTime.intValue());
            result.put("FinishOverdue", finishOverdue.intValue());
            result.put("ExePlanToday", exePlanToday.intValue());
            Integer count = exeInTime.intValue() + exeOverdue.intValue() + finisInTime.intValue() + finishOverdue.intValue();
            return new Tuple(count, result);
        } catch(Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return new Tuple(0, result);
    }
    
    /**
     * Поиск задач по штатной единице с учётом состояний задач и прав доступа пользователя
     * @param staff
     * @param states
     * @param currentUser
     * @return 
     */
    public List<BaseDict> findTaskByStaffStates(Staff staff, List<State> states, User currentUser){
        return findTaskByStaffStates(staff, states)
                .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                .collect(Collectors.toList());
    }
    
    public Stream<Task> findTaskByStaffStates(Staff staff, List<State> states){
        em.getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> root = cq.from(Task.class); 
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get(Task_.owner), staff));
        if (!states.isEmpty()){
            criteries.add(root.get("state").get("currentState").in(states));
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(root).where(builder.and(predicates));
        TypedQuery<Task> q = em.createQuery(cq);
        return q.getResultStream();
    }               
    
    public Stream<Task> findTaskByStates(List<State> states){
        em.getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Task> cq = builder.createQuery(Task.class);
        Root<Task> root = cq.from(Task.class); 
        List<Predicate> criteries = new ArrayList<>();
        if (!states.isEmpty()){
            criteries.add(root.get("state").get("currentState").in(states));
        }
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(root).where(builder.and(predicates));
        TypedQuery<Task> query = em.createQuery(cq);
        return query.getResultStream();
    }
    
    /**
     * Поиск задач в заданных состояниях для department, доступные currentUser
     * @param department
     * @param states
     * @param currentUser
     * @param searcheInSubGroups
     * @return 
     */
    public List<BaseDict> findTaskByDepartStates(Department department, List<State> states, User currentUser, boolean searcheInSubGroups){        
        return findTaskByStates(states)
                .filter(task -> checkTaskInDepartment(task, department, task.getOwner().getOwner(), searcheInSubGroups, currentUser))
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск задач в заданных состояниях для company , доступные currentUser
     * @param company
     * @param states
     * @param currentUser
     * @param searcheInSubGroups
     * @return 
     */
    public List<BaseDict> findTaskByCompanyStates(Company company, List<State> states, User currentUser, boolean searcheInSubGroups){
        return findTaskByStates(states)
                .filter(task -> checkTaskInCompany(task, company, currentUser, searcheInSubGroups))
                .collect(Collectors.toList());
    }
    
    /**
     * Выполняет проверку сначала на принадлежность task к владельцу exampleOwner->(Company or Department) 
     * или какому-нибудь дочернему от exampleOwner (при условии что searcheInSubGroups = true)
     * и если принадлежность существует, то выполняется проверка на наличие у пользователя прав доступа к staff
     * @param task проверяемый объект
     * @param exampleOwner искомый владелец (критерий поиска), может быть как компания так и подразделением
     * @param checkedOwner владелец, для которого выполняется проверка 
     * @param user пользователь, для которого проверяются права к checkedItem
     * @param searcheInSubGroups
     * @return true - если объект доступен, false - если не доступен
     */
    @Override
    protected boolean checkPreloadItem(BaseDict exampleOwner, BaseDict checkedOwner, Task task, User user, boolean searcheInSubGroups){        
        if (exampleOwner instanceof Company){
            return checkTaskInCompany(task, (Company) exampleOwner, user, searcheInSubGroups);
        } else {
            return checkTaskInDepartment(task, (Department) exampleOwner, task.getOwner().getOwner(), searcheInSubGroups, user);                    
        }
    } 
    
    /**
     * Проверка принадлежности task указанному exampleDepartment и если да, то выполняется праверка прав доступа для user
     * @param task
     * @param exampleDepartment искомое подразделение
     * @param department проверяемое подразделение 
     * @return 
     */
    private boolean checkTaskInDepartment(Task task, Department exampleDepartment, Department department, boolean searcheInSubGroups, User user){
        if (department == null) return false;            
        if (department.equals(exampleDepartment)){
            return preloadCheckRightView(task, user);
        } else {
            //если нужно искать с учётом вложенных подразделений, то проверку выполняем снизу вверх
            return searcheInSubGroups ? checkTaskInDepartment(task, exampleDepartment,  department.getParent(), searcheInSubGroups, user) : false;
        }
    }
    
    /**
     * Проверяет, относится task к указанной company и если да, то выполняется праверка прав доступа для user
     * @param task
     * @param company
     * @param user
     * @return 
     */
    private boolean checkTaskInCompany(Task task, Company company, User user, boolean searcheInSubGroups){        
        Staff staff = task.getOwner();        
        if (staff == null) return false;
        
        Company companyStaff;
        if (!searcheInSubGroups){   //если не искать в подразделениях
            companyStaff = staff.getCompany();
        } else {
            companyStaff = staffFacade.findCompanyForStaff(staff);
        }
        
        if (Objects.equals(companyStaff, company)){
            return preloadCheckRightView(task, user);
        } else {
            return false;
        }           
    }
    
    /**
     * Формирование даты планового срока исполнения. Учитывается рабочее время
     * @param task      
     */
    public void makeDatePlan(Task task){
        Integer deltasec = task.getDeltaDeadLine();
        Date startDate = task.getBeginDate();
        Date planExecDate = workTimeService.calcWorkDayByStaff(startDate, deltasec, task.getOwner());
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
        task.setResultIcon(result.getIconName());
        task.setFactExecDate(new Date());        
        task.getState().setCurrentState(stateFacade.getCompletedState());
        messagesFacade.makeAsReadByTask(task, new Date());
        addLogEvent(task, DictLogEvents.TASK_FINISHED, 
                new String[]{task.getOwner().getEmployeeFIO(), task.getName(), ItemUtils.getBandleLabel(task.getResult(), userFacade.getUserLocale(user))}
                , user);
    }
    
    /**
     * Вычисляет число задач указанного пользователя, находящихся у него на исполнении
     * @param staff
     * @return 
     */
    public Long getCountExecTasksByUser(Staff staff){
        em.getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
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
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    @Override
    public void actualizeRoles(Task task){
        super.actualizeRoles(task);               
        if (task.getOwner() != null){
            setRoleExecutor(task, task.getOwner().getEmployee());
        }
    }
    
    /* *** ПРОЧЕЕ *** */  
    
    public Long findCountStaffLinks(Staff staff){
        em.getEntityManagerFactory().getCache().evict(Task.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Task> root = cq.from(Task.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Task_.owner), staff));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
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