package com.maxfill.escom.beans.processes;

import com.maxfill.model.basedict.process.Process;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.Results;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Контролер формы "Монитор исполнения процессов"
 * @author maksim
 */
@Named
@ViewScoped
public class MonitorBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = 3695442325119569465L;
    
    private TreeNode root = new DefaultTreeNode(null, null);
    private TreeNode selectedNode;
    private BaseDict currentItem;
    
    /* Атрибуты фильтра */
    private Date dateStart;
    private Date dateEnd;
    private User initiator;
    private Staff curator;
    private String number;
    
    private List<State> states = new ArrayList<>();
    private List<String> results = new ArrayList<>();
    private List<String> allResults;
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private StateFacade stateFacade;
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private TaskBean taskBean;    
    
    private Integer taskId;
    
    @Override
    public void onAfterFormLoad(){ 
        if (taskId != null){
            currentItem = taskBean.findItem(taskId);
            onOpenItem();
            taskId = null;
        }
    }
        
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ProcessExecutionControl");
    }
    
    /**
     * Обновление данных журнала процессов
     */
    public void onRefreshData(){
        root = new DefaultTreeNode(null, null);
        if (loadTree() == 0){
            MsgUtils.warnMsg("NO_SEARCHE_FIND");
        }
        PrimeFaces.current().ajax().update("mainFRM:monitorTable");
    }
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params) { 
         if (params.containsKey("itemId")){
            taskId = Integer.valueOf(params.get("itemId"));
        }
        states.add(stateFacade.getRunningState());
    }    
    
    /**
     * Обработка события открытия карточки процесса или задачи
     */
    public void onOpenItem(){
        if (currentItem == null) return;
        if (currentItem instanceof Process){
            processBean.prepEditItem((Process)currentItem, getParamsMap());
        } else {
            taskBean.prepEditItem((Task)currentItem, getParamsMap());
        }
    }
    
    public void onMenuItemOpen(){
        currentItem = (BaseDict)selectedNode.getData();
    }
    
    private int loadTree(){
        List<Process> processes = processFacade.findItemsByFilters("", "", makeFilters(new HashMap()), getCurrentUser());
        processes.forEach(proc->{            
            TreeNode processNode = new DefaultTreeNode(proc, root);
            makeTree(proc, processNode);            
        });
        expandTree(selectedNode);                
        return processes.size();
    }
    
    private void expandTree(TreeNode node){
        if (node == null) return;
        node.setExpanded(true);
        expandTree(node.getParent());
        node.setSelected(true);
    }
    
    private void makeTree(Process process, TreeNode processNode){
        process.getScheme().getTasks().forEach(task->new DefaultTreeNode(task, processNode));
        process.getChildItems().forEach(subProc->{
            TreeNode subProcNode = new DefaultTreeNode(subProc, processNode);
            makeTree(subProc, subProcNode);
        });
    }
    
    /**
     * Формирование фильтров для запроса
     * @param filters
     * @return 
     */
    protected Map<String, Object> makeFilters(Map filters) {        
        filters.put("actual", true);
        filters.put("deleted", false);
        filters.put("parent", null);
        if(dateStart != null || dateEnd != null) {
            Map <String, Date> dateFilters = new HashMap <>();
            dateFilters.put("startDate", dateStart);        //дата начала периода отбора
            dateFilters.put("endDate", dateEnd);            //дата конца периода отбора
            filters.put("planExecDate", dateFilters);       //поле по которому отбираем
        } 
        if (initiator != null){
            filters.put("author", initiator);
        }
        if (curator != null){
            filters.put("curator", curator);
        }
        if (!states.isEmpty()){
            filters.put("states", states);
        }
        if (!results.isEmpty()){            
            filters.put("procResults", results);
        }
        if (StringUtils.isNotBlank(number)){
            filters.put("regNumber", number);
        }
        return filters;
    }         
    
    /**
     * Формирует заголовок для записи в таблице монитора
     * @param item
     * @return 
     */
    public String onGetItemTitle(BaseDict item){
        StringBuilder sb = new StringBuilder();
        if (item instanceof Task){
            Task task = (Task)item;
            if (task.getRoleInProc() != null){
                sb.append(getLabelFromBundle(task.getRoleInProc().getRoleFieldName())).append(": ");
            }
            if (task.getOwner() != null && task.getOwner().getEmployee() != null){
                sb.append(task.getOwner().getEmployee().getShortFIO());
            } else {
                sb.append("<").append(getLabelFromBundle("NotAssigned")).append(">");
            }        
        } else {
            if (item instanceof Process){
                Process process = (Process) item;
                sb.append(getLabelFromBundle("Curator")).append(": ");
                if (process.getCurator() != null && process.getCurator().getEmployee() != null){
                    sb.append(process.getCurator().getEmployee().getShortFIO());
                }                
            }
        }
        return sb.toString();
    }        
        
    public String onGetItemResult(Results item){
        if (StringUtils.isBlank(item.getResult())) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(getLabelFromBundle("Done"));
        sb.append(": ");
        sb.append(getLabelFromBundle(item.getResult()));
        return sb.toString();
    }
    
    /**
     * Обработка события изменения инициатора в фильтре на форме
     * @param event 
     */
    public void onChangeInitiator(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<User> users = (List<User>) event.getObject();
        if (users.isEmpty()) return;
        initiator = users.get(0);
    }
    public void onChangeInitiator(ValueChangeEvent event){
        initiator = (User) event.getNewValue();
    }        
    
    /**
     * Обработка события изменения Куратора в фильтре на форме
     * @param event 
     */
    public void onChangeCurator(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> staff = (List<Staff>) event.getObject();
        if (staff.isEmpty()) return;
        curator = staff.get(0);
    }
    public void onChangeCurator(ValueChangeEvent event){
        curator = (Staff) event.getNewValue();
    }
    
    public void onAfterItemCloseForm(SelectEvent event){
        String result = (String) event.getObject();
        if (!SysParams.EXIT_NOTHING_TODO.equals(result) ){
            onRefreshData();
        }
    }
            
    /* *** GETS & SETS *** */

    public List<State> getStates() {
        return states;
    }
    public void setStates(List<State> states) {
        this.states = states;
    }
    
    public User getInitiator() {
        return initiator;
    }
    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public Staff getCurator() {
        return curator;
    }
    public void setCurator(Staff curator) {
        this.curator = curator;
    }
        
    public TreeNode getSelectedNode() {
        return selectedNode;
    }
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public BaseDict getCurrentItem() {
        return currentItem;
    }
    public void setCurrentItem(BaseDict currentItem) {
        this.currentItem = currentItem;
        selectedNode = EscomBeanUtils.findTreeNode(root, currentItem);
    }

    public TreeNode getRoot() {
        return root;
    }   

    public List<String> getAllResults() {
        if (allResults == null){
            allResults = processFacade.findProcessResults();
        }
        return allResults;
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_MONITOR;
    }

    public List<String> getResults() {
        return results;
    }
    public void setResults(List<String> results) {
        this.results = results;
    }
        
    public Date getDateStart() {
        return dateStart;
    }
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
     
}