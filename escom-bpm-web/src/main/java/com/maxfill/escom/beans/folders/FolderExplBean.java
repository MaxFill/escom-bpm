package com.maxfill.escom.beans.folders;

import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.beans.docs.DocsSearche;
import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.filter.Filter;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.utils.ItemUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.SortOrder;
import org.primefaces.model.TreeNode;

/* Расширение контролёра обозревателя архива */
@Named
@ViewScoped
public class FolderExplBean extends ExplorerTreeBean{
    private static final long serialVersionUID = 1718197265045722509L;
    private  static final String PROC_ITEMS_NAME  = "westFRM:accord:procTree:";
    private static final Integer LEH_TREE_PROCESS = PROC_ITEMS_NAME.length();
    
    @Inject
    private ProcessTypesBean processTypesBean;
    @Inject
    private ProcessBean processBean;
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    
    private TreeNode procTree;
    
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
                setFilterId(null);
            }
        }            
        super.onAfterFormLoad();
    }         
     
    /**
     * Находит папку документа и отображает документ в ней
     * @param doc 
     */
    public void onShowDocInFolder(Doc doc){
        if (doc == null) return;
        checkedItems.add(doc);
        if (!DictExplForm.TAB_TREE.equals(currentTab)){
            PrimeFaces.current().executeScript("PF('accordion').select(0);");            
        }
        Folder folder = (Folder) doc.getOwner();
        makeSelectedFolder(folder);                                
    }
    
    /* ДЕРЕВО: обработка события установки текущего элемента в дереве */
    public void onProcNodeSelect(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        onSelectInProc(node);
    }
    
    @Override
    protected void onSelectInProc(TreeNode node){
        if (node == null) return;
        
        if (procSelectedNode != null) {
            procSelectedNode.setSelected(false);
        }
        procSelectedNode = node;
        currentTab = DictExplForm.TAB_PROC;
        procSelectedNode.setSelected(true);
        currentItem = (ProcessType) procSelectedNode.getData();
        
        setSource(DictDetailSource.PROCESS_SOURCE);
        refreshLazyData();
        makeNavigator(currentItem);
        setCurrentViewModeAsDetail();
               
        if ("ui-icon-folder-collapsed".equals(currentItem.getIconTree())){
            processTypesBean.loadChilds(currentItem, procSelectedNode);
            PrimeFaces.current().ajax().update("westFRM:accord:procTree");
        }        
        
        BaseDict rootItem = (BaseDict) procTree.getChildren().get(0).getData();
        String journalName = "";
        if (!rootItem.equals(currentItem)){
            journalName = currentItem.getName();
        }        
        makeJurnalHeader(rootItem.getName(), journalName, "DisplaysDocsForRunningProcesses");
    }        
    
    /**
     * Загрузка в таблицу обозревателя документов, относящихся к процессам
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return 
     */
    @Override
    protected List<BaseDict> loadDocs(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters){
        return processFacade.findActualDetailItems((ProcessType)currentItem, 0, 0, sortField, sortField, getCurrentUser())
            .stream()
            .filter(process-> 
                    Objects.equals(DictStates.STATE_RUNNING, process.getState().getCurrentState().getId())
                    && process.getDocument() != null 
                    && docFacade.preloadCheckRightView(process.getDocument(), getCurrentUser()))
            .map(process->process.getDocument())
            .collect(Collectors.toList());                
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
                tbKey = tbKey - currentPage;
                BaseDict dragItem = (BaseDict) ItemUtils.findItemInDetailByKeyRow(tbKey, loadItems);
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
     * Создание документа из области загрузки файла в обозревателе документов
     */
    public void onCreateDoc(){
        Set<String> errors = new HashSet<>();
        if (treeSelectedNode == null || treeSelectedNode.getData() == null){
            errors.add(MessageFormat.format(MsgUtils.getMessageLabel("FolderIsNotSelected"), new Object[]{}));
            MsgUtils.showErrorsMsg(errors);
            return;
        }          
        Attaches attache = sessionBean.getAttaches().get(0); 
        getCreateParams().put("attache", attache);
        getCreateParams().put("name", attache.getName());
        onCreateDetailItem();
    }
    
    /**
     * Создание процесса посредством перетаскивания документа в вид процесса
     */
    public void createProcess(){
        List<Doc> docs = new ArrayList<>();
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
        processBean.createItemAndOpenCard(null, dropItem, params, getParamsMap());
    }
    
    @Override
    public String makeUploadFileCaption(){
        if (treeSelectedNode == null) return "";
        String folderName = ((Folder)treeSelectedNode.getData()).getName();
        StringBuilder sb = new StringBuilder(getLabelFromBundle("SelectFileAndCreateDoc"));
        sb.append(" ").append(getLabelFromBundle("InFolder")).append(" ").append(folderName);
        return sb.toString();
    }
    
    public boolean isFullSearche(){
        return StringUtils.isNotBlank(conf.getFullSearcheConnect());
    }
    
    /* ПОИСК */
    
    /* Обработка события изменения поля DateCreate */
    public void onChangeDateDocument(ValueChangeEvent event){ 
        getModel().changeDateDocument((String) event.getNewValue());          
    }
    
    /* GETS & SETS */

    @Override
    public DocsSearche getModel() {
        return (DocsSearche)super.getModel(); 
    }    
    
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