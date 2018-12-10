package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictTaskStatus;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.basedict.assistant.AssistantFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.utils.DateUtils;
import java.util.ArrayList;
import java.util.Comparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

/**
 * Контролер формы "Мои задачи"
 * @author maksim
 */
@Named
@ViewScoped
public class TaskListBean extends LazyLoadBean<Task>{
    private static final long serialVersionUID = -6035396289063993668L;

    @EJB
    private TaskFacade taskFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private AssistantFacade assistantFacade;
    @EJB
    private UserFacade userFacade;
    
    @Inject
    private TaskBean taskBean;
    @Inject
    private ProcessBean processBean;
    
    private boolean showOnlyExecute = true;
    private List<State> states = new ArrayList<>();
    private List<Staff> executors;
    private Staff executor;
    private String number;
    private String name;
    private Integer tasksStatus; //0 ExeInTime, 1 ExeOverdue, 2 FinishInTime, 3 FinishOverdue
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params) {
        states.add(stateFacade.getRunningState());
        executor = getCurrentStaff();
        executors = initExecutors();
        if (params.containsKey("tasksStatus")){
            tasksStatus = Integer.valueOf(params.get("tasksStatus"));
            period = sessionBean.getTaskPeriod();
            dateStart = sessionBean.getTaskDateStart();
            dateEnd = sessionBean.getTaskDateEnd();
        }
    }
    
    /**
     * Формирование списка доступных для выбора Исполнителей
     * @param task 
     */
    private List<Staff> initExecutors(){                
        //админ может выбрать любого
        if (userFacade.isAdmin(getCurrentUser())){
           return staffFacade.findActualStaff();            
        }        
        
        Set<Staff> staffs = new HashSet<>();
        if (executor != null){
            staffs.add(executor);
        }
        staffs.addAll(assistantFacade.findAssistByUser(getCurrentUser()));
        List<Staff> chiefs = assistantFacade.findChiefsByUser(getCurrentUser(), getCurrentUser()).stream()
                .map(user->user.getStaff())
                .collect(Collectors.toList());
        staffs.addAll(chiefs);
        List<Staff> result = new ArrayList<>(staffs);
        return result.stream()                
                .sorted(Comparator.comparing(Staff::getName, nullsFirst(naturalOrder())))
                .collect(Collectors.toList());                        
    }
    
    /* *** ЗАДАЧИ *** */
    
    public void onCreateTask(String beanId){
        Date dateBegin = new Date();
        Date datePlan = DateUtils.addDays(new Date(), 1);
        Task task = taskFacade.createTask("", getCurrentStaff(), getCurrentUser(), datePlan);
        task.setBeginDate(dateBegin);
        onOpenTask(task);
    }
    
    public void onOpenTask() {
        onOpenTask(selected);
    }
    public void onOpenTask(Task task) {
        selected = task;
        taskBean.prepEditItem(selected, getParamsMap());
    }
    
    /**
     * Обработка события после закрытия карточки задания
     * @param event
     */
    public void onTaskDlgClose(SelectEvent event){
        if (event.getObject() == null) return;        
        String action = (String) event.getObject();        
        switch (action){
            case "delete":{
                if (selected.getId() != null){
                    taskFacade.remove(selected);
                    refresh();
                }
                break;
            }
            case SysParams.EXIT_EXECUTE:{
                refresh();
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{
                refresh();
                break;
            }
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
        }        
    }
    
    /* *** ПРОЦЕСС *** */
    
    public void onOpenProcess(Task task){
        if (task.getScheme() == null) return;
        Process process = task.getScheme().getProcess();
        processBean.prepEditItem(process, getParamsMap());
    }
    
    public String getProcessName(Task task){
        if (task.getScheme() == null) return "";
        Process process = task.getScheme().getProcess();
        return process.getFullRegNumber()+" " + process.getNameEndElipse();
    }
    
    /* *** ФИЛЬТРЫ *** */
    
    public void onRefresh(){
        refresh();
    }
    
    public void onChangeChBoxType(){
        refresh();
    }
        
    public void refresh(){
        refreshLazyData();
        PrimeFaces.current().ajax().update("mainFRM:tasksTBL");
    }
    
    @Override
    protected Map<String, Object> makeFilters(Map filters) {
        filters.put("owner", executor);         
        if (!showOnlyExecute){     
            if(dateStart != null || dateEnd != null) {
                Map <String, Date> dateFilters = new HashMap <>();
                dateFilters.put("startDate", dateStart);
                dateFilters.put("endDate", dateEnd);
                filters.put("planExecDate", dateFilters);
            }
        }
        if (StringUtils.isNotBlank(number)){
            filters.put("id", number);
        }
        if (StringUtils.isNotBlank(name)){
            filters.put("name", name);
        }
        if (tasksStatus != null){
            Map <String, Date> periodFilters = new HashMap <>();
            periodFilters.put("startDate", dateStart);
            periodFilters.put("endDate", dateEnd);
            filters.put("dateCreate", periodFilters);
            filters.put("tasksStatus", tasksStatus);
            states.clear();
            switch (tasksStatus){
                case DictTaskStatus.EXE_IN_TIME:{ 
                    states.add(stateFacade.getRunningState());
                    break;
                }
                case DictTaskStatus.EXE_IN_OVERDUE:{
                    states.add(stateFacade.getRunningState());
                    break;
                }
                case DictTaskStatus.FINISH_IN_TIME:{
                    states.add(stateFacade.getCompletedState());
                    break;
                }
                case DictTaskStatus.FINISH_IN_OVERDUE:{
                    states.add(stateFacade.getCompletedState());
                    break;
                }
                case DictTaskStatus.EXE_PLAN_TODAY:{
                    states.add(stateFacade.getRunningState());
                    break; 
                }
            }
        }
        filters.put("states", states);
        return filters;
    }
    
    /* *** СЛУЖЕБНЫЕ *** */
    
    @Override
    protected BaseLazyFacade getLazyFacade() {
        return taskFacade;
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MY_TASKS;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("MyTasks");
    }
    
    /* GETS & SETS */

    public boolean isShowOnlyExecute() {
        return showOnlyExecute;
    }
    public void setShowOnlyExecute(boolean showOnlyExecute) {
        this.showOnlyExecute = showOnlyExecute;
    }

    public List<State> getStates() {
        return states;
    }
    public void setStates(List<State> states) {
        this.states = states;
    }

    public List<Staff> getExecutors() {
        return executors;
    }
    public void setExecutors(List<Staff> executors) {
        this.executors = executors;
    }
    
    public Staff getExecutor() {
        return executor;
    }
    public void setExecutor(Staff executor) {
        this.executor = executor;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
        
}
