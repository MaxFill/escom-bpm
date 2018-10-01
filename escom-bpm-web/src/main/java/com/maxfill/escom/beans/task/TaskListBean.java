package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
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
    
    @Inject
    private TaskBean taskBean;
    @Inject
    private ProcessBean processBean;
    
    private boolean showOnlyExecute = true;
    private List<State> states = new ArrayList<>();
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params) {
        states.add(stateFacade.getRunningState());
    }
    
    /* *** ЗАДАЧИ *** */
    
    public void onCreateTask(String beanId){
        Date dateBegin = new Date();
        Date datePlan = DateUtils.addDays(new Date(), 1);
        Task task = taskFacade.createTask("", getCurrentStaff(), getCurrentUser(), datePlan);
        task.setBeginDate(dateBegin);
        onOpenTask(task);
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
        return task.getScheme().getProcess().getNameEndElipse();
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
        filters.put("owner", getCurrentStaff());
        filters.put("states", states); 
        if (!showOnlyExecute){     
            if(dateStart != null || dateEnd != null) {
                Map <String, Date> dateFilters = new HashMap <>();
                dateFilters.put("startDate", dateStart);
                dateFilters.put("endDate", dateEnd);
                filters.put("planExecDate", dateFilters);
            }
        }
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
        
}
