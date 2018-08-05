package com.maxfill.escom.beans.processes;

import com.maxfill.model.process.Process;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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
    
    private TreeNode root;
    private TreeNode selectedNode;
    private BaseDict currentItem;
    
    /* Атрибуты фильтра */
    private Date dateStart;
    private Date dateEnd;
    private User initiator;
    private List<State> states = new ArrayList<>();
            
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private StateFacade stateFacade;
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private TaskBean taskBean;
    
    /**
     * Обновление данных журнала процессов
     */
    public void onRefreshData(){
        root = null;
    }
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params) { 
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
    
    private void loadTree(){        
        List<Process> processes = processFacade.findItemsByFilters("", "", makeFilters(new HashMap()));
        processes.forEach(proc->{
            TreeNode processNode = new DefaultTreeNode(proc, root);
            proc.getScheme().getTasks().forEach(task->{
                    TreeNode taskNode = new DefaultTreeNode(task, processNode);
                    if (Objects.equals(selectedNode, taskNode)){
                        taskNode.setSelected(true);
                        processNode.setExpanded(true);
                    }
                });
            if (Objects.equals(selectedNode, processNode)){
                processNode.setSelected(true);
                processNode.setExpanded(selectedNode.isExpanded());               
            }
        });
    }
    
    /**
     * Формирование фильтров для запроса
     * @param filters
     * @return 
     */
    protected Map<String, Object> makeFilters(Map filters) {      
        if(dateStart != null || dateEnd != null) {
            Map <String, Date> dateFilters = new HashMap <>();
            dateFilters.put("startDate", dateStart);        //дата начала периода отбора
            dateFilters.put("endDate", dateEnd);            //дата конца периода отбора
            filters.put("planExecDate", dateFilters);       //поле по которому отбираем
        } 
        if (initiator != null){
            filters.put("author", initiator);
        }
        if (!states.isEmpty()){
            filters.put("states", states);
        }
        return filters;
    }
         
    /**
     * Формирует данные результата для записи в таблице монитора
     * @param item
     * @return 
     */
    public String onGetItemResult(BaseDict item){
        if (item instanceof Task){
            Task task = (Task) item;
            return getLabelFromBundle(task.getResult());
        } else {
            return ""; //TODO что можно вывести для исполняеющего процесса? 
        }
    }
    
    /**
     * Формирует заголовок для записи в таблице монитора
     * @param item
     * @return 
     */
    public String onGetItemTitle(BaseDict item){
        if (item instanceof Task){
            Staff staff = (Staff)item.getOwner();
            return staff.getEmployee().getNameEndElipse();
        } else {
            return item.getAuthor().getNameEndElipse();
        }        
    }
    
    /**
     * Обработка события изменения инициатора в фильтре на форме
     * @param event 
     */
    public void onChangeInitiator(SelectEvent event){
        List<User> users = (List<User>) event.getObject();
        if (users.isEmpty()) return;
        initiator = users.get(0);
    }
    public void onChangeInitiator(ValueChangeEvent event){
        initiator = (User) event.getNewValue();
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
        if (root == null){
            root = new DefaultTreeNode(null, null);
            loadTree();
        }
        return root;
    }   

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MONITOR;
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
    
}
