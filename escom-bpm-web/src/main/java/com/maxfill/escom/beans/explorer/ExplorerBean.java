package com.maxfill.escom.beans.explorer;

import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.filter.Filter;
import com.maxfill.model.basedict.filter.FiltersFacade;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.folder.FolderNavigation;
import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.services.searche.SearcheService;
import java.io.IOException;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.*;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SortOrder;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.UploadedFile;

/* Контролер формы обозревателя */
@Named
@ViewScoped
public class ExplorerBean extends LazyLoadBean<BaseDict>{
    private static final long serialVersionUID = 5230153127233924868L;   

    @Inject
    protected DocBean docBean;
    @Inject
    private AttacheBean attacheBean; 
    
    @EJB
    protected FiltersFacade filtersFacade;
    
    @EJB
    private SearcheService searcheService;    
    
    protected BaseTreeBean rootBean;
    protected BaseTreeBean treeBean;
    protected BaseTableBean tableBean;
    protected BaseTableBean searcheBean;
    
    protected BaseDict currentItem;    
    private BaseDict editItem; 
    private Set<BaseDict> copiedItems;

    protected TreeNode tree;
    protected TreeNode filterTree;

    protected TreeNode filterSelectedNode;
    protected TreeNode treeSelectedNode;
    protected TreeNode procSelectedNode; 
        
    protected TreeNode dragNode, dropNode;
        
    private Deque navigator; 

    private final String typeMixed = "mixed";        
    private String currentType = typeMixed;

    protected String typeDetail;
    protected String typeTree;
    protected String typeRoot;

    private Integer typeEdit; //режим редактирования записи
    
    protected List<BaseDict> loadItems = new ArrayList<>();     //список объектов, полученных запросом, для отображения в таблице обозревателя
    
    private final Map<String, Object> createParams = new HashMap<>();
    protected BaseDict dropItem;
    
    private SearcheModel model;
    private String treeSearcheKey; 
    
    /* *** ПОЛЯ ОБОЗРЕВАТЕЛЯ *** */    
    private Boolean layoutState = true;
    private String jurnalHeader;
    private String selectorHeader;
    private String explorerHeader;
    protected String currentTab = "0";
    
    //private List<SortMeta> sortOrder;
    
    private Integer rowsInPage = DictExplForm.ROW_IN_PAGE;
    protected Integer currentPage = 0;
    protected String versionOS;   

    /* *** СЛУЖЕБНЫЕ ПОЛЯ *** */
    private Integer source = DictDetailSource.ALL_ITEMS_SOURCE;
    protected Integer viewMode;         //режим отображения формы
    private Integer selectMode;         //режим выбора для селектора
    
    //параметры открытия обозревателя
    private Integer selectedDocId;      //при открытии обозревателя в это поле заносится id документа для открытия
    private Integer filterId = null;    //при открытии обозревателя в это поле заносится id фильтра что бы его показать 
    private Integer folderId = null;    //при открытии обозревателя в это поле заносится id фильтра что бы его показать
    
    protected Integer itemId;
               
    private SortOrder defSortOrder = SortOrder.ASCENDING;
    private String defSortField = "name";    
      
    /* Cобытие при открытии формы обозревателя/селектора  */
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (viewMode == null){            
            if (params.containsKey("selectMode")){
                selectMode = Integer.valueOf(params.get("selectMode"));
                viewMode = DictExplForm.SELECTOR_MODE;
            } else {
                viewMode = DictExplForm.EXPLORER_MODE;
                if (params.containsKey("filterId")) {
                    filterId = Integer.valueOf(params.get("filterId"));
                }
                if (params.containsKey("folderId")) {
                    folderId = Integer.valueOf(params.get("folderId"));
                }
            }
            if (params.containsKey("itemId")){
                itemId = Integer.valueOf(params.get("itemId"));
            }
        }
    }

    @Override
    public void onAfterFormLoad() {
        if (itemId != null){
            currentItem = tableBean.findItem(itemId);            
            if (currentItem != null){
                onEditDetailItem(currentItem);
                itemId = null;
            }
        }
        super.onAfterFormLoad();
    }
    
    /* КАРТОЧКИ ОБЪЕКТОВ */
    
    /* КАРТОЧКИ: открытие карточки объекта для просмотра */
    public void onViewDetailItem(){
        BaseDict item = getCurrentItem();        
        setTypeEdit(DictEditMode.VIEW_MODE);
        if (isItemDetailType(item)){
            editItem = tableBean.prepViewItem(item, tableBean.getParamsMap(), new HashSet<>());       
        } else
            if (isItemTreeType(item)){
                editItem = treeBean.prepViewItem(item, tableBean.getParamsMap(), new HashSet<>());       
            } else
                if (isItemRootType(item)){
                    editItem = rootBean.prepViewItem(item, tableBean.getParamsMap(), new HashSet<>());       
                }
    }

    /* КАРТОЧКИ: открытие карточки объекта из дерева для просмотра */
    public void onViewTreeItem(){
        onViewTreeItem(getCurrentItem());
    }
    public void onViewTreeItem(BaseDict item){
        currentItem = item;
        setTypeEdit(DictEditMode.VIEW_MODE);
        if (isItemRootType(item)){
            editItem = rootBean.prepViewItem(item, rootBean.getParamsMap(), new HashSet<>());
        } else {
            editItem = treeBean.prepViewItem(item, treeBean.getParamsMap(), new HashSet<>());
        }      
    }
    
    /* КАРТОЧКИ: открытие карточки объекта для редактирование */
    public void onEditDetailItem(BaseDict item){
        currentItem = item;
        setTypeEdit(DictEditMode.EDIT_MODE);
        if (isItemTreeType(item)){
            editItem = treeBean.prepEditItem(item, treeBean.getParamsMap());
        } else 
            if (isItemDetailType(item)) {
                editItem = tableBean.prepEditItem(item, tableBean.getParamsMap());        
            } else 
                if (isItemRootType(item)) {
                    editItem = rootBean.prepEditItem(item, tableBean.getParamsMap());        
                }
    }

    /* КАРТОЧКИ: открытие карточки объекта из дерева для редактирования */
    public void onEditTreeItem(){
        BaseDict item = getCurrentItem();
        setTypeEdit(DictEditMode.EDIT_MODE);        
        if (isItemRootType(item)){
            editItem = rootBean.prepEditItem(item, rootBean.getParamsMap());
        } else {
            editItem = treeBean.prepEditItem(item, treeBean.getParamsMap());
        }
    }
    
    /* КАРТОЧКИ: создание объекта в дереве с открытием карточки */
    public void onCreateTreeItem() {        
        if (treeSelectedNode == null){
            MsgUtils.errorMsg("ParentNoSet");
            return;
        }
        BaseDict selected = (BaseDict) treeSelectedNode.getData();
        BaseDict owner = null;
        BaseDict parent = null;
        if (isItemTreeType(selected)){ 
            parent = selected;            
        } else   
            if (isItemRootType(selected)){                
                owner = selected;
            }
        typeEdit = DictEditMode.INSERT_MODE;        
        editItem = treeBean.createItemAndOpenCard(parent, owner, createParams, treeBean.getParamsMap());
    }
    
    /* КАРТОЧКИ: создание объекта в дереве на нулевом уровне */
    public void onCreateRootItem() {
        BaseDict owner = null;
        BaseDict parent = null;
        typeEdit = DictEditMode.INSERT_MODE;
        editItem = rootBean.createItemAndOpenCard(parent, owner, createParams, rootBean.getParamsMap());
    }
    
    /* КАРТОЧКИ: создание объекта в таблице с открытием его карточки  */
    public void onCreateDetailItem(){
        if (Objects.equals(tableBean, treeBean)){
            onCreateTreeItem();
            return;
        }
        typeEdit = DictEditMode.INSERT_MODE;
        BaseDict parent = null;
        BaseDict owner = null;
        if (treeSelectedNode != null){
            owner = (BaseDict) treeSelectedNode.getData();
        }        
        editItem = tableBean.createItemAndOpenCard(parent, owner, createParams, tableBean.getParamsMap());
    }
    
    public Boolean checkCanCreateDetailItem(Set<String> errors){
        typeEdit = DictEditMode.INSERT_MODE;
        BaseDict parent = null;
        BaseDict owner = null;
        if (treeSelectedNode != null){
            owner = (BaseDict) treeSelectedNode.getData();
        }        
        tableBean.checkCanCreateItem(parent, owner, errors, createParams);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return false;
        }
        return true;
    }
        
    /* КАРТОЧКИ: обработка после закрытия карточки объекта  */
    public void onUpdateAfterCloseForm(SelectEvent event){ 
        String exitResult = (String) event.getObject();
        if (!SysParams.EXIT_NOTHING_TODO.equals(exitResult)) {
            switch (typeEdit){
                case DictEditMode.EDIT_MODE: {
                    try {
                        editItem.setIconTree(currentItem.getIconTree());
                        BeanUtils.copyProperties(currentItem, editItem);
                        //onSetCurrentItem(editItem);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    if (isItemRootType(editItem) || isItemTreeType(editItem)){ 
                        treeSelectedNode.getChildren().clear();
                        BaseDict item = (BaseDict) treeSelectedNode.getData();
                        item.setIconTree("ui-icon-folder-collapsed");
                        onSelectInTree(treeSelectedNode);
                    }
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    TreeNode newNode;
                    if (isItemDetailType(editItem)) {
                        //detailItems.add(editItem);
                        loadItems.add(editItem);
                        //onSetCurrentItem(editItem);
                        break;
                    }
                    if (isItemRootType(editItem)){
                        newNode = addNewItemInTree(editItem, tree);
                    } else {                        
                        newNode = addNewItemInTree(editItem, treeSelectedNode);
                    }                    
                    onSelectInTree(newNode);
                    break;
                }
            }             
        }        
        createParams.clear();        
    }
    
    /* TODO Нашёл что вызов данного метода есть в обозревателе документов (doc-explorer) Возможно что просто устаревший...*/
    public void onUpdateAfterChangeItem(SelectEvent event){
        editItem = (BaseDict) getItemBean(currentItem).getLazyFacade().find(currentItem.getId());
        try {                    
            BeanUtils.copyProperties(currentItem, editItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } 
    }    
    
    public boolean isItemDetailType(BaseDict item){
        return Objects.equals(typeDetail, item.getClass().getSimpleName());
    }    
    public boolean isItemTreeType(BaseDict item){
        return Objects.equals(typeTree, item.getClass().getSimpleName());
    }
    public boolean isItemRootType(BaseDict item){
        return Objects.equals(typeRoot, item.getClass().getSimpleName());
    }    
    
    /**
     * Возвращает бин объекта. Бин задаётся в форме обозревателя
     * @param item
     * @return 
     */
    protected BaseTableBean getItemBean(BaseDict item){
        if (isItemDetailType(item)){
            return tableBean;
        }
        if (isItemTreeType(item)){
            return treeBean;
        }
        if (isItemRootType(item)){
            return rootBean;
        }
        throw new RuntimeException("ESCOM_ERROR: bean not found for item: [" + item.toString() + "]");
    }
    
    /* ИЗБРАННОЕ */
    
    /* ИЗБРАННОЕ: удаление из избранных отмеченных записей. Вызов с панели инструментов формы обозревателя  */    
    public void onDelCheckedFromFavorites() {
        getCheckedItems().stream().forEach(item -> {
            if (isItemDetailType(item)){
                tableBean.delFromFavorites(item);
            } else
                if (isItemTreeType(item)){
                    treeBean.delFromFavorites(item);
                }
        });
        loadItems.removeAll(getCheckedItems());        
    }
    
    /* ИЗБРАННОЕ: удаление из избранного записи контента. */  
    public void onDelContentFromFavorites(BaseDict item){
        if (isItemDetailType(item)){
            tableBean.delFromFavorites(item);
        } else
            if (isItemTreeType(item)){
                treeBean.delFromFavorites(item);
            }
        loadItems.remove(item);         
    }
    
    /* ИЗБРАННОЕ: добавление записи контента в избранное  */
    public void onAddContentInFavorites(){
        onAddContentInFavorites(currentItem);
    }    
    public void onAddContentInFavorites(BaseDict item){
        if (item == null){
            return;
        }
        if (isItemDetailType(item)){
            tableBean.addInFavorites(item);
        } else
            if (isItemTreeType(item)){
                treeBean.addInFavorites(item);
            } else
                if (isItemRootType(item)){
                    rootBean.addInFavorites(item);
                }
    } 
    
    /* ИЗБРАННОЕ: добавление отмеченных записей контента в избранное */
    public void onAddCheckedContentInFavorites() {
        getCheckedItems().stream().forEach(item -> {
            if (isItemDetailType(item)){
                tableBean.addInFavorites(item);
            } else
                if (isItemTreeType(item)){
                    treeBean.addInFavorites(item);
                }
        });
    }
    
    /* КОРЗИНА */
    
    /* КОРЗИНА: восстановление из корзины отмеченных записей */
    public void onRestoreCheckedContentTrash(){
        getCheckedItems().stream().forEach(item -> {
                if (isItemDetailType(item)){
                    tableBean.doRestoreItemFromTrash(item);
                } else
                    if (isItemTreeType(item)){
                        treeBean.doRestoreItemFromTrash(item);
                        restoreItemInTree(item);
                    } else
                        if (isItemRootType(item)){
                            rootBean.doRestoreItemFromTrash(item);
                            restoreItemInTree(item);
                        }
            });
        loadItems.removeAll(getCheckedItems());     
    } 
    
    /* КОРЗИНА: восстановление в дереве из корзины объекта  */
    public void restoreItemInTree(BaseDict item) {
        BaseDict parent = item.getParent();
        TreeNode parentNode;
        if (parent == null) {
            parentNode = getTree();
        } else {
            parentNode = EscomBeanUtils.findTreeNode(tree, parent);
        }
        if (parentNode != null) {
            addNewItemInTree(item, parentNode); //нужно
        }
    }
    
    /* КОРЗИНА: восстановление объекта из корзины - вызов с экранной формы  */
    public void onRestoreContentFromTrash(BaseDict item) {
        if (isItemDetailType(item)){
            tableBean.doRestoreItemFromTrash(item);
        } else
            if (isItemTreeType(item)){
                treeBean.doRestoreItemFromTrash(item);
                restoreItemInTree(item);
            } else {
                if (isItemRootType(item)){
                    rootBean.doRestoreItemFromTrash(item);
                    restoreItemInTree(item);
                }
            }
        refreshLazyData();
    }
    
    /* КОРЗИНА: помещение в корзину отмеченных записей контента  */
    public void onMoveCheckedContentToTrash(){
        Set<String> errors = new HashSet<>();
        getCheckedItems().stream()
                .forEach(item -> {
                    if (isItemDetailType(item)){
                        tableBean.moveToTrash(item, errors);
                    } else
                        if (isItemTreeType(item)) {
                            treeBean.moveToTrash(item, errors);
                            TreeNode node = EscomBeanUtils.findTreeNode(tree, item);
                            if (node != null && node.getParent() != null){
                                node.getParent().getChildren().remove(node);
                            }
                        } else
                            if (isItemRootType(item)){
                                rootBean.moveToTrash(item, errors);
                            }
                });
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        } else {            
            refreshLazyData();
            PrimeFaces.current().ajax().update("westFRM");
            PrimeFaces.current().ajax().update("mainFRM");
        }
    }
    
    /* КОРЗИНА: перемещение контента в корзину  */
    public void onMoveContentToTrash(BaseDict item){
        getCheckedItems().clear();
        getCheckedItems().add(item);
        onMoveCheckedContentToTrash();
    }
    
    private void moveContentToTrash(BaseDict item, Set<String> errors){
        if (item == null) return;
        
        if (isItemDetailType(item)){
            tableBean.moveToTrash(item, errors);            
        } else
            if (isItemTreeType(item)){
                treeBean.moveToTrash(item, errors); 
                /*
                if (errors.isEmpty()) {
                    TreeNode node = EscomBeanUtils.findTreeNode(tree, item); 
                    node.getParent().getChildren().remove(node); 
                }
                */
            } else
                if (isItemRootType(item)){
                    rootBean.moveToTrash(item, errors);
                }        
    }
    
    /* КОРЗИНА: перемещение записи из дерева в корзину  */
    public void onMoveTreeItemToTrash(){
        if (treeSelectedNode == null) return;        
        Set<String> errors = new HashSet<>();
        moveContentToTrash(currentItem, errors);
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        } else {
            removeNodeFromTree(treeSelectedNode);
            refreshLazyData();
            PrimeFaces.current().ajax().update("westFRM:accord:tree");
            PrimeFaces.current().ajax().update("mainFRM");
        }
    }
    
    /* КОРЗИНА: полная очистка корзины без проверки на наличие зависимых связей 
    Очистка удаляет все дочерние и детальные объекты! Команда доступна только администратору */
    public void onClearTrash() {        
        loadItems.stream().forEach((item -> {
            if (isItemDetailType(item)){
                tableBean.deleteItem(item);
            } else
                if (isItemTreeType(item)){
                    treeBean.deleteItem(item);
                } else 
                    if (isItemRootType(item)){
                        rootBean.deleteItem(item);
                    }            
        }));
        refreshLazyData();
    }
    
    /* КОРЗИНА: удаление из корзины выбранных записей контента */
    public void onClearCheckedContentTrash(){
        getCheckedItems().stream().forEach((item -> deleteContentFromTrash(item)));
        refreshLazyData();
    }

    /* КОРЗИНА: удаление из корзины объекта контента  */
    public void onDeleteContentFromTrash(BaseDict item) {
        deleteContentFromTrash(item);
        refreshLazyData();
    }

    public void deleteContentFromTrash(BaseDict item){
        if (isItemDetailType(item)){
            tableBean.deleteItem(item);
        } else
            if (isItemTreeType(item)){
                treeBean.deleteItem(item);
            } else {
                if (isItemRootType(item)){
                    rootBean.deleteItem(item);
                }
            }
    }
    
    /* ФИЛЬТРЫ */
    
    /* ФИЛЬТР: Формирует дерево фильтров */
    public TreeNode getFilterTree() {
        if (filterTree == null && tableBean != null) {
            filterTree = new DefaultTreeNode("Root", null);
            filterTree.setExpanded(true);
            
            //формируем корневой элемент для фильтров detail объекта            
            Metadates tableMD = tableBean.getMetadatesObj();            
            filtersFacade.findRootItems(getCurrentUser())            
                    .filter(treeItem -> !(DictFilters.ON_MY_EDIT == treeItem.getId() && !appBean.isUseModeshape()))
                    .forEach(treeItem -> {
                        treeItem.setIcon(tableMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeDetail, tableMD.getBundleJurnalName(), tableMD);
                    }
            );

            //формируем корневой элемент для фильтров tree объекта
            if (treeBean != null){            
                Metadates treeMD = treeBean.getMetadatesObj();                
                filtersFacade.findRootItems(getCurrentUser())
                    .filter(treeItem -> !(DictFilters.ON_MY_EDIT == treeItem.getId() && !appBean.isUseModeshape()))
                    .forEach(treeItem -> {
                        treeItem.setIcon(treeMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeTree, treeMD.getBundleJurnalName(), treeMD);
                    }
                );
            }
            //формируем корневой элемент для фильтров root объекта
            if (rootBean != null){                
                Metadates rootMD = rootBean.getMetadatesObj();                
                filtersFacade.findRootItems(getCurrentUser())
                    .filter(treeItem -> !(DictFilters.ON_MY_EDIT == treeItem.getId() && !appBean.isUseModeshape()))
                    .forEach(treeItem -> {
                        treeItem.setIcon(rootMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeRoot, rootMD.getBundleJurnalName(), rootMD);
                    }
                );
            }
        }
        return filterTree;
    }
    
    /* ФИЛЬТР: добавление узла в дерево фильтров при его формировании  */
    private TreeNode addFilterInTree(TreeNode parentNode, BaseDict item, String nodeType, String nodeName, Metadates metadate) {           
        TreeNode newNode;

        synchronized (this) {
            newNode = new DefaultTreeNode(nodeType, item, parentNode);
            newNode.setExpanded(true);
        }

        String bundleName = MsgUtils.getBandleLabel(nodeName);
        item.setName(bundleName);

        List<Filter> childs = filtersFacade.findChildsFilters((Filter)item, metadate);
        childs.stream()
                .forEach(itemChild -> addFilterInTree(newNode, itemChild, nodeType, itemChild.getName(), metadate)
        );        

        return newNode;
    }

    /* ФИЛЬТР: установка текущего элемента в ФИЛЬТРАХ по заданному объекту filter */
    public void makeSelectedFilter(BaseDict filter){
        TreeNode rootNode = getFilterTree();
        TreeNode node = EscomBeanUtils.findTreeNode(rootNode, filter);
        doFilterTreeNodeSelect(node);
        PrimeFaces.current().ajax().update("westFRM");
        PrimeFaces.current().ajax().update("mainFRM");
    }
    
    /* ФИЛЬТР: обработка события щелчка по фильтру на форме */
    public void onFilterTreeNodeSelect(NodeSelectEvent event) {
        filterSelectedNode = event.getTreeNode();
        doFilterTreeNodeSelect(filterSelectedNode);
    }
    
    /* ФИЛЬТР: обработка события выбора фильтра */
    protected void doFilterTreeNodeSelect(TreeNode node){
        if (node == null) return;

        currentTab = DictExplForm.TAB_FILTER;

        //если фильтр был ранее установлен, то выполняем сброс установки фильтра
        if (filterSelectedNode != null) {
            filterSelectedNode.setSelected(false);
        }

        filterSelectedNode = node;
        filterSelectedNode.setSelected(true);

        Filter filter = (Filter) filterSelectedNode.getData();        

        doMakeFilterJurnalHeader(filterSelectedNode, filter);        
        
        refreshLazyData();
        setSource(DictDetailSource.FILTER_SOURCE);
    }

    /* ФИЛЬТР: формирование заголовка журнала для разделов фильтров */
    private void doMakeFilterJurnalHeader(TreeNode node, Filter filter){
        TreeNode parentNode = node.getParent();
        String firstName = "";
        if (!parentNode.equals(filterTree)){
            Filter parentFilter = (Filter) parentNode.getData();
            if (parentFilter.getParent() == null){
                firstName = parentFilter.getName();
            }
        }
        
        makeJurnalHeader(firstName, filter.getName(), "DisplaysObjectsSelectedFilter");        
    }
    
    /* ФИЛЬТР: проверят, является ли фильтр "Корзина" текущим элементом в дереве */
    public boolean isTrashSelected(){
        TreeNode node = filterSelectedNode;
        if (node == null || !currentTab.equals(DictExplForm.TAB_FILTER)){
            return false;
        }
        Filter filter = (Filter) node.getData(); 
        return filter.getId().equals(DictFilters.TRASH_ID);         
    }

    /* ФИЛЬТР: проверят, отображается ли сейчас дерево  */
    public boolean isShowTree(){
        return Objects.equals(currentTab, DictExplForm.TAB_TREE);
    }
    
    /* ФИЛЬТР: проверят, является ли фильтр "Избранные" текущим элементом в дереве */
    public boolean isFavoriteSelected(){
        TreeNode node = filterSelectedNode;
        if (node == null || !currentTab.equals(DictExplForm.TAB_FILTER)){
            return false;
        }        
        Filter filter = (Filter) node.getData(); 
        return filter.getId().equals(DictFilters.FAVORITE_ID);         
    } 
    
    /* ФИЛЬТР: обработка события переключения между панелью фильтров и панелью дерева в аккордионе  */
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
    
    protected void onSelectInProc(TreeNode node){        
    }
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА */
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: установка текущего объекта по двойному клику в таблице обозревателя   */
    @Override
    public void onRowDblClckOpen(SelectEvent event){
        BaseDict item = (BaseDict) event.getObject();
        onSetCurrentItem(item);        
    }        

    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: возвращает список объектов для таблицы обозревателя  */       
    
    @Override
    public void refreshLazyData(){
        loadItems = null;
        super.refreshLazyData();
        checkedItems.clear();
    }
      
    /**
     * Загрузка данных в таблицу обозревателя
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return 
     */
    @Override
    public List<BaseDict> onLoadItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        this.filters = filters;
        if (loadItems == null) {
            switch (getSource()){
                case DictDetailSource.FILTER_SOURCE:{
                    Filter filter = (Filter) filterSelectedNode.getData();
                    if (filterSelectedNode.getType().equals(typeDetail)){
                        loadItems = tableBean.makeFilteredContent(filter, first, pageSize, sortField,  sortOrder.name());                    
                        setCurrentViewModeDetail();                        
                    } else
                        if (filterSelectedNode.getType().equals(typeTree)){
                            loadItems = treeBean.makeFilteredContent(filter, first, pageSize, sortField,  sortOrder.name());
                            setCurrentViewModeTree();
                        } else
                            if (filterSelectedNode.getType().equals(typeRoot)){
                                loadItems = rootBean.makeFilteredContent(filter, first, pageSize, sortField,  sortOrder.name());
                                setCurrentViewModeRoot();
                            }
                    break;
                }
                case DictDetailSource.TREE_SOURCE:{ 
                    BaseDict treeItem = (BaseDict) treeSelectedNode.getData(); 
                    if (isItemTreeType(treeItem)){                    
                            loadItems = treeBean.makeGroupContent(treeItem, tableBean, viewMode, first, pageSize, sortField,  sortOrder.name());                            
                            //count = treeBean.getDetailBean().getFacade().findCountActualDetails(currentItem).intValue();
                        } else
                            if (isItemRootType(treeItem)){                            
                                loadItems = rootBean.makeGroupContent(treeItem, tableBean, viewMode, first, pageSize, sortField,  sortOrder.name());
                                //count = rootBean.getDetailBean().getFacade().findCountActualDetails(currentItem).intValue();
                            }
                    break;
                }
                case DictDetailSource.SEARCHE_SOURCE:{
                    loadItems = doSearcheItems(first, pageSize, sortField, sortOrder, makeFilters(filters));
                    break;
                }
                case DictDetailSource.PROCESS_SOURCE:{
                    loadItems = loadDocs(first, pageSize, sortField, sortOrder, makeFilters(filters));
                    break;
                }
                default:{
                    loadItems = new ArrayList<>();
                    break;
                }
            }
        }        

        pageSize = first + pageSize;
        if (pageSize > loadItems.size()){
            pageSize = loadItems.size();
        }
        if (first > pageSize){                
            first = currentPage;
        }            

        if (!Objects.equals(defSortField, sortField) || !Objects.equals(defSortOrder, sortOrder)) {
            if (currentItem == null){
                loadItems = tableBean.sortDetails(loadItems, sortField, sortOrder);
            } else {
                if (isItemDetailType(currentItem)){
                    loadItems = tableBean.sortDetails(loadItems, sortField, sortOrder);
                } else
                    if (isItemTreeType(currentItem)){
                        loadItems = treeBean.sortDetails(loadItems, sortField, sortOrder);
                    } else {
                        if (isItemRootType(currentItem)){
                            loadItems = rootBean.sortDetails(loadItems, sortField, sortOrder);
                        }
                    }
            }
            defSortField = sortField;
            defSortOrder = sortOrder;
        }
        if (first > pageSize){                
            first = pageSize;
        }        

        return loadItems.subList(first, pageSize);
    }    
    
    protected List<BaseDict> loadDocs(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters){
        return new ArrayList<>();
    }
            
    @Override
    public int countItems(){
        if (loadItems == null) return 0;
        return loadItems.size();
    }    

    public boolean isEmptyDetails(){
        return CollectionUtils.isEmpty(loadItems);
    }

    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: раскрытие содержимого группы/папки (провалиться внутрь группы в обозревателе)  */ 
    public void onLoadGroupContent(BaseDict item) {
        onSetCurrentItem(item);
        if (isItemDetailType(item)){            
        } else
            if (isItemTreeType(item)){
                makeSelectedGroup(item);
            } else
                if (isItemRootType(item)){
                    makeSelectedGroup(item);
                }
    }
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: переход на уровень вверх в таблице */ 
    public void onGotoUpLevelContent() {
        if (treeSelectedNode == null) return;

        currentItem = (BaseDict) treeSelectedNode.getData();        
        if (currentItem.getParent() != null) {           
            makeSelectedGroup(currentItem.getParent());
        } else 
            if (currentItem.getOwner() != null){                
                makeSelectedGroup(currentItem.getOwner());
            }
    }
    
    /* ДЕРЕВО */
    
    /* ДЕРЕВО: обновление дерева объектов  */
    public void onReloadTreeItems() {
        tree = null;
        treeSelectedNode = null;        
        setSource(DictDetailSource.ALL_ITEMS_SOURCE);
        jurnalHeader = null;
        refreshLazyData();
        checkedItems.clear();
    }    

    /* ДЕРЕВО: сбросить в дереве установки развёртывания и выделения */  
    private void clearTree(TreeNode root){
        root.setExpanded(false);
        root.setSelected(false);
        root.getChildren().stream().forEach(child ->clearTree(child));
    }
    
    /* ДЕРЕВО: получение дерева  */
    public TreeNode getTree() {
        if (tree == null){
            if (rootBean != null){
                tree = rootBean.makeTree();
            } else 
                if (treeBean != null){
                    tree = treeBean.makeTree();
                }            
        }
        return tree;
    }    
    
    /* ДЕРЕВО: обработка события установки текущего элемента в дереве */
    public void onTreeNodeSelect(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        onSelectInTree(node);
    }    
    
    /* ДЕРЕВО: Установка текущего элемента в дереве по заданному узлу node */
    public void onSelectInTree(TreeNode node) {        
        if (node == null) return;
        
        if (treeSelectedNode != null) {
            treeSelectedNode.setSelected(false);
        }
        treeSelectedNode = node;
        currentTab = DictExplForm.TAB_TREE;
        treeSelectedNode.setSelected(true);
        currentItem = (BaseDict) treeSelectedNode.getData();        
        
        if (isItemTreeType(currentItem)){
            if ("ui-icon-folder-collapsed".equals(currentItem.getIconTree())){
                treeBean.loadChilds(currentItem, treeSelectedNode);
                PrimeFaces.current().ajax().update("westFRM:accord:tree");
            }                                    
        } else
            if (isItemRootType(currentItem)){
                if ("ui-icon-folder-collapsed".equals(currentItem.getIconTree())){
                    rootBean.loadChilds(currentItem, treeSelectedNode);
                    PrimeFaces.current().ajax().update("westFRM:accord:tree");
                }
            }
        
        setSource(DictDetailSource.TREE_SOURCE); 
        refreshLazyData();
        
        makeNavigator(currentItem);         
        setCurrentViewModeMixed();
       
        BaseDict rootItem = (BaseDict) tree.getChildren().get(0).getData();
        String journalName = "";
        if (!rootItem.equals(currentItem)){
            journalName = currentItem.getName();
        }        
        makeJurnalHeader(rootItem.getName(), journalName, "DisplaysObjectsRelatedTo");
    }        
        
    /* ДЕРЕВО: установка текущего элемента в ДЕРЕВЕ по заданному объекту item */
    public void makeSelectedGroup(BaseDict item){      
        if (item == null) return;

        //сброс предыдущей установки текущей node в дереве
        if (treeSelectedNode != null) {
            treeSelectedNode.setSelected(false);
        }
        
        TreeNode node = EscomBeanUtils.findTreeNode(tree, item);
         
        if (node != null){
            onSelectInTree(node);
        }
    }
    
    /* ДЕРЕВО: обработка события двойного клика в дереве  */
    public void onRowDblClckInTree(SelectEvent event) {
        treeSelectedNode = (TreeNode) event.getObject();
        if (treeSelectedNode == null){
            return;
        }
        currentItem = (BaseDict) treeSelectedNode.getData();
        makeSelectedGroup(currentItem);        
        onEditTreeItem();        
    }
    
    /* ДЕРЕВО: добавление нового объекта в дерево  */
    public TreeNode addNewItemInTree(BaseDict item, TreeNode parentNode){        
        if (item == null) return null;       
        if (parentNode == null){
            parentNode = tree;
        }
        parentNode.setExpanded(true);
        if (isItemRootType(item)){
            return rootBean.addItemInTree(parentNode, item, typeRoot);
        } else {
            return treeBean.addItemInTree(parentNode, item, "tree");
        }
    }
         
    /* ДЕРЕВО: удаление узла в дереве  */
    public void removeNodeFromTree(TreeNode node){
        if (node == null) return; 
        
        TreeNode parent = node.getParent();
        parent.getChildren().remove(node);        
        if (!parent.equals(tree)){ //если удалён элемент не верхнего уровня, то делаем выделение его родителя
            onSelectInTree(parent);
        } else { //если был удалён элемент верхнего уровня, то ничего в дереве не выделяем
            treeSelectedNode = null;
            currentItem = null;
        }
    }
    
    /* ДЕРЕВО: определяет, является ли текущее выделение в дереве root уровнем */
    public boolean isSelectRootItem(){
        if (treeSelectedNode == null){
            return true; //так надо
        }
        return Objects.equals(treeSelectedNode.getParent(), tree);
    }

    /* ДЕРЕВО: разворачивает всё дерево */
    public void onExpandTree(){
        expandDown(tree);
    }
    
    /* ДЕРЕВО: разворачивает текущую ветку от текущего элемента вглубь */
    public void onExpandNode(){
        expandDown(treeSelectedNode);
    }
    
    /* ДЕРЕВО: разворачивает ветку вглубь */
    private void expandDown(TreeNode node){
        if (node == null) return;
        node.setExpanded(true);
        if (!node.equals(tree)){
            BaseDict item = (BaseDict) node.getData();
            if (isItemTreeType(item)){
                treeBean.loadChilds(item, node);            
            } else
                if (isItemRootType(item)){
                    rootBean.loadChilds(item, node);                
                }
            }
        node.getChildren().stream().forEach(childNode -> expandDown(childNode));
    }
    
    /* ДЕРЕВО: Разворачивает ветку от текущего узла до корня */
    private void expandUp(TreeNode node){
        node.setExpanded(true);
        if (node.getParent() != null){
            expandUp(node.getParent());
        }
    }
    
    /* ДЕРЕВО: Cворачивает всё дерево  */
    public void onCollapseTree() {
        doCollapseNode(tree);
    }
    
    /* ДЕРЕВО: Cворачивает текущую ветку от текущего элемента вглубь  */
    public void onCollapseNode(){
        doCollapseNode(treeSelectedNode);
    };
   
    /* ДЕРЕВО: Cворачивает ветку  */
    private void doCollapseNode(TreeNode node){
        if (node == null) return;
        node.setExpanded(false);
        node.getChildren().stream().forEach(childNode -> doCollapseNode(childNode));
    }
    
    /* ДЕРЕВО: Обработка события развёртывания node  */
    public void onNodeExpand(NodeExpandEvent event){
        TreeNode node = event.getTreeNode();
        if (node == null)  return;
        node.setExpanded(true);
    }
    
    /* ДЕРЕВО: Обработка события свёртывания узла  */
    public void onNodeCollapse(NodeCollapseEvent event){
        TreeNode node = event.getTreeNode();
        if (node == null){
            return;
        }
        node.setExpanded(false);
    }        

    /* ДЕРЕВО: Выполняет поиск в дереве */
    private void doSearcheInTree(TreeNode rootNode, List<TreeNode> rezult){
        rootNode.getChildren().stream().map(node -> {
            BaseDict item = (BaseDict) node.getData();            
            if (StringUtils.containsIgnoreCase(item.getName(), treeSearcheKey) || extTreeSearche(treeSearcheKey, item)){
                node.setSelected(true);
                expandUp(node);
                rezult.add(node);
            }
            return node;
        }).forEach(node -> doSearcheInTree(node, rezult));
    }

    /* Дополнительный поиск в дереве */
    protected boolean extTreeSearche(String searche, BaseDict item){
        return false;
    }

    /* НАВИГАТОР */

    /* НАВИГАТОР: установка текущей папки по нажатию на кнопку навигатора  */ 
    public void navigationSetSelected(FolderNavigation nv) {
        BaseDict group = (BaseDict) nv.getFolder();
        makeSelectedGroup(group);
    }
    
    /* НАВИГАТОР: формирование навигационной цепочки  */ 
    public void makeNavigator(BaseDict item) {
        navigator = new LinkedList();        
        navigator.addFirst(new FolderNavigation(item));
        while (item.getParent() != null) {
            item = (BaseDict) item.getParent();
            navigator.addFirst(new FolderNavigation(item));
        }
        if (rootBean != null && !isItemRootType(item)){
            navigator.addFirst(new FolderNavigation(item.getOwner()));            
        }
    }
    
    /* КОПИРОВАНИЕ И ВСТАВКА */
    
    /* КОПИРОВАНИЕ: вызов копирования отмеченных объектов в таблице */
    public void onCopySelectedItem() {
        if (CollectionUtils.isEmpty(checkedItems)){
            MsgUtils.warnMsg("NoCheckedItems");
            return;
        }
        doCopyItems(checkedItems);
    }

    /* КОПИРОВАНИЕ: вызов копирования объекта из дерева */
    public void onCopySelectedTreeItem() {
        if (isCanCopyTreeItem()){            
            onCopyItem(currentItem);
        } else {
            MsgUtils.errorFormatMsg("ObjectNotCopied", new Object[]{currentItem.getName()});
        }
    }

    /* КОПИРОВАНИЕ: вызов копирования объекта из таблицы обозревателя */
    public void onCopyItem(BaseDict item) {
        if (item == null){return;}
        doCopyItems(Collections.singletonList(item));
    }

    /* КОПИРОВАНИЕ: копирование объектов в память  */
    public void doCopyItems(List<BaseDict> sourceItems) {
        copiedItems = sourceItems.stream()
                .map(copyItem->getItemBean(copyItem).doCopy(copyItem))
                .collect(Collectors.toSet());
        copiedItems.stream().forEach(item-> MsgUtils.succesFormatMsg("ObjectIsCopied", new Object[]{item.getName()}));
    }

    /**
     * Определяет возможность Копировать в дереве
     * @return
     */
    public boolean isCanPasteItem(){
        return CollectionUtils.isEmpty(copiedItems);
    }

    /**
     * Определяет доступность кнопки "Копировать" в дереве
     * @return
     */
    public boolean isCanCopyTreeItem(){
        return !isSelectRootItem();
    }

    /**
     * Определяет доступность кнопки "Удалить" в дереве
     * @return
     */
    public boolean isCanDeleteTreeItem(){
        boolean flag = isSelectRootItem();
        return flag;
    }
    
    /* ВСТАВКА: вставка объекта в дерево */
    public void onPasteItemToTree(){
        if (CollectionUtils.isEmpty(copiedItems)){
            MsgUtils.errorMsg("NoHaveObjectsToInsert");
            return;
        }
        
        Set<String> errors = new HashSet<>();
        List<BaseDict> rezults = pasteItem(currentItem, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
        }
        if (!rezults.isEmpty()){
            rezults.stream().filter(item-> isItemRootType(item) || isItemTreeType(item))
                .forEach(item -> addNewItemInTree(item, treeSelectedNode));
            refreshLazyData();
            MsgUtils.succesMsg("PasteCopiedObjectDone");
        }
    }

    /* ВСТАВКА: вставка объекта в таблицу обозревателя */
    public void onPasteItemToTable(){
        Set<String> errors = new HashSet<>();
        BaseDict parent = null;
        if (treeSelectedNode != null){
            parent = (BaseDict)treeSelectedNode.getData();
        }
        List<BaseDict> rezults = pasteItem(parent, errors);
        if (!errors.isEmpty()){            
            MsgUtils.showErrors(errors);
            return;
        }
        if (!rezults.isEmpty()){
            MsgUtils.succesMsg("PasteCopiedObjectDone");
            //Вот тут должна быть так же и вставка в дерево или его обновление если скопированный в таблице объект является древовидным!
            refreshLazyData();
        }
    }
    
    /* ВСТАВКА: обработка списка объектов для их вставки. Parent в данном контексте обозначает, куда(во что) помещается объект.
    Реальный parent будет установлен в бине объекта */
    private List<BaseDict> pasteItem(BaseDict parent, Set<String> errors) {        
        List<BaseDict> rezults = new ArrayList<>();
        copiedItems.stream().forEach(item -> {
            BaseDict pasteItem = prepPasteItem(item, parent, errors);
            if (pasteItem != null){
                rezults.add(pasteItem);
            }
        });
        return rezults;
    }
    
    /* ВСТАВКА */
    public BaseDict prepPasteItem(BaseDict sourceItem, BaseDict recipient, Set<String> errors){
        BaseTableBean bean = getItemBean(sourceItem);
        BaseDict pasteItem = bean.doPasteItem(sourceItem, recipient, errors);

        if (!errors.isEmpty()) return null;

        //если объект копируется, то с ним нужно скопировать зависимости
        if (bean.isNeedCopyOnPaste(sourceItem, recipient)){
            List<List<?>> dependency = bean.doGetDependency(sourceItem);
            if (CollectionUtils.isNotEmpty(dependency)){
                copyPasteDependency(dependency, pasteItem, errors);
                pasteItem = bean.findItem(pasteItem.getId());
            }
        }
        return pasteItem;
        /*else //если объект не копируется, а только создаётся ссылка
            {
                bean.preparePasteItem(pasteItem, sourceItem, recipient);
                return sourceItem;
            }
            */
    }    
    
    /* ВСТАВКА копирование дочерних и подчинённых объектов */
    private void copyPasteDependency(List<List<?>> dependency, BaseDict pasteItem, Set<String> errors){
        for (List<?> depend : dependency){
            depend.stream().forEach(detailItem -> prepPasteItem((BaseDict)detailItem, pasteItem, errors));
        }        
    } 
    
    /* ПОИСК */

    /* Обработка нажатия на кнопку алфавитной панели   */
    public void onAbcSearche(ActionEvent event) {
        String action = (String) event.getComponent().getAttributes().get("action");
        getModel().setNameSearche(action);
        onSearcheItem();
    }
    
    /* Обработка действия по нажатию кнопки Поиск */
    public void onSearcheItem() {        
        if (getModel().isSearcheInGroups() && (treeBean == null || treeSelectedNode == null)) {
            MsgUtils.errorMsg("NO_SEARCHE_GROUPS");
            return ;
        } 
        setSource(DictDetailSource.SEARCHE_SOURCE);
        refreshLazyData();        
        
        switch (currentTab) {
            case DictExplForm.TAB_TREE: {
                if (treeSelectedNode != null) {
                    treeSelectedNode.setSelected(false);
                    treeSelectedNode = null;
                }
                break;
            }
            case DictExplForm.TAB_FILTER: {
                if (filterSelectedNode != null) {
                    filterSelectedNode.setSelected(false);
                    filterSelectedNode = null;
                }
                break;
            }
        }        
    }    
            
    /* Выполняет поиск объектов с учётом критериев поиска  */
    public List<BaseDict> doSearcheItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        List<BaseDict> searcheGroups = new ArrayList<>();
        Map<String, Object> paramEQ = new HashMap<>();
        Map<String, Object> paramLIKE = new HashMap<>();
        Map<String, Object> paramIN = new HashMap<>();
        Map<String, Date[]> paramDATE = new HashMap<>();       

        //добавление в запрос точных критериев
        if (model.isOnlyActualItem()) {
            paramEQ.put("actual", true);
        }
        if (model.getAuthorSearche() != null) {
            paramEQ.put("author", model.getAuthorSearche());
        }
                
        //добавление в запрос не точных критериев
        String name = model.getNameSearche().trim();
        if (name.equals("*")) {
            name = "%";
        } else {
            name = name + "%";
        }
                
        if (model.isFullTextSearche() && name.length() >= 3 &&
                Objects.equals("Doc", tableBean.getLazyFacade().getItemClass().getSimpleName())){ //если поиск выполняется в документах
            Set<Integer> docIds = searcheService.fullSearche(name);
            if (docIds.isEmpty()){
              docIds.add(0);
            } 
            paramIN.put("id", docIds);
        } else {
            if (name.length() > 1) {
                paramLIKE.put("name", name);
            }
        }
        
        //добавление в запрос критериев на вхождение
        //paramIN.put("state", states);

        //добавление в запрос даты создания
        if (model.isDateCreateSearche()) {
            Date[] dateArray = new Date[2];
            dateArray[0] = model.getDateCreateStart();
            dateArray[1] = model.getDateCreateEnd();
            paramDATE.put("dateCreate", dateArray);
        }

        //добавление в запрос даты изменения
        if (model.isDateChangeSearche()) {
            Date[] dateArray = new Date[2];
            dateArray[0] = model.getDateChangeStart();
            dateArray[1] = model.getDateChangeEnd();
            paramDATE.put("dateChange", dateArray);
        }
        
        Map<String, Object> addParams = new HashMap<>();
        model.addSearcheParams(paramEQ, paramLIKE, paramIN, paramDATE, searcheGroups, addParams);
        
        List<Integer> statesIds = model.getStateSearche().stream().map(item -> item.getId()).collect(Collectors.toList());

        List<BaseDict> result = searcheBean.doSearche(statesIds, paramEQ, paramLIKE, paramIN, paramDATE, addParams, first, pageSize);
        //count = searcheBean.getFacade().getCountByParameters(statesIds, paramEQ, paramLIKE, paramIN, paramDATE, addParams).intValue();             
        setSource(DictDetailSource.SEARCHE_SOURCE);
        setCurrentViewModeDetail();        
        makeJurnalHeader(MsgUtils.getBandleLabel(searcheBean.getMetadatesObj().getBundleJurnalName()), MsgUtils.getBandleLabel("SearcheResult"), "DisplaysObjectsSelectedSearche");
        navigator = null;
        if (result.isEmpty()) {
            MsgUtils.warnMsg("NO_SEARCHE_FIND");            
        }
        return result;
    }

    /* Выполняет поиск в дереве объектов */
    public void onSearcheInTree(){        
        if (StringUtils.isNotBlank(treeSearcheKey)){
            onExpandTree();
            clearTree(tree);
            List<TreeNode> rezult = new ArrayList<>();
            doSearcheInTree(tree, rezult);
            if (rezult.isEmpty()){
                MsgUtils.warnMsg("NO_SEARCHE_FIND");
                return;
            }
            if (rezult.size() == 1){
                onSelectInTree(rezult.get(0));
            }
        }
    }

    /* Обработка drop помещения объекта в фильтр */
    protected void doDropToFilter(List<BaseDict> dragItems, Filter filter){
        Set<String> errors = new HashSet<>();
        switch (filter.getId()){
            case DictFilters.TRASH_ID:{
                checkedItems =
                    dragItems.stream().filter(dragItem -> 
                        (isItemDetailType(dragItem) && tableBean.prepareDropItemToTrash(dragItem, errors))
                        ||
                        (isItemTreeType(dragItem) && treeBean.prepareDropItemToTrash(dragItem, errors))
                        || 
                        (isItemRootType(dragItem) && rootBean.prepareDropItemToTrash(dragItem, errors))                      
                    ).collect(Collectors.toList());
                onShowMovedDlg("MoveItemToTrashDlg");
                break;
            }
            case DictFilters.NOTACTUAL_ID:{
                checkedItems =
                    dragItems.stream().filter(dragItem -> 
                        (isItemDetailType(dragItem) && tableBean.prepareDropItemToNotActual(dragItem, errors))
                        ||
                        (isItemTreeType(dragItem) && treeBean.prepareDropItemToNotActual(dragItem, errors))
                        ||
                        (isItemRootType(dragItem) && rootBean.prepareDropItemToNotActual(dragItem, errors))
                    ).collect(Collectors.toList());
                onShowMovedDlg("MoveItemToNotActual");
                break;
            }
            case DictFilters.FAVORITE_ID:{
                dragItems.stream().forEach(dragItem -> onAddContentInFavorites(dragItem));
                break;
            }
            default:{
                MsgUtils.warnMsg("OperationIsNotApplicable");
                PrimeFaces.current().ajax().update("mainFRM:tblDetail");
            }
        }
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        }    
    }
         
    /* DRAG & DROP: перемещение объекта в корзину */
    public void dropItemToTrash(){
        Set<String> errors = new HashSet<>();
        checkedItems.stream().forEach(dragItem -> moveContentToTrash(dragItem, errors));
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
        } else {
            refreshLazyData();
            //обновление нужно потому что больше его выполнить некому!
            PrimeFaces.current().ajax().update("mainFRM:tblDetail");
        }
    }
    
    /* DRAG & DROP: перемещение объекта в не актулаьные */
    public void dropItemToNotActual(){
        checkedItems.stream().forEach(dragItem -> {
            if (isItemDetailType(dragItem)){
                tableBean.moveItemToNotActual(dragItem);
            } else 
                if (isItemTreeType(dragItem)){
                    treeBean.moveItemToNotActual(dragItem);
                } else 
                    if (isItemRootType(dragItem)){
                        rootBean.moveItemToNotActual(dragItem);
                    }
        });    
    }
            
    /* *** СЕЛЕКТОР *** */
    
    /* СЕЛЕКТОР: определяет режим множественного выбора в селекторе  */
    public boolean isMultySelectMode(){
        return Objects.equals(selectMode, DictExplForm.MULTY_SELECT_MODE);
    }
    
    /* СЕЛЕКТОР: определяет режим единичного выбора в селекторе  */
    public boolean isSinglSelectMode(){
        return Objects.equals(selectMode, DictExplForm.SING_SELECT_MODE);
    }    
    
    /* СЕЛЕКТОР: определяет, является ли объект доступным для выбора в селекторе  */
    public boolean isCanSelectedItem(BaseDict item){
        return Objects.equals(item.getClass().getSimpleName(), searcheBean.getLazyFacade().getItemClass().getSimpleName());
    }
        
    /* CЕЛЕКТОР: выбор объекта по двойному клику  */
    public String onRowDblClckSelector(SelectEvent event) {
        BaseDict item = (BaseDict) event.getObject();
        return onSelect(item);
    }
    
    /* СЕЛЕКТОР: обработка действия в селекторе по нажатию кнопки единичного выбора */
    public String onSelect(BaseDict item) {
        if (item == null){return "";}
        getCheckedItems().clear();
        getCheckedItems().add(item);        
        return onCloseCard(getCheckedItems());
    }
        
    /* СЕЛЕКТОР: закрытие селектора без выбора объектов  */
    public String onClose() {
        getCheckedItems().clear();        
        return onCloseCard(getCheckedItems());
    }
    
    /* СЕЛЕКТОР: действие по нажатию кнопки множественного выбора для списка  */
    public String onMultySelect() {        
        return onCloseCard(getCheckedItems());
    }        
        
    /* СЕЛЕКТОР: обработка действия в селекторе объектов при выборе элемента в дереве   */
    public String onSelectTreeItem() {        
        if (currentItem == null || !isCanSelectedItem(currentItem)){
            return "";
        }        
        List<BaseDict> groups = new ArrayList<>();
        groups.add(currentItem);
        return onCloseCard(groups);
    }
    
    /* ДОКУМЕНТЫ И ВЛОЖЕНИЯ */    
            
    public void addAttacheFromScan(SelectEvent event){
        if (currentItem == null) return;
        Doc doc = (Doc) currentItem;
        docBean.addAttacheFromScan(doc, event);
        docBean.getLazyFacade().edit(doc);
    }
        
    public boolean isItemHaveAttache(BaseDict item){
        if (!item.getClass().getSimpleName().equals(DictObjectName.DOC)){
            return false;
        }
        Doc doc = (Doc) item;
        return doc.getMainAttache() != null;
    }   
       
    /* Загрузка файла через контрол на форме обозревателя документов */
    public void onUploadFile(FileUploadEvent event) throws IOException{        
        if (checkCanCreateDetailItem(new HashSet<>())){
            UploadedFile uploadFile = EscomFileUtils.handleUploadFile(event); 
            Attaches attache = attacheBean.uploadAtache(uploadFile);
            createParams.put("attache", attache);
        }
    }            
    
    /* Подготовка вложений документов для отправки на e-mail  */
    public void prepareSendMailDocs(String mode){
        List<BaseDict> checked = prepareCheckedItems();
        if (!checked.isEmpty()){
            sessionBean.openMailMsgForm(mode, checked);
        } else {
            MsgUtils.warnMsg("NO_SELECT_DOCS");
        }
    } 
    
    /* Формирование списка документов из отмеченных папок и документов  */    
    private List<BaseDict> prepareCheckedItems(){
        List<BaseDict> sourceItems = getCheckedItems(); 
        List<BaseDict> targetItems = new ArrayList<>();
        for (BaseDict content : sourceItems){
            if (content.getClass().getSimpleName().equals("Folders")){
                Folder folder = (Folder) content;
                folder.getDetailItems().stream().forEach(doc -> targetItems.add(doc));
            } else {
                targetItems.add(content);
            }
        }
        return targetItems;
    }
    
    public void onEditDocument(){
        docBean.onOpenFormLockMainAttache((Doc) currentItem);
    }
    
    public void onViewDocument(BaseDict item){
        //onSetCurrentItem(item);
        docBean.onViewMainAttache((Doc) item);
    }
    
    /* ПЕЧАТЬ */
    
    /* ПЕЧАТЬ : Открытие на предпросмотр журнала документов */
    public void openDocJournalReport() {
        Map<String, Object> params = new HashMap<>();
        params.put("USER_LOGIN", getCurrentUser().getLogin());
        params.put("REPORT_TITLE", MsgUtils.getBandleLabel("DocJournal"));
        List<Doc> docs = new ArrayList<>();
        loadItems.stream().filter(item -> item instanceof Doc).forEach(item -> docs.add((Doc) item)); 

        Collator collator = Collator.getInstance(getLocale());
        
        Comparator<Doc> comparator = (Doc doc1, Doc doc2) -> {
            int rezult;
            String companyName1 = "";
            String companyName2 = "";
            if (doc1.getCompany() != null && StringUtils.isNotBlank(doc1.getCompany().getName())){
                companyName1 = doc1.getCompany().getName();
            }
            if (doc2.getCompany() != null && StringUtils.isNotBlank(doc2.getCompany().getName())){
                companyName2 = doc2.getCompany().getName();
            }
            rezult = collator.compare(companyName1, companyName2);
            if (rezult == 0){
                String docTypeName1 = "";
                String docTypeName2 = "";
                if (doc1.getDocType() != null && StringUtils.isNotBlank(doc1.getDocType().getName())){
                    docTypeName1 = doc1.getDocType().getName();
                }
                if (doc2.getDocType() != null && StringUtils.isNotBlank(doc2.getDocType().getName())){
                    docTypeName2 = doc2.getDocType().getName();
                }
                rezult = collator.compare(docTypeName1, docTypeName2);
            }
            if (rezult == 0){
                String docNumber1 = "";
                String docNumber2 = "";
                if (doc1.getRegNumber() != null && StringUtils.isNotBlank(doc1.getRegNumber())){
                    docNumber1 = doc1.getRegNumber();
                }
                if (doc2.getRegNumber() != null && StringUtils.isNotBlank(doc2.getRegNumber())){
                    docNumber2 = doc2.getRegNumber();
                }
                rezult = collator.compare(docNumber1, docNumber2);
            }
            return rezult;
        };       
        
        List<Object> dataReport = docs.stream().sorted(comparator).collect(Collectors.toList());
        sessionBean.preViewReport(dataReport, params, DictFrmName.REP_DOC_JOURNAL);
    }
    
    /* ПРОЧИЕ МЕТОДЫ */
    
    /* Определяет доступность кнопки "Создать" на панели обозревателя */
    public boolean getCanCreateItem(){
        if (tableBean == null) return false;
        return tableBean.canCreateItem(treeSelectedNode);                
    }
    
    /* Открытие формы администрирования объекта */  
    public void onOpenAdmCardForm() {
        onOpenAdmCardForm(currentItem);
    }    
    public void onOpenAdmCardForm(BaseDict item) {
        if (item == null) return;        
        onSetCurrentItem(item);
        BaseTableBean bean = getItemBean(item);
        bean.setSourceItem(item);        
        sessionBean.openDialogFrm(DictFrmName.FRM_ADMIN_OBJECTS, bean.getParamsMap());
    }           
    
    /* Установка текущей страницы списка данных в обозревателе/селекторе  */
    public void onPageChange(PageEvent event) {
        setCurrentPage(((DataTable) event.getSource()).getFirst());
    }       
            
    /* Построение объекта для сортировки таблицы обозревателя  */
    /*
    public List<SortMeta> getSortOrder() {
        if (sortOrder == null){
            sortOrder = new ArrayList<>();
            UIViewRoot viewRoot =  FacesContext.getCurrentInstance().getViewRoot();
            UIComponent columnIcon = viewRoot.findComponent("mainFRM:tblDetail:colIcon"); 
            UIComponent columnName = viewRoot.findComponent("mainFRM:tblDetail:shortName"); 
            SortMeta sm1 = new SortMeta();
            sm1.setSortBy((UIColumn)columnIcon);
            sm1.setSortField("iconName");
            sm1.setSortOrder(SortOrder.DESCENDING);                        
            SortMeta sm2 = new SortMeta();
            sm2.setSortBy((UIColumn)columnName);
            sm2.setSortField("nameEndElipse");
            sm2.setSortOrder(SortOrder.ASCENDING);
            sortOrder.add(sm1);
            sortOrder.add(sm2);
        }
        return sortOrder;
    }    
    */
    /* Формирование заголовка журнала обозревателя   */ 
    public void makeJurnalHeader(String firstName, String secondName, String toolTipKey){              
        StringBuilder sb = new StringBuilder(firstName);
        sb.append(": ");
        sb.append(secondName); 
        setJurnalHeader(MessageFormat.format(MsgUtils.getMessageLabel(toolTipKey), new Object[]{sb.toString()}));
        setCurrentPage(0);        
    }
    
    public String makeUploadFileCaption(){        
        return getLabelFromBundle("SelectFileAndCreateDoc");
    }
    
    /* Отображает диалог перемещения объекта */
    public void onShowMovedDlg(String dlgName) {
        PrimeFaces.current().executeScript("PF('" + dlgName + "').show();");        
    }   
    
    /* Признак текущего режима отображения контента    */
    public boolean isNowShowDetail(){
        return Objects.equals(currentType, typeDetail);
    }
    public boolean isNowShowTree(){
        return Objects.equals(currentType, typeTree);
    }   
    public boolean isNowShowRoot(){
        return Objects.equals(currentType, typeRoot);
    }
    public boolean isNowShowMix(){
        return Objects.equals(currentType, typeMixed);
    }         
    
    /* Определяет режим отображения: обозреватель или селектор */
    public boolean isSelectorViewMode(){
        return Objects.equals(viewMode, DictExplForm.SELECTOR_MODE);
    }
    public boolean isExplorerViewMode(){
        return Objects.equals(viewMode, DictExplForm.EXPLORER_MODE);
    }
    
    /* Установка текущего элемента в таблице обозревателя */
    public void onSetCurrentItem(BaseDict item){
        currentItem = item;
        getCheckedItems().clear();
        getCheckedItems().add(item);  
    }

    /* Установка режима отображения в обозревателе в зависимости от того, какой(ие) тип(ы) объектов отображаются  */
    public void setCurrentViewModel(TreeNode node){
        BaseDict item = (BaseDict) node.getData();
        if (isItemDetailType(item)){
            setCurrentViewModeDetail();
        } else 
            if (isItemTreeType(item)){
                setCurrentViewModeTree();                
            } else
                if (isItemRootType(item)){
                    setCurrentViewModeRoot();
                }
    }
    public void setCurrentViewModeDetail(){
        currentType = typeDetail;
    }
    public void setCurrentViewModeTree(){
        currentType = typeTree;
    }
    public void setCurrentViewModeRoot(){
        currentType = typeRoot;
    }
    public void setCurrentViewModeMixed(){
        currentType = typeMixed;
    }
    
    /**
     * Запись версии операционной системы клиента
     */
    public void setVersionOS() {        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        versionOS = params.get("version");
    } 
    
    /**
     * Определяет видимость кнопки "Выбрать" в контекстном меню дерева
     * @return 
     */
    public boolean isCanSelectInTree(){
        return isSelectorViewMode() && Objects.equals(tableBean, treeBean);
    }
    
    /* GETS & SETS */       

    public BaseDict getCurrentItem() {
        return currentItem;
    }

    public String getCurrentTab() {
        return currentTab;
    }
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }    

    public void setRootBean(BaseTreeBean rootBean) {
        this.rootBean = rootBean;
        this.typeRoot = rootBean.getLazyFacade().getItemClass().getSimpleName();
        rootBean.setBeanId(rootBean.toString());
    }
    public BaseTreeBean getRootBean() {
        return rootBean;
    }    
    
    public BaseTreeBean getTreeBean() {
        return treeBean;
    }
    public void setTreeBean(BaseTreeBean treeBean) {
        this.treeBean = treeBean;
        this.typeTree = treeBean.getLazyFacade().getItemClass().getSimpleName();
        treeBean.setBeanId(treeBean.toString());
    }
    
    public void setTableBean(BaseTableBean tableBean) {
        this.tableBean = tableBean; 
        this.typeDetail = tableBean.getLazyFacade().getItemClass().getSimpleName();
        tableBean.setBeanId(tableBean.toString());
    }
    public BaseTableBean getTableBean() {
        return tableBean;
    }

    public BaseTableBean getSearcheBean() {
        return searcheBean;
    }
    public void setSearcheBean(BaseTableBean searcheBean) {
        model = searcheBean.initSearcheModel();
        this.searcheBean = searcheBean;
    }

    public String getTypeRoot() {
        return typeRoot;
    }
    public String getTypeDetail() {
        return typeDetail;
    }
    public String getTypeOwner() {
        return typeTree;
    }

    public TreeNode getFilterSelectedNode() {
        return filterSelectedNode;
    }
    public void setFilterSelectedNode(TreeNode filterSelectedNode) {
        this.filterSelectedNode = filterSelectedNode;
    }

    public TreeNode getTreeSelectedNode() {
        return treeSelectedNode;
    }
    public void setTreeSelectedNode(TreeNode treeSelectedNode) {
        this.treeSelectedNode = treeSelectedNode;
    }

    public String getCurrentType() {
        return currentType;
    }

    public Integer getFilterId() {
        return filterId;
    }
    public void setFilterId(Integer filterId) {
        this.filterId = filterId;
    }

    public Integer getFolderId() {
        return folderId;
    }
    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }
    
    public Integer getRowsInPage() {
        return rowsInPage;
    }
    public void setRowsInPage(Integer rowsInPage) {
        this.rowsInPage = rowsInPage;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getSource() {
        return source;
    }
    public void setSource(Integer source) {
        this.source = source;
    }
    
    public String getJurnalHeader() {
        if (jurnalHeader == null){            
            if (isSelectorViewMode()){
                jurnalHeader = selectorHeader;
            }else {
                jurnalHeader = explorerHeader;
            }            
        }
        return jurnalHeader;
    }
    public void setJurnalHeader(String jurnalHeader) {
        this.jurnalHeader = jurnalHeader;
    }

    public String getSelectorHeader() {
        return selectorHeader;
    }
    public void setSelectorHeader(String selectorHeader) {
        this.selectorHeader = selectorHeader;
    }

    public String getExplorerHeader() {
        return explorerHeader;
    }
    public void setExplorerHeader(String explorerHeader) {
        this.explorerHeader = explorerHeader;
    }

    public String getTreeSearcheKey() {
        return treeSearcheKey;
    }
    public void setTreeSearcheKey(String treeSearcheKey) {
        this.treeSearcheKey = treeSearcheKey;
    }
    
    public void setTypeEdit(Integer typeEdit) {
        this.typeEdit = typeEdit;
    }

    public Integer getSelectMode() {
        return selectMode;
    }
    
    public Deque getNavigator() {
        return navigator;
    }    
    
    public Map<String, Object> getCreateParams() {
        return createParams;
    }
    
    public SearcheModel getModel() {
        return model;
    }
    
    public Boolean getLayoutState() {
        return layoutState;
    }
    public void setLayoutState(Boolean layoutState) {
        this.layoutState = layoutState;
    }

    public Integer getSelectedDocId() {
        return selectedDocId;
    }
    public void setSelectedDocId(Integer selectedDocId) {
        this.selectedDocId = selectedDocId;
    }   

    public SortOrder getDefSortOrder() {
        return defSortOrder;
    }
    public void setDefSortOrder(SortOrder defSortOrder) {
        this.defSortOrder = defSortOrder;
    }

    public String getDefSortField() {
        return defSortField;
    }
    public void setDefSortField(String defSortField) {
        this.defSortField = defSortField;
    }    
    
    @Override
    public boolean isEastShow(){
        return true;
    }
    @Override
    public boolean isWestShow(){
        return true;
    }
    
    @Override
    public String getFormName() { 
        /*
        if (isSelectorViewMode()){
            return typeDetail.toLowerCase() + "-selector";
        }
        */
        return typeDetail.toLowerCase() + "-explorer";       
    }
    
    @Override
    public String getFormHeader() {
        return explorerHeader;
    }

    @Override
    protected BaseLazyFacade getLazyFacade() {
        return getItemBean(currentItem).getLazyFacade();
    }
}