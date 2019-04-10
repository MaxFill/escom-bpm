package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.folder.FolderNavigation;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.core.states.State;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.primefaces.model.TreeNode;

import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortOrder;

/* Расширение контролёра формы "Обозреватель задач" */
@Named
@ViewScoped
public class TaskExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 4688543715184790711L;

    @Inject
    private StaffBean staffBean;
    @Inject
    private TaskFacade taskFacade;
    
    @Override
    protected void initBean() {
        typeRoot = "Company";
    }

    //подгрузка веток в дерево 
    @Override
    protected void loadSubTree(){                
        if (isItemTreeType(currentItem)){ //если выделено подразделение
            if ("ui-icon-folder-collapsed".equals(currentItem.getIconTree())){
                treeBean.loadChilds(currentItem, treeSelectedNode);
                loadStaffByDepNode((Department)currentItem, treeSelectedNode);
                PrimeFaces.current().ajax().update("westFRM:accord:tree");
            }                                    
        } else
            if (isItemRootType(currentItem)){ //если выделена компания
                if ("ui-icon-folder-collapsed".equals(currentItem.getIconTree())){
                    rootBean.loadChilds(currentItem, treeSelectedNode);
                    loadStaffByCompanyNode((Company) currentItem, treeSelectedNode);
                    PrimeFaces.current().ajax().update("westFRM:accord:tree");
                }
            }
    }
    
    @Override
    protected List<BaseDict> loadDetailItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters){
        BaseDict treeItem = (BaseDict) treeSelectedNode.getData(); 
        List<State> states = getModel().getStateSearche();
        boolean searcheInSubGroups = getModel().isSearcheInGroups();
        if (treeItem instanceof Staff){
            return taskFacade.findTaskByStaffStates((Staff) treeItem, states, getCurrentUser());
        } else 
            if (treeItem instanceof Department){                
                return taskFacade.findTaskByDepartStates((Department) treeItem, states, getCurrentUser(), searcheInSubGroups);
            } else {
                return taskFacade.findTaskByCompanyStates((Company) treeItem, states, getCurrentUser(), searcheInSubGroups);   
            }        
    }
    
    /**
     * Загрузка штатных единиц в ветку компании
     * @param company
     * @param node 
     */
    private void loadStaffByCompanyNode(Company company, TreeNode node){
        staffBean.findStaffsByCompany(company)
                .forEach(staff->addItemInTree(node, staff, DictObjectName.STAFF));
    }
    
    /**
     * Загрузка штатных единиц в ветку подразделения
     * @param department
     * @param node 
     */
    private void loadStaffByDepNode(Department department, TreeNode node){
        staffBean.findDetailItems(department, 0, 0, "", "")
                .forEach(staff->addItemInTree(node, staff, DictObjectName.STAFF));
    }        
    
    /**
     * Определяет, может ли быть скопирован объект
     * @return
     */
    @Override
    public boolean isCanCopyTreeItem(){
        return false;
    }

    /**
     * Определяет доступность кнопки "Удалить" в дереве
     * @return
     */
    @Override
    public boolean isCanDeleteTreeItem(){
        return false;
    }

    @Override
    public TreeNode addNewItemInTree(BaseDict item, TreeNode parentNode){
        if (isItemRootType(item)){
            return super.addNewItemInTree(item, tree);
        } else {
            return super.addNewItemInTree(item, parentNode);
        }
    }
    
    @Override
    public void makeNavigator(BaseDict item) {
        navigator = new LinkedList();        
        navigator.addFirst(new FolderNavigation(item));
        if (item instanceof Staff){            
            if (item.getOwner() == null){
                navigator.addFirst(new FolderNavigation(((Staff) item).getCompany()));
                return;
            }
            item = item.getOwner();  
            navigator.addFirst(new FolderNavigation(item));
        } 
        while (item.getParent() != null) {
            item = (BaseDict) item.getParent();
            navigator.addFirst(new FolderNavigation(item));
        }
        if (!isItemRootType(item)){
            navigator.addFirst(new FolderNavigation(item.getOwner()));
        } 
    }
}