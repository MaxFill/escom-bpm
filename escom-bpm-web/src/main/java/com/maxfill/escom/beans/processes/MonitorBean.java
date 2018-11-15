package com.maxfill.escom.beans.processes;

import com.maxfill.model.basedict.process.Process;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictRoles;
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
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
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
    private Integer selProcId;
    
    /* Атрибуты фильтра */
    private Date dateStart;
    private Date dateEnd;
    private Staff initiator;
    private Staff curator;
    private String number;
    private String name;
    private final TreeNode procTypesTree = new DefaultTreeNode("", null);
    private TreeNode selProcTypeNode = null;
    private List<State> states = new ArrayList<>();
    private List<String> results = new ArrayList<>();
    private List<String> allResults;
    private boolean filterCollapsed = false;
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private ProcessTypesFacade procTypesFacade;
    
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
        if (selProcId != null){
            filterCollapsed = true;
            onRefreshData();
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
        if (params.containsKey("procId")){
            selProcId = Integer.valueOf(params.get("procId"));
        }
        states.add(stateFacade.getRunningState());
        procTypesFacade.findRootItems(getCurrentUser())
                .forEach(procType->{
                    TreeNode childNode = new DefaultTreeNode(procType, procTypesTree);
                    childNode.setExpanded(true);
                    childNode.setSelected(true);
                    loadProcTypesTree(procType, childNode); 
                });        
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
    
    /**
     * Установка текущего объекта из контекстного меню
     */
    public void onMenuItemOpen(){
        currentItem = (BaseDict)selectedNode.getData();
    }
    
    private int loadTree(){
        List<Process> processes = processFacade.findItemsByFilters("regNumber", null, makeFilters(new HashMap()), getCurrentUser());
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
        process.getChildItems().stream()
                .filter(subProc->!subProc.isDeleted())
                .forEach(subProc->{
                    TreeNode subProcNode = new DefaultTreeNode(subProc, processNode);
                    makeTree(subProc, subProcNode);
        });
        if (process.getScheme() != null){
            process.getScheme().getTasks().forEach(task->new DefaultTreeNode(task, processNode));
        }
    }
    
    /**
     * Формирование фильтров для запроса
     * @param filters
     * @return 
     */
    protected Map<String, Object> makeFilters(Map filters) { 
        if (selProcId != null){
           filters.put("id", selProcId); 
           selProcId = null;
        } else {           
            filters.put("actual", true);
            filters.put("deleted", false);        
            //filters.put("parent", null);
            if (selProcTypeNode != null){
                filters.put("owner", selProcTypeNode.getData());
            }
            if(dateStart != null || dateEnd != null) {
                Map <String, Date> dateFilters = new HashMap <>();
                dateFilters.put("startDate", dateStart);        //дата начала периода отбора
                dateFilters.put("endDate", dateEnd);            //дата конца периода отбора
                filters.put("planExecDate", dateFilters);       //поле по которому отбираем
            } 
            if (initiator != null){
                filters.put("author", initiator.getEmployee());
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
            if (StringUtils.isNotBlank(name)){
                filters.put("name", name);
            }
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
                if (processFacade.isHaveRole(process, DictRoles.ROLE_CURATOR)){
                    sb.append(getLabelFromBundle("Curator")).append(": ");
                    if (process.getCurator() != null && process.getCurator().getEmployee() != null){
                        sb.append(process.getCurator().getEmployee().getShortFIO());
                    }
                } else {
                    sb.append(getLabelFromBundle("Initiator")).append(": ");
                    sb.append(process.getAuthor().getShortFIO());
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
        List<Staff> staffs = (List<Staff>) event.getObject();
        if (staffs.isEmpty()) return;
        initiator = staffs.get(0);
    }
    public void onChangeInitiator(ValueChangeEvent event){
        initiator = (Staff) event.getNewValue();
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
    
    private void loadProcTypesTree(ProcessType proccessType, TreeNode treeNode){
       procTypesFacade.findActualChilds(proccessType, getCurrentUser())
               .forEach(procType-> {
                    TreeNode childNode = new DefaultTreeNode(procType, treeNode);
                    loadProcTypesTree(procType, childNode);
               });
    }        
    
    /* *** GETS & SETS *** */

    public MonitorBean getEditedItem(){
        return this;
    }
    public List<State> getStates() {
        return states;
    }
    public void setStates(List<State> states) {
        this.states = states;
    }

    public void setInitiator(Staff initiator) {
        this.initiator = initiator;
    }
    public Staff getInitiator() {
        return initiator;
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

    public boolean isFilterCollapsed() {
        return filterCollapsed;
    }

    public void setFilterCollapsed(boolean filterCollapsed) {
        this.filterCollapsed = filterCollapsed;
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_MONITOR;
    }

    public TreeNode getProcTypesTree() {
        return procTypesTree;
    }

    public TreeNode getSelProcTypeNode() {
        return selProcTypeNode;
    }
    public void setSelProcTypeNode(TreeNode selProcTypeNode) {
        this.selProcTypeNode = selProcTypeNode;
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
     
}