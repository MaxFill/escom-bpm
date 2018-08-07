package com.maxfill.escom.beans.folders;

import com.google.common.base.Objects;
import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.process.Process;
import com.maxfill.utils.ItemUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.TreeNode;

/* Расширение контролёра обозревателя архива */
@Named
@ViewScoped
public class FolderExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 1718197265045722509L;
    private  static final String PROC_ITEMS_NAME  = "mainFRM:accord:procTree:";
    private static final Integer LEH_TREE_PROCESS = PROC_ITEMS_NAME.length();
    
    @Inject
    private ProcessTypesBean processTypesBean;
    @Inject
    private ProcessBean processBean;
    
    @EJB
    private DocFacade docFacade;
    
    private TreeNode procTree;
    private TreeNode procSelectedNode;             
    
    /* Расширение для поиска в дереве папок по индексу дела */
    @Override
    protected boolean extTreeSearche(String searche, BaseDict item){
        Folder folder = (Folder) item;
        if (StringUtils.isBlank(folder.getFolderNumber())) return false;
        String folderNumber = folder.getFolderFullNumber();
        return folderNumber.equalsIgnoreCase(searche);
    }

    @Override
    public void onAfterFormLoad(){
        if (getFilterId() != null) {
            Filter filter = filtersFacade.find(getFilterId());
            if (filter != null) {
                makeSelectedFilter(filter);
            }
        }
    }
     
    /* Обработка события переключения между панелью фильтров и панелью дерева в аккордионе  */
    @Override
    public void onTreeTabChange(TabChangeEvent event) {
        Tab tab = event.getTab();
        String tabId = tab.getId();
        switch (tabId) {
            case "tabTree": {
                currentTab = DictExplForm.TAB_TREE;
                onSelectInTree(treeSelectedNode);
                break;
            }
            case "tabFilter": {
                currentTab = DictExplForm.TAB_FILTER;
                doFilterTreeNodeSelect(filterSelectedNode);
                break;
            }
            case "tabProc":{
               currentTab = DictExplForm.TAB_PROC;
               onSelectInProc(procSelectedNode);
               break;
            }
        }
    }
    
    /* ДЕРЕВО: обработка события установки текущего элемента в дереве */
    public void onProcNodeSelect(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        onSelectInProc(node);
    }
    
    private void onSelectInProc(TreeNode node){
        if (node == null) return;
        
        if (procSelectedNode != null) {
            procSelectedNode.setSelected(false);
        }
        procSelectedNode = node;
        currentTab = DictExplForm.TAB_PROC;
        procSelectedNode.setSelected(true);
        currentItem = (ProcessType) procSelectedNode.getData();
                
        List<Process> processes = ((List<Process>) currentItem.getDetailItems()).stream()
                     .filter(p-> Objects.equal(DictStates.STATE_RUNNING, p.getState().getCurrentState()))
                     .collect(Collectors.toList()); 

        Set<Doc> docsSet = new HashSet<>();
        processes.forEach(p->p.getDocs().stream()
                .filter(doc->docFacade.preloadCheckRightView(doc, getCurrentUser()))                
                .forEach(doc->docsSet.add(doc))
        );        
        setDetails(new ArrayList<>(docsSet), DictDetailSource.PROCESS_SOURCE); 
        
        makeNavigator(currentItem);
        setCurrentViewModeDetail();
       
        BaseDict rootItem = (BaseDict) procTree.getChildren().get(0).getData();
        String journalName = "";
        if (!rootItem.equals(currentItem)){
            journalName = currentItem.getName();
        }        
        makeJurnalHeader(rootItem.getName(), journalName, "DisplaysDocsForRunningProcesses");
    }
    
    /* Формирует список объектов для таблицы обозревателя  */
    @Override
    public List<BaseDict> getDetailItems(){
        if (detailItems == null) {
            switch (getSource()){
                case DictDetailSource.FILTER_SOURCE:{
                    doFilterTreeNodeSelect(filterSelectedNode);
                    break;
                }
                case DictDetailSource.TREE_SOURCE:{
                    onSelectInTree(treeSelectedNode);
                    break;
                }
                case DictDetailSource.SEARCHE_SOURCE:{
                    onSearcheItem();
                    break;
                }
                case DictDetailSource.PROCESS_SOURCE:{
                    onSelectInProc(procSelectedNode);
                    break;
                }
                default:{
                    detailItems = new ArrayList<>();
                    break;
                }
            }
        }
        return detailItems;
    }
    
   /* Обработка события drop в дерево объектов  */
    @Override
    public void dropToTree(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String dragId = params.get("dragId"); //определяем приёмник запись в дереве
        String dropId = params.get("dropId"); //получаем приёмник TreeNode куда поместили объект

        switch (currentTab){ //в зависимости от того, какое открыто дерево
            case DictExplForm.TAB_TREE:{
                String rkNode = dropId.substring(LEH_TREE_ITEMS, dropId.length());
                dropNode = EscomBeanUtils.findUiTreeNode(tree, rkNode);
                break;
            }
            case DictExplForm.TAB_FILTER:{
                String rkNode = dropId.substring(LEH_TREE_FILTERS, dropId.length());
                dropNode = EscomBeanUtils.findUiTreeNode(filterTree, rkNode);
                break;
            }
            case DictExplForm.TAB_PROC:{
                String rkNode = dropId.substring(LEH_TREE_PROCESS, dropId.length());
                dropNode = EscomBeanUtils.findUiTreeNode(procTree, rkNode);
                break;
            }
        }

        if (dropNode != null) {
            dropItem = (BaseDict) dropNode.getData();   //определили получателя

            //определям источник : объект тянется из дерева
            if (dragId.substring(0, LEH_TREE_ITEMS).equals(TREE_ITEMS_NAME)) {
                String rkNode = dragId.substring(LEH_TREE_ITEMS, dragId.length());
                dragNode = EscomBeanUtils.findUiTreeNode(getTree(), rkNode);
                BaseDict dragItem = (BaseDict) dragNode.getData();
                checkedItems.clear();
                makeCheckedItemList(dragItem);
                if (!checkedItems.isEmpty()){
                    Set<String> errors = new HashSet<>();
                    //проверяем, что тянем, так как может быть тянем root, а это пока не допустимо!
                    if (isItemTreeType(dragItem)){
                        // если тянем treeItem и бросаем в treeItem
                        if (treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                            onShowMovedDlg("MoveTreeDlg");
                        }
                    } else {
                        String error = MessageFormat.format(MsgUtils.getMessageLabel("MoveItemNotAvailable"), new Object[]{dragItem.getName(), dropItem.getName()});
                        errors.add(error);
                    }
                    if (!errors.isEmpty()) {
                        MsgUtils.showErrors(errors);
                    }
                }
                return;
            }

            //определям источник : объект тянется из таблицы обозревателя
            if (dragId.substring(0, LEH_TABLE_NAME).equals(TABLE_NAME)) {
                String rkTbl = dragId.substring(LEH_TABLE_NAME, dragId.length());
                String rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
                Integer tbKey = Integer.parseInt(rwKey);
                BaseDict dragItem = (BaseDict) ItemUtils.findItemInDetailByKeyRow(tbKey, getDetailItems());
                makeCheckedItemList(dragItem);
                if (!checkedItems.isEmpty()){
                    switch (currentTab){ //в зависимости от того, какое открыто дерево
                        case DictExplForm.TAB_TREE:{
                            doDropToTree(checkedItems);
                            break;
                        }
                        case DictExplForm.TAB_PROC:{
                            doDropToProcTree(checkedItems);
                            break;
                        }
                        case DictExplForm.TAB_FILTER:{
                            doDropToFilter(checkedItems, (Filter) dropItem);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /* Обработка drop события помещения объекта в дерево */
    private void doDropToProcTree(List<BaseDict> dragItems){
        if (dragItems.isEmpty() || dropItem == null) return;
        onShowMovedDlg("CreateProcessDlg");        
    }
    
    /**
     * Создание процесса посредством перетаскивания документа в вид процесса
     */
    public void createProcess(){
        Set<Doc> docs = new HashSet<>();
        checkedItems.stream().forEach(dragItem -> {
            if (isItemDetailType(dragItem)){
                docs.add((Doc)dragItem);                
            } else
                if (isItemTreeType(dragItem)){
                    ((Folder)dragItem).getDetailItems().forEach(doc->docs.add(doc));
                }            
        });
        Map<String, Object> params = new HashMap<>();
        params.put("documents", docs);
        processBean.createItemAndOpenCard(null, dropItem, params);
    }
    
    /* GETS & SETS */
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("DocsExplorer");
    }
    
    /* Формирование дерева процессов */
    public TreeNode getProcTree() {
        if (procTree == null){            
            procTree = processTypesBean.makeTree();            
        }
        return procTree;
    }    

    public TreeNode getProcSelectedNode() {
        return procSelectedNode;
    }
    public void setProcSelectedNode(TreeNode procSelectedNode) {
        this.procSelectedNode = procSelectedNode;
    }
        
}