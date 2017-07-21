package com.maxfill.escom.beans.explorer;

import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.filters.Filter;
import com.maxfill.facade.FiltersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.folders.FolderNavigation;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.utils.ItemUtils;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.utils.Tuple;
import java.io.IOException;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.UploadedFile;

/* Контролер формы обозревателя */
@Named
@ViewScoped
public class ExplorerBean implements Serializable {
    private static final long serialVersionUID = 5230153127233924868L;   
    static final protected Logger LOGGER = Logger.getGlobal();
    
    private static final String TREE_ITEMS_NAME  = "explorer_west:accord:tree:";
    private static final String TREE_FILTERS_NAME = "explorer_west:accord:filtersTree:";
    
    private static final String TABLE_NAME = "explorer:tblDetail:";
    private static final String NAVIG_NAME = "explorer:navigator";
    private static final Integer LEH_NAVIG_NAME = NAVIG_NAME.length();
    private static final Integer LEH_TREE_ITEMS  = TREE_ITEMS_NAME.length();
    private static final Integer LEH_TREE_FILTERS = TREE_FILTERS_NAME.length();
    private static final Integer LEH_TABLE_NAME = TABLE_NAME.length();
          
    @Inject
    private SessionBean sessionBean;
    @Inject
    private DocBean docBean;
    
    @EJB
    private FiltersFacade filtersFacade;
        
    protected BaseTreeBean rootBean;
    protected BaseTreeBean treeBean;
    protected BaseExplBean tableBean;
    protected BaseExplBean searcheBean;
    
    private BaseDict currentItem;    
    private BaseDict editItem; 
    private Set<BaseDict> copiedItems;
    
    private TreeNode tree;
    private TreeNode filterTree;
    
    private TreeNode filterSelectedNode;
    private TreeNode treeSelectedNode;
    private TreeNode dragNode, dropNode;
        
    private Deque navigator; 

    private final String typeMixed = "mixed";        
    private String currentType = typeMixed;
    
    private String typeDetail;
    private String typeTree;
    private String typeRoot;

    private Integer typeEdit;                   //режим редактирования записи
    
    private List<BaseDict> checkedItems = new ArrayList<>();  //список выбранных объектов на форме обозревателя/селектора
    private List<BaseDict> detailItems = new ArrayList<>();   //список подчинённых объектов
    
    private final Map<String, Object> createParams = new HashMap<>();
    private BaseDict dropItem; 
    
    private SearcheModel model;
    private String treeSearcheKey; 
    
    /* *** ПОЛЯ ОБОЗРЕВАТЕЛЯ *** */
    private LayoutOptions layoutOptions; 
    private Boolean layoutState = true;
    private String jurnalHeader;
    private String selectorHeader;
    private String explorerHeader;
    private String currentTab = DictExplForm.TAB_FILTER;
    private List<SortMeta> sortOrder;
    private Integer rowsInPage = DictExplForm.ROW_IN_PAGE;
    private Integer currentPage = 0;
    
    /* *** СЛУЖЕБНЫЕ ПОЛЯ *** */
    private Integer source = DictDetailSource.TREE_SOURCE;
    private Integer viewMode;          //режим отображения формы
    private Integer selectMode;        //режим выбора для селектора            
    private Integer selectedDocId;      //при открытии обозревателя документов в это поле заносится параметр id документа для открытия
        
    @PostConstruct
    public void init() {        
       // System.out.println("Создан explorerBean="+ this.toString());
    }     
    
    @PreDestroy
    private void destroy(){
        //sessionBean.saveLayoutOptions(layoutOptions, getFrmName());
        //System.out.println("Удалён explorerBean="+ this.toString());
    }
    
    /* Cобытие при открытии формы обозревателя/селектора  */
    public void onOpenExplorer(){
        if (viewMode == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (params.containsKey("selectMode")){
                selectMode = Integer.valueOf(params.get("selectMode"));
                viewMode = DictExplForm.SELECTOR_MODE;
            } else {
                viewMode = DictExplForm.EXPLORER_MODE;
            }
        }    
    }

    /* КАРТОЧКИ ОБЪЕКТОВ */
    
    /* КАРТОЧКИ: открытие карточки объекта для просмотра */
    public void onViewContentItem(){
        BaseDict item = getCurrentItem();
        setTypeEdit(DictEditMode.VIEW_MODE);
        editItem = sessionBean.prepViewItem(item);       
    }
    
    /* КАРТОЧКИ: открытие карточки объекта для редактирование */
    public void onEditContentItem(){
        BaseDict item = getCurrentItem();
        setTypeEdit(DictEditMode.EDIT_MODE);
        /*
        if (!isItemDetailType(item)){
            makeSelectedGroup(item);
        }
        */
        editItem = sessionBean.prepEditItem(item);
    }

    /* КАРТОЧКИ: создание объекта в дереве с открытием карточки */
    public void onCreateTreeItem() {
        BaseDict selected = (BaseDict) treeSelectedNode.getData();
        if (selected == null){
            return;
        }
        BaseDict owner = null;
        BaseDict parent = null;
        if (isItemTreeType(selected)){ 
            parent = selected;            
        } else   
            if (isItemRootType(selected)){                
                owner = selected;
            }
        typeEdit = DictEditMode.INSERT_MODE;        
        editItem = treeBean.createItemAndOpenCard(parent, owner, createParams);
    }
    
    /* КАРТОЧКИ: создание объекта в дереве на нулевом уровне */
    public void onCreateRootItem() {
        BaseDict owner = null;
        BaseDict parent = null;
        typeEdit = DictEditMode.INSERT_MODE;
        editItem = rootBean.createItemAndOpenCard(parent, owner, createParams);
    }
    
    /* КАРТОЧКИ: создание объекта в таблице с открытием его карточки  */
    public void onCreateDetailItem(){
        typeEdit = DictEditMode.INSERT_MODE;
        BaseDict parent = null;
        BaseDict owner = null;
        if (treeSelectedNode != null){
            owner = (BaseDict) treeSelectedNode.getData();
        }        
        editItem = tableBean.createItemAndOpenCard(parent, owner, createParams);
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
            EscomBeanUtils.showErrorsMsg(errors);
            return false;
        }
        return true;
    }
        
    /* КАРТОЧКИ: обработка после закрытия карточки  */
    public void onUpdateAfterCloseForm(SelectEvent event){
        Tuple<Boolean, String> tuple = (Tuple) event.getObject();
        Boolean isNeedUdate = tuple.a;
        if (isNeedUdate) {
            switch (typeEdit){
                case DictEditMode.EDIT_MODE: {                    
                    try {                    
                        BeanUtils.copyProperties(currentItem, editItem);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }                    
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    if (isItemTreeType(editItem) || isItemRootType(editItem)){
                        TreeNode newNode;                    
                        if (editItem.getParent() == null){
                            newNode = addNewItemInTree(editItem, tree);
                        } else {
                            newNode = addNewItemInTree(editItem, treeSelectedNode);
                        }
                        onSelectInTree(newNode);
                    }
                    break;
                }
            }
            EscomBeanUtils.SuccesFormatMessage("Successfully", "DataIsSaved", new Object[]{editItem.getName()});
        }
        createParams.clear();
        reloadDetailsItems(); 
        onSetCurrentItem(editItem);
    }
    
    public void onUpdateAfterChangeItem(SelectEvent event){
        editItem = sessionBean.reloadItem(currentItem);
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
        getDetailItems().removeAll(getCheckedItems());        
    }
    
    /* ИЗБРАННОЕ: удаление из избранного записи контента. */  
    public void onDelContentFromFavorites(BaseDict item){
        if (isItemDetailType(item)){
            tableBean.delFromFavorites(item);
        } else
            if (isItemTreeType(item)){
                treeBean.delFromFavorites(item);
            }
        getDetailItems().remove(item);         
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
        getDetailItems().removeAll(getCheckedItems());     
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
            }      
        getDetailItems().remove(item);         
    }
    
    /* КОРЗИНА: помещение в корзину отмеченных записей контента  */
    public void onMoveCheckedContentToTrash(){
        Set<String> errors = new HashSet<>();
        getCheckedItems().stream()
                .forEach(item -> {
                    if (isItemDetailType(item)){
                        tableBean.moveToTrash(item, errors);
                    } else
                        if (isItemTreeType(item)){
                            treeBean.moveToTrash(item, errors);
                            TreeNode node = EscomBeanUtils.findTreeNode(tree, item); 
                            node.getParent().getChildren().remove(node); 
                        } else
                            if (isItemRootType(item)){
                                rootBean.moveToTrash(item, errors);
                            }
                });
        if (!errors.isEmpty()) {
            EscomBeanUtils.showErrorsMsg(errors);
        } else {            
            getDetailItems().removeAll(getCheckedItems());
        }
    }
    
    /* КОРЗИНА: перемещение контента в корзину  */
    public void onMoveContentToTrash(BaseDict item){
        getCheckedItems().clear();
        getCheckedItems().add(item);
        onMoveCheckedContentToTrash();
    }
    public void onMoveContentToTrash(BaseDict item, Set<String> errors){
        if (item == null) return;
        
        if (isItemDetailType(item)){
            tableBean.moveToTrash(item, errors);            
        } else
            if (isItemTreeType(item)){
                treeBean.moveToTrash(item, errors); 
                TreeNode node = EscomBeanUtils.findTreeNode(tree, item); 
                node.getParent().getChildren().remove(node); 
            } else
                if (isItemRootType(item)){
                    rootBean.moveToTrash(item, errors);
                }              
    }
    
    /* КОРЗИНА: перемещение записи из дерева в корзину  */
    public void onMoveTreeItemToTrash(){
        if (treeSelectedNode == null){
            return;
        }
        Set<String> errors = new HashSet<>();
        onMoveContentToTrash(currentItem, errors);
        if (!errors.isEmpty()) {
            EscomBeanUtils.showErrorsMsg(errors);            
        } else {
            removeNodeFromTree(treeSelectedNode);
            reloadDetailsItems();
        }
    }
    
    /* КОРЗИНА: полная очистка корзины без проверки на наличие зависимых связей 
    Очистка удаляет все дочерние и детальные объекты! Команда доступна только администратору */
    public void onClearTrash() {        
        getDetailItems().stream().forEach((item -> {
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
        reloadDetailsItems();
    }
    
    /* КОРЗИНА: удаление из корзины выбранных записей контента */
    public void onClearCheckedContentTrash(){
        getCheckedItems().stream().forEach((item -> {
            if (isItemDetailType(item)){
                tableBean.deleteItem(item);
            } else
                if (isItemTreeType(item)){
                    treeBean.deleteItem(item);
                }
        }));
        getDetailItems().removeAll(getCheckedItems());
    }
    
    /* КОРЗИНА: удаление из корзины объекта контента  */
    public void onDeleteContentFromTrash(BaseDict item){
        if (isItemDetailType(item)){
            tableBean.deleteItem(item);
        } else
            if (isItemTreeType(item)){
                treeBean.deleteItem(item);
            }
        getDetailItems().remove(item);        
    }
    
    /* ФИЛЬТРЫ */
    
    /* ФИЛЬТР: Формирует дерево фильтров */
    public TreeNode getFilterTree() {
        if (filterTree == null) {
            filterTree = new DefaultTreeNode("Root", null);
            filterTree.setExpanded(true);
            
            //формируем корневой элемент для фильтров detail объекта
            List<Filter> sourceTreeItems = filtersFacade.findRootItems();
            Metadates tableMD = tableBean.getMetadatesObj();
            final String tableJurnalName = tableMD.getBundleJurnalName();
            sourceTreeItems.stream()
                    .forEach(treeItem -> {
                        treeItem.setIcon(tableMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeDetail, tableJurnalName, tableMD);
                    }
            );

            //формируем корневой элемент для фильтров tree объекта
            if (treeBean != null){
                sourceTreeItems = filtersFacade.findRootItems();
                Metadates treeMD = treeBean.getMetadatesObj();
                final String treeJurnalName = treeMD.getBundleJurnalName();
                sourceTreeItems.stream()
                    .forEach(treeItem -> {
                        treeItem.setIcon(treeMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeTree, treeJurnalName, treeMD);
                    }
                );
            }
            //формируем корневой элемент для фильтров root объекта
            if (rootBean != null){
                sourceTreeItems = filtersFacade.findRootItems();
                Metadates rootMD = rootBean.getMetadatesObj();
                final String treeJurnalName = rootMD.getBundleJurnalName();
                sourceTreeItems.stream()
                    .forEach(treeItem -> {
                        treeItem.setIcon(rootMD.getIconObject());
                        addFilterInTree(filterTree, treeItem, typeRoot, treeJurnalName, rootMD);
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

        String bundleName = EscomBeanUtils.getBandleLabel(nodeName);
        item.setName(bundleName);

        List<Filter> childs = filtersFacade.findChildsFilters((Filter)item, metadate);
        childs.stream()
                .forEach(itemChild -> addFilterInTree(newNode, itemChild, nodeType, itemChild.getName(), metadate)
        );        

        return newNode;
    }
    
    /* ФИЛЬТР: обработка события щелчка по фильтру на форме */
    public void onFilterTreeNodeSelect(NodeSelectEvent event) {
        filterSelectedNode = event.getTreeNode();
        doFilterTreeNodeSelect(filterSelectedNode);
    }
    
    /* ФИЛЬТР: обработка события выбора фильтра */
    private void doFilterTreeNodeSelect(TreeNode node){
        if (node == null) return;
        
        setCurrentTab(DictExplForm.TAB_FILTER);
        Filter filter = (Filter) node.getData();
        if (filter == null){
            return;
        }
        doMakeFilterJurnalHeader(node, filter);                   
        List<BaseDict> result = null;
        if (node.getType().equals(typeDetail)){
            result = tableBean.makeFilteredContent(filter);                    
            setCurrentViewModeDetail();
        } else
            if (node.getType().equals(typeTree)){
                result = treeBean.makeFilteredContent(filter);
                setCurrentViewModeTree();
            } else
                if (node.getType().equals(typeRoot)){
                    result = rootBean.makeFilteredContent(filter);
                    setCurrentViewModeRoot();
                }
        setDetails(result, DictDetailSource.FILTER_SOURCE);
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
        makeJurnalHeader(firstName, filter.getName());
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
        currentTab = tab.getId();        
        switch (currentTab) {
            case DictExplForm.TAB_TREE: {                
                onSelectInTree(treeSelectedNode);
                break;
            }
            case DictExplForm.TAB_FILTER: {
                doFilterTreeNodeSelect(filterSelectedNode);
                break;
            }
        }
    }
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА */
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: установка текущего объекта по двойному клику в таблице обозревателя   */
    public void onRowDblClckOpen(SelectEvent event){
        BaseDict item = (BaseDict) event.getObject();
        onSetCurrentItem(item);        
    }
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: обновление данных в таблице обозревателя  */
    public void reloadDetailsItems() {        
        detailItems = null;
    }        

    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: возвращает список объектов для таблицы обозревателя  */
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
            }
        }
        return detailItems;
    }
    
    /* ОБОЗРЕВАТЕЛь ТАБЛИЦА: установка списка таблицы обозревателя  */
    public void setDetails(List<BaseDict> details, int source) {
        setSource(source);
        this.detailItems = details;
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
        if (treeSelectedNode == null){
            return;
        }
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
        reloadDetailsItems();
        setSource(DictDetailSource.TREE_SOURCE);        
    }
    
    /* ДЕРЕВО: свернуть ветви */
    public void collapseTree() {
        getTree().getChildren().stream().forEach(node -> node.setExpanded(false));
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
            } else {
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
        if (node == null) {
            return;
        }
        if (treeSelectedNode != null) {
            treeSelectedNode.setSelected(false);
        }
        treeSelectedNode = node;
        setCurrentTab(DictExplForm.TAB_TREE);
        treeSelectedNode.setSelected(true);
        currentItem = (BaseDict) treeSelectedNode.getData();
        List<BaseDict> details = null; 
        if (isItemTreeType(currentItem)){
            details = treeBean.makeGroupContent(currentItem, viewMode);
        } else
            if (isItemRootType(currentItem)){
                details = rootBean.makeGroupContent(currentItem, viewMode);
            }
        setDetails(details, DictDetailSource.TREE_SOURCE); 
                       
        makeNavigator(currentItem);         
        setCurrentViewModeMixed();
       
        BaseDict rootItem = (BaseDict) tree.getChildren().get(0).getData();            
        makeJurnalHeader(rootItem.getName(), currentItem.getName());
    }
    
    /* ДЕРЕВО: установка текущего элемента в дереве по заданному объекту item */
    public void makeSelectedGroup(BaseDict item){      
        if (item == null){
            return;
        }                             
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
        onEditContentItem();        
    }
    
    /* ДЕРЕВО: добавление нового объекта в дерево  */
    public TreeNode addNewItemInTree(BaseDict item, TreeNode parentNode){        
        if (item == null){return null;}        
        if (parentNode == null){
            parentNode = tree;
        }
        parentNode.setExpanded(true);
        String type = "tree";
        if (isItemRootType(item)){
            type = typeRoot;
        }
        TreeNode newNode = new DefaultTreeNode(type, item, parentNode);
        item.getChildItems().stream().forEach(child -> addNewItemInTree((BaseDict)child, newNode));
        return newNode;
    }    
         
    /* ДЕРЕВО: удаление узла в дереве  */
    public void removeNodeFromTree(TreeNode node){
        if (node == null){
            return;
        }
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
        return treeSelectedNode.getParent().equals(tree);
    }
    
    /* ДЕРЕВО: разворачивает текущую ветку от текущего элемента вглубь */
    public void onExpandNode(){
        expandDown(treeSelectedNode);
    };
    
    /* ДЕРЕВО: разворачивает ветку вглубь */
    private void expandDown(TreeNode node){
        if (node == null){
            return;
        }
        node.setExpanded(true);
        node.getChildren().stream().forEach(childNode -> expandDown(childNode));
    }
    
    /* ДЕРЕВО: Разворачивает ветку от текущего узла до корня */
    private void expandUp(TreeNode node){
        node.setExpanded(true);
        if (node.getParent() != null){
            expandUp(node.getParent());
        }
    }
    
    /* ДЕРЕВО: Cворачивает текущую ветку от текущего элемента вглубь  */
    public void onCollapseNode(){
        doCollapseNode(treeSelectedNode);
    };
   
    /* ДЕРЕВО: Cворачивает ветку  */
    private void doCollapseNode(TreeNode node){
        if (node == null){
            return;
        }
        node.setExpanded(false);
        node.getChildren().stream().forEach(childNode -> doCollapseNode(childNode));
    }
    
    /* ДЕРЕВО: Обработка события развёртывания node  */
    public void onNodeExpand(NodeExpandEvent event){
        TreeNode node = event.getTreeNode();
        if (node == null){
            return;
        }
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
            if (StringUtils.containsIgnoreCase(item.getName(), treeSearcheKey)){
                node.setSelected(true);
                expandUp(node);
                rezult.add(node);
            }
            return node;
        }).forEach(node -> doSearcheInTree(node, rezult));
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
        if (checkedItems.isEmpty()){
            EscomBeanUtils.WarnMsgAdd("Warning", "NoCheckedItems");
            return;
        }
        doCopyItems(checkedItems);
    }

    /* КОПИРОВАНИЕ: вызов копирования объекта из дерева */
    public void onCopySelectedTreeItem() {
        onCopyItem(currentItem);
    }

    /* КОПИРОВАНИЕ: вызов копирования объекта из таблицы обозревателя */
    public void onCopyItem(BaseDict item) {
        if (item == null){return;}
        if (item.getId() == 0){
            EscomBeanUtils.ErrorFormatMessage("Error", "ObjectNotCopied", new Object[]{item.getName()});
            return;
        }
        List<BaseDict> sourceItems = new ArrayList<>();
        sourceItems.add(item);
        doCopyItems(sourceItems);
    }

    /* КОПИРОВАНИЕ: копирование объектов в память  */
    public void doCopyItems(List<BaseDict> sourceItems) {
        copiedItems = sourceItems.stream().map(copyItem -> sessionBean.prepCopyItem(copyItem)).collect(Collectors.toSet()); //копируем и сохраняем в copiedItems
        copiedItems.stream().forEach(item-> EscomBeanUtils.SuccesFormatMessage("Successfully", "ObjectIsCopied", new Object[]{item.getName()}));
    }

    /* ВСТАВКА: вставка объекта в дерево */
    public void onPasteItemToTree(){
        Set<String> errors = new HashSet<>();
        List<BaseDict> rezults = pasteItem(currentItem, errors);
        if (!errors.isEmpty()){
            EscomBeanUtils.showErrorsMsg(errors);
        }
        if (!rezults.isEmpty()){
            rezults.stream().filter(item-> isItemRootType(item) || isItemTreeType(item))
                .forEach(item -> addNewItemInTree(item, treeSelectedNode));
            reloadDetailsItems();
        }
    }

    /* ВСТАВКА: вставка объекта в таблицу обозревателя */
    public void onPasteItemToTable(){
        Set<String> errors = new HashSet<>();
        BaseDict parent = null;
        if (treeSelectedNode != null){
            parent = (BaseDict)treeSelectedNode.getData();
        }
        pasteItem(parent, errors);
        if (!errors.isEmpty()){            
            EscomBeanUtils.showErrorsMsg(errors);
            return;
        }
        reloadDetailsItems();
    }
    
    /* ВСТАВКА: обработка списка объектов для их вставки. Parent в данном контексте обозначает, куда(во что) помещается объект.
    Реальный parent будет установлен в бине объекта */
    private List<BaseDict> pasteItem(BaseDict parent, Set<String> errors) {
        if (copiedItems == null){return null;}
        List<BaseDict> rezults = new ArrayList<>();
        copiedItems.stream().forEach(item -> {
            BaseDict pasteItem = sessionBean.prepPasteItem(item, parent, errors);
            if (pasteItem != null){
                rezults.add(pasteItem);
            }
        });
        return rezults;
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
            EscomBeanUtils.ErrorMsgAdd("Error", "NO_SEARCHE_GROUPS", "");
        } else {
            doSearcheItems();
            if (getDetailItems().isEmpty()) {
                EscomBeanUtils.WarnMsgAdd("Info", "NO_SEARCHE_FIND");
                return;
            }
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
    }
    
    public void dateCreateStartChange() {
        Date dateCreateEnd = getModel().getDateCreateEnd();
        if (dateCreateEnd == null) {
            getModel().setDateCreateEnd(getModel().getDateCreateStart());
        }
    }

    public void dateChangeStartChange() {
        Date dateChangeEnd = getModel().getDateChangeEnd();
        if (dateChangeEnd == null) {
            getModel().setDateChangeEnd(getModel().getDateChangeStart());
        }
    }
    
    /* Выполняет поиск объектов с учётом критериев поиска  */
    public void doSearcheItems() {
        List<BaseDict> searcheGroups = new ArrayList<>();
        Map<String, Object> paramEQ = new HashMap<>();
        Map<String, Object> paramLIKE = new HashMap<>();
        Map<String, Object> paramIN = new HashMap<>();
        Map<String, Date[]> paramDATE = new HashMap<>();

        //готовим группы в которых будет поиск
        if (model.isSearcheInGroups()) {
            TreeNode ownerNode = getTreeSelectedNode();
            if (ownerNode != null) {
                BaseDict owner = (BaseDict) ownerNode.getData();
                searcheGroups.addAll(owner.getChildItems());
                if (!searcheGroups.contains(owner)) {
                    searcheGroups.add(owner);
                }
            }
        }

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
        if (StringUtils.isNotEmpty(name) && !SysParams.ALL.equals(name.trim())) {
            paramLIKE.put("name", name);
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
                
        List<BaseDict> result = searcheBean.doSearche(statesIds, paramEQ, paramLIKE, paramIN, paramDATE, searcheGroups, addParams);
        
        setDetails(result, DictDetailSource.SEARCHE_SOURCE);
        setCurrentViewModeDetail();
        makeJurnalHeader(EscomBeanUtils.getBandleLabel(searcheBean.getMetadatesObj().getBundleJurnalName()), EscomBeanUtils.getBandleLabel("SearcheResult"));
    }
    
    /* Выполняет поиск в дереве объектов */
    public void onSearcheInTree(){
        if (StringUtils.isNotBlank(treeSearcheKey)){
            clearTree(tree);
            List<TreeNode> rezult = new ArrayList<>();
            doSearcheInTree(tree, rezult);
            if (rezult.isEmpty()){
                EscomBeanUtils.WarnMsgAdd("Info", "NO_SEARCHE_FIND");
                return;
            }
            if (rezult.size() == 1){
                onSelectInTree(rezult.get(0));
            }
        }
    }
    
    /* DRAG & DROP */          
    
    /*  Формирование списка объектов для перетаскивания.  В список включается перетаскиваемый объект и уже отмеченные объекты  */
    private void makeDragList(BaseDict dragItem){
        if (dragItem == null){
            checkedItems.clear();
            return;
        }
        if (!checkedItems.contains(dragItem)){
            checkedItems.add(dragItem);
        }
    }
    
    /* Обработка события drop в дерево объектов  */
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
        }
        
        if (dropNode != null) {            
            dropItem = (BaseDict) dropNode.getData();   //определили получателя

            //определям источник : объект тянется из дерева             
            if (dragId.substring(0, LEH_TREE_ITEMS).equals(TREE_ITEMS_NAME)) {
                String rkNode = dragId.substring(LEH_TREE_ITEMS, dragId.length());
                dragNode = EscomBeanUtils.findUiTreeNode(getTree(), rkNode);
                BaseDict dragItem = (BaseDict) dragNode.getData();                
                checkedItems.clear();
                makeDragList(dragItem);
                if (!checkedItems.isEmpty()){
                    Set<String> errors = new HashSet<>();
                    //проверяем, что тянем, так как может быть тянем root, а это пока не допустимо!
                    if (isItemTreeType(dragItem)){                        
                        // если тянем treeItem и бросаем в treeItem
                        if (treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                            onShowMovedDlg("MoveTreeDlg");
                        }                       
                    } else {
                        String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("MoveItemNotAvailable"), new Object[]{dragItem.getName()});
                        errors.add(error);
                    }
                    if (!errors.isEmpty()) {
                        EscomBeanUtils.showErrorsMsg(errors);
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
                makeDragList(dragItem);
                if (!checkedItems.isEmpty()){
                    switch (currentTab){ //в зависимости от того, какое открыто дерево
                        case DictExplForm.TAB_TREE:{
                            doDropToTree(checkedItems);
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
    
    /* Обработка drop помещения объекта в фильтр */
    private void doDropToFilter(List<BaseDict> dragItems, Filter filter){        
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
        }
        if (!errors.isEmpty()) {
            EscomBeanUtils.showErrorsMsg(errors);
        }    
    }
    
    /* Обработка drop помещения объекта в дерево */
    private void doDropToTree(List<BaseDict> dragItems){
        Set<String> errors = new HashSet<>();
        switch (source){
            case DictDetailSource.TREE_SOURCE:{    //если источник для detail дерево, то будем перемещать объект                            
                checkedItems =
                    dragItems.stream().filter(dragItem -> 
                        // если тянем datailItem и бросаем в treeItem
                        (isItemDetailType(dragItem) && isItemTreeType(dropItem) && tableBean.prepareMoveItemToGroup(dropItem, dragItem, errors))
                        || // если тянем treeItem и бросаем в treeItem
                        (isItemTreeType(dragItem) && isItemTreeType(dropItem) && treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors))
                        || // если тянем datailItem и бросаем в rootItem
                        (isItemDetailType(dragItem) && isItemRootType(dropItem) && tableBean.prepareMoveItemToGroup(dropItem, dragItem, errors))
                    ).collect(Collectors.toList());
                onShowMovedDlg("MoveTblDlg");
                break;
            }
            case DictDetailSource.SEARCHE_SOURCE:{ //если источник для detail поиск, то будем добавлять в группу
                checkedItems =
                    dragItems.stream().filter(dragItem -> 
                        // если тянем datailItem и бросаем в treeItem
                        (isItemDetailType(dragItem) && isItemTreeType(dropItem) && tableBean.prepareAddItemToGroup(dropItem, dragItem, errors))
                        || // если тянем datailItem и бросаем в treeItem
                        (isItemDetailType(dragItem) && isItemRootType(dropItem) && tableBean.prepareAddItemToGroup(dropItem, dragItem, errors))                  
                    ).collect(Collectors.toList()); 
                onShowMovedDlg("AddTblDlg");
                break;
            }
        }
        if (!errors.isEmpty()) {
            EscomBeanUtils.showErrorsMsg(errors);
        }
    }
        
    /* Обработка события drop в таблицу обозревателя объектов  */
    public void dropToTable(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String dropId = params.get("dropId"); //получаем id приёмника, куда поместили объект
        String dragId = params.get("dragId"); //получаем id источника 

        //ищем в таблице запись приёмника
        String rkTbl = dropId.substring(LEH_TABLE_NAME, dropId.length());
        String rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
        Integer tbKey = Integer.parseInt(rwKey);
        dropItem = EscomBeanUtils.findUITableContent(getDetailItems(), tbKey);
        if (dropItem != null) {
            //ищем в таблице запись источника    
            rkTbl = dragId.substring(LEH_TABLE_NAME, dragId.length());
            rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
            tbKey = Integer.parseInt(rwKey);
            BaseDict dragItem = EscomBeanUtils.findUITableContent(getDetailItems(), tbKey);
            makeDragList(dragItem);
            if (!checkedItems.isEmpty()) { 
                Set<String> errors = new HashSet<>();
                if (isItemDetailType(dragItem) && tableBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                   onShowMovedDlg("MoveTblDlg");
                } else{
                    dragNode = EscomBeanUtils.findTreeNode(tree, dragItem);
                    dropNode = EscomBeanUtils.findTreeNode(tree, dropItem);
                    if (isItemTreeType(dragItem) && isItemTreeType(dropItem) && treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                        onShowMovedDlg("MoveTreeDlg");
                    }
                }
                if (!errors.isEmpty()) {
                    EscomBeanUtils.showErrorsMsg(errors);
                }    
            }
        } else {
            EscomBeanUtils.ErrorMsgAdd("Error", "ErrUnableDetermineID", ""); //не удалось определить идентификатор получателя операции
        } 
    } 
    
    /* DRAG & DROP: обработка drop в навигаторе */ 
    public void dropToNavig() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String dragId = params.get("dragId"); //получаем источник 
        String dropId = params.get("dropId"); //получаем приёмник 

        Integer lenDrop = dropId.length();

        String rkTbl = dropId.substring(LEH_NAVIG_NAME, lenDrop);
        String rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
        
        dropItem = (Folder) EscomBeanUtils.findUiNavigatorItem(getNavigator(), Integer.parseInt(rwKey));
        dropNode = EscomBeanUtils.findTreeNode(tree, (Folder) dropItem);

        if (dragId.substring(0, LEH_TABLE_NAME).equals(TABLE_NAME)) {
            rkTbl = dragId.substring(LEH_TABLE_NAME, dragId.length());
            rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
            Integer tbKey = Integer.parseInt(rwKey);
            BaseDict flDrag = EscomBeanUtils.findUITableContent(getDetailItems(), tbKey);
            BaseDict dragItem = flDrag;
            Set<String> errors = new HashSet<>();
            if (isItemDetailType(flDrag)){
                tableBean.prepareMoveItemToGroup(dropItem, dragItem, errors);            
            } else
                if (isItemTreeType(flDrag)){
                    dragNode = EscomBeanUtils.findTreeNode(tree, dragItem);
                    treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors);
                }                
            if (!errors.isEmpty()) {
                EscomBeanUtils.showErrorsMsg(errors);
            }    
        }
        EscomBeanUtils.ErrorMsgAdd("Error", "ErrUnableDetermineID", ""); //не удалось определить идентификатор получателя операции
    }
    
    /* DRAG & DROP: отработка команды на перемещение в дереве */
    public void moveGroupToGroup() {
        checkedItems.stream().forEach(dragItem -> {
            treeBean.moveGroupToGroup(dropItem, dragItem); //делаем изменения в модели данных

            //удаляем позицию из его предыдущего родителя
            TreeNode dragParentNode = dragNode.getParent();
            dragParentNode.getChildren().remove(dragNode);

            //добавляем объект к новому родителю
            dropNode.getChildren().add(dragNode);
            makeNavigator(dragItem);
        });        
        reloadDetailsItems();
    }
    
    /* DRAG & DROP: отработка команды на перемещение из таблицы в дерево  */
    public void moveItemToGroup(){
        checkedItems.stream().forEach(dragItem -> {            
            tableBean.moveItemToGroup(dropItem, dragItem, treeSelectedNode);
        });
        getDetailItems().removeAll(checkedItems);
    }
    
    /* DRAG & DROP добавление объекта в группу */
    public void addItemToGroup(){
        if (!isItemTreeType(dropItem)){ //если бросили в treeItem               
            return;
        }
        checkedItems.stream()
                .filter(dragItem -> !isItemRootType(dragItem))
                .forEach(dragItem -> {
                    if (sessionBean.prepAddItemToGroup(dragItem, dropItem)){
                        EscomBeanUtils.SuccesFormatMessage("Successfully", "AddObjectToGroupComplete", new Object[]{dragItem.getName(), dropItem.getName()});
                    }
                });
    }
    
    /* DRAG & DROP: перемещение объекта в корзину */
    public void dropItemToTrash(){
        Set<String> errors = new HashSet<>();
        checkedItems.stream().forEach(dragItem -> onMoveContentToTrash(dragItem, errors));
        if (!errors.isEmpty()){            
            EscomBeanUtils.showErrorsMsg(errors);         
        }
    }
    
    /* DRAG & DROP: перемещение объекта в не актулаьные */
    public void dropItemToNotActual(){
        checkedItems.stream().forEach(dragItem -> {
            if (isItemDetailType(dragItem)){
                tableBean.moveItemToNotActual(dragItem);
            }
        });    
    }
            
    /* СЕЛЕКТОР */
    
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
        return Objects.equals(item.getClass().getSimpleName(), searcheBean.getItemClass().getSimpleName());
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
        return doClose(getCheckedItems());
    }
    
    /* СЕЛЕКТОР: закрытие формы селектора  */
    private String doClose(List<BaseDict> selected){
        RequestContext.getCurrentInstance().closeDialog(selected);
        return "/view/index?faces-redirect=true";
    }
    
    /* СЕЛЕКТОР: закрытие селектора без выбора объектов  */
    public String onClose() {
        getCheckedItems().clear();        
        return doClose(getCheckedItems());
    }
    
    /* СЕЛЕКТОР: действие по нажатию кнопки множественного выбора для списка  */
    public String onMultySelect() {        
        return doClose(getCheckedItems());
    }        
        
    /* СЕЛЕКТОР: обработка действия в селекторе объектов при выборе элемента в дереве   */
    public String onSelectTreeItem() {        
        if (currentItem == null || !isCanSelectedItem(currentItem)){
            return "";
        }        
        List<BaseDict> groups = new ArrayList<>();
        groups.add(currentItem);
        return doClose(groups);
    }
    
    /* ДОКУМЕНТЫ И ВЛОЖЕНИЯ */
    
    public boolean isItemHaveAttache(BaseDict item){
        if (!item.getClass().getSimpleName().equals(DictObjectName.DOC)){
            return false;
        }
        Doc doc = (Doc) item;
        return doc.getAttache() != null;
    }   
       
    public void onUploadFile(FileUploadEvent event) throws IOException{        
        if (checkCanCreateDetailItem(new HashSet<>())){
            UploadedFile uploadFile = EscomFileUtils.handleUploadFile(event); 
            Attaches attache = sessionBean.uploadAtache(uploadFile);
            createParams.put("attache", attache);
        }
    }    
        
    /* Показать документ в папке */
    public void onShowDocInFolder(Doc doc){
        if (doc != null){
            Folder owner = (Folder) doc.getOwner();
            TreeNode node = null;
            if (owner != null){
                node = EscomBeanUtils.findTreeNode(getTree(), owner);
            }
            if (getTreeSelectedNode() != null) {
                getTreeSelectedNode().setSelected(false);
            }
            setTreeSelectedNode(node);
            setSelectedDocId(null);
            RequestContext.getCurrentInstance().execute("PF('accordion').select(0);");
        }
    }
    
    /* Подготовка вложений документов для отправки на e-mail  */
    public void prepareSendMailDocs(String mode){
        List<BaseDict> checked = prepareCheckedItems();
        if (!checked.isEmpty()){
            sessionBean.openMailMsgForm(mode, checked);
        } else {
            EscomBeanUtils.WarnMsgAdd("Error", "NO_SELECT_DOCS");
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
        onSetCurrentItem(item);
        docBean.onViewMainAttache((Doc) currentItem);
    }
    
    /* ПРОЧИЕ МЕТОДЫ */
    
    /* Открытие формы администрирования объекта */  
    public void onOpenAdmCardForm() {
        onOpenAdmCardForm(currentItem);
    }    
    public void onOpenAdmCardForm(BaseDict item) {
        if (item == null){
            return;
        }
        onSetCurrentItem(item);
        sessionBean.openAdmCardForm(item);               
    }           
    
    /* Установка текущей страницы списка данных в обозревателе/селекторе  */
    public void onPageChange(PageEvent event) {
        setCurrentPage(((DataTable) event.getSource()).getFirst());
    }   
    
    public String getLabelFromBundle(String key){
        return EscomBeanUtils.getBandleLabel(key);
    }
            
    /* Построение объекта для сортировки таблицы обозревателя  */
    public List<SortMeta> getSortOrder() {
        if (sortOrder == null){
            sortOrder = new ArrayList<>();
            UIViewRoot viewRoot =  FacesContext.getCurrentInstance().getViewRoot();
            UIComponent columnIcon = viewRoot.findComponent("explorer:tblDetail:colIcon"); 
            UIComponent columnName = viewRoot.findComponent("explorer:tblDetail:shortName"); 
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
    
    /* Формирование заголовка журнала обозревателя   */ 
    public String makeJurnalHeader(String firstName, String secondName){      
        StringBuilder sb = new StringBuilder(firstName);
        sb.append(": ");
        sb.append(secondName); 
        setJurnalHeader(sb.toString());
        setCurrentPage(0);
        return sb.toString();
    }
    
    /* Отображает диалог перемещения объекта */
    public void onShowMovedDlg(String dlgName) {
        RequestContext.getCurrentInstance().execute("PF('" + dlgName + "').show();");
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
    
    /* GETS & SETS */
   
    public BaseDict getCurrentItem() {
        return currentItem;
    }
        
    public void setCheckedItems(List<BaseDict> items) {
        checkedItems = items;
    }
    public List<BaseDict> getCheckedItems() {
        return checkedItems;
    }      

    public void setRootBean(BaseTreeBean rootBean) {
        this.rootBean = rootBean;
        this.typeRoot = rootBean.getItemClass().getSimpleName();
    }
    public BaseTreeBean getRootBean() {
        return rootBean;
    }    
    
    public BaseTreeBean getTreeBean() {
        return treeBean;
    }
    public void setTreeBean(BaseTreeBean treeBean) {
        this.treeBean = treeBean;
        this.typeTree = treeBean.getItemClass().getSimpleName();
        currentTab = DictExplForm.TAB_TREE;
    }
    
    public void setTableBean(BaseExplBean tableBean) {
        this.tableBean = tableBean; 
        this.typeDetail = tableBean.getItemClass().getSimpleName();
    }
    public BaseExplBean getTableBean() {
        return tableBean;
    }

    public BaseExplBean getSearcheBean() {
        return searcheBean;
    }
    public void setSearcheBean(BaseExplBean searcheBean) {
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

    public Set<BaseDict> getCopiedItems() {
        return copiedItems;
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
     
    public String getCurrentTab() {
        return currentTab;
    }
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
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

    public LayoutOptions getLayoutOptions() {
        if (layoutOptions == null){
            layoutOptions = sessionBean.getExplLayoutOptions(getFrmName());
        }
        return layoutOptions;
    }

    private String getFrmName(){         
        if (isSelectorViewMode()){
            return typeDetail + "-selector";
        }
        return typeDetail + "-explorer";
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
}