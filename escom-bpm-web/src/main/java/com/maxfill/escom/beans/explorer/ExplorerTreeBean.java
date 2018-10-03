package com.maxfill.escom.beans.explorer;

import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.filter.Filter;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.utils.ItemUtils;
import java.io.File;
import java.net.HttpURLConnection;
import org.primefaces.model.TreeNode;
import javax.faces.context.FacesContext;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/* Контролер формы обозревателя c поддержкой групп */
@Named
@ViewScoped
public class ExplorerTreeBean extends ExplorerBean{
    private static final long serialVersionUID = -5568149615717031598L;

    @EJB
    private AttacheService attacheService;
            
    protected static final String TREE_ITEMS_NAME  = "westFRM:accord:tree:";
    protected static final String TREE_FILTERS_NAME = "westFRM:accord:filtersTree:";

    protected static final String TABLE_NAME = "mainFRM:tblDetail:";
    protected static final String NAVIG_NAME = "mainFRM:navigator";

    protected static final Integer LEH_NAVIG_NAME = NAVIG_NAME.length();
    protected static final Integer LEH_TREE_ITEMS  = TREE_ITEMS_NAME.length();
    protected static final Integer LEH_TREE_FILTERS = TREE_FILTERS_NAME.length();
    protected static final Integer LEH_TABLE_NAME = TABLE_NAME.length();   
    
    @Override
    public void onAfterFormLoad(){
        if (getFolderId() !=null){            
            BaseDict folder = treeBean.getLazyFacade().find(getFolderId());
            if (folder != null){
                makeSelectedFolder(folder);
                setFolderId(null);
            }
        }
    }
    
    /**
     * Находит и выделяет папку в дереве
     * @param folder 
     */
    public void makeSelectedFolder(BaseDict folder){
        if (folder == null) return;
        doSelectFolder(folder);                
        PrimeFaces.current().ajax().update("westFRM");
        PrimeFaces.current().ajax().update("mainFRM");
    }
        
    private void doSelectFolder(BaseDict folder){
        BaseDict parent = folder.getParent();
        if (parent != null){
            doSelectFolder(parent);
        }
        TreeNode node = EscomBeanUtils.findTreeNode(getTree(), folder);
        onSelectInTree(node);
    }
    
    /**
     * Выгрузка файлов документов текущей папки в архив
     */
    public void onUploadToFile(){
        if (currentItem == null) return;
        if (versionOS == null){
            versionOS = "";          
        }
        Folder folder = (Folder) currentItem;
        String pathZipFile = attacheService.makeFolderZIP(folder, getCurrentUser(), versionOS);
        if (StringUtils.isNoneBlank(pathZipFile)){
            String zipName = folder.getPath()+".zip";
            EscomFileUtils.attacheDownLoad(pathZipFile, zipName);
            File zipFile = new File(pathZipFile);
            zipFile.delete();                       
        } else {
            MsgUtils.errorMsg("FailedGenerateZIP");
        }
    }
    
    /* Обработка drop помещения объекта в дерево */
    protected void doDropToTree(List<BaseDict> dragItems){
        Set<String> errors = new HashSet<>();
        switch (getSource()){
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
                if (errors.isEmpty() && !checkedItems.isEmpty()){
                    onShowMovedDlg("MoveTblDlg");
                }
                break;
            }
            case DictDetailSource.SEARCHE_SOURCE:{ //если источник для detail поиск, то будем добавлять в группу
                checkedItems =
                        dragItems.stream().filter(dragItem ->
                                // если тянем datailItem и бросаем в treeItem
                                (isItemDetailType(dragItem) && isItemTreeType(dropItem) && tableBean.checkRightBeforeAddItemToGroup(dropItem, dragItem, errors))
                                        || // если тянем datailItem и бросаем в treeItem
                                        (isItemDetailType(dragItem) && isItemRootType(dropItem) && tableBean.checkRightBeforeAddItemToGroup(dropItem, dragItem, errors))
                        ).collect(Collectors.toList());
                if (errors.isEmpty() && !checkedItems.isEmpty()){
                    onShowMovedDlg("AddTblDlg");
                }
                break;
            }
        }
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
            PrimeFaces.current().ajax().update("mainFRM:tblDetail");
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

            //определям источник : объект тянется из таблицы 
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
                        case DictExplForm.TAB_FILTER:{
                            doDropToFilter(checkedItems, (Filter) dropItem);
                            break;
                        }
                    }
                }
            }
        }
    }

    /*  Формирование списка объектов для перетаскивания.  В список включается перетаскиваемый объект и уже отмеченные объекты  */
    protected void makeCheckedItemList(BaseDict dragItem){
        if (dragItem == null){
            checkedItems.clear();
            return;
        }
        if (!checkedItems.contains(dragItem)){
            checkedItems.add(dragItem);
        }
    }

    private boolean checkPossibilityMoving(BaseDict dropItem, BaseDict dragItem, Set<String> errors){
        if (isItemDetailType(dropItem)){
            String error = MessageFormat.format(MsgUtils.getMessageLabel("MoveItemNotAvailable"), new Object[]{dragItem.getName(), dropItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }

    /* Обработка события drop в таблицу обозревателя объектов  */
    public void dropToTable(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String dropId = params.get("dropId"); //получаем id приёмника, куда поместили объект
        String dragId = params.get("dragId"); //получаем id источника

        //ищем в таблице запись приёмника
        String rkTbl = dropId.substring(LEH_TABLE_NAME, dropId.length());
        String rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
        if(!rwKey.matches("\\d+")) {
            return;
        }
        Integer tbKey = Integer.parseInt(rwKey);
        tbKey = tbKey - currentPage;
        dropItem = EscomBeanUtils.findUITableContent(loadItems, tbKey);
        if (dropItem != null) {
            //ищем в таблице запись источника
            rkTbl = dragId.substring(LEH_TABLE_NAME, dragId.length());
            rwKey = rkTbl.substring(0, rkTbl.indexOf(":"));
            if(!rwKey.matches("\\d+")) {
               return;
            }
            tbKey = Integer.parseInt(rwKey);
            tbKey = tbKey - currentPage;
            BaseDict dragItem = EscomBeanUtils.findUITableContent(loadItems, tbKey);
            makeCheckedItemList(dragItem);
            if (!checkedItems.isEmpty()) {
                Set<String> errors = new HashSet<>();
                if (checkPossibilityMoving(dropItem, dragItem, errors)){
                    if (isItemDetailType(dragItem) && tableBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                        onShowMovedDlg("MoveTblDlg");
                    } else{
                        dragNode = EscomBeanUtils.findTreeNode(tree, dragItem);
                        dropNode = EscomBeanUtils.findTreeNode(tree, dropItem);
                        if (isItemTreeType(dragItem) && isItemTreeType(dropItem) && treeBean.prepareMoveItemToGroup(dropItem, dragItem, errors)){
                            onShowMovedDlg("MoveTreeDlg");
                        }
                    }
                }
                if (!errors.isEmpty()) {
                    MsgUtils.showErrors(errors);
                }
            }
        } else {
            MsgUtils.errorMsg("ErrUnableDetermineID"); //не удалось определить идентификатор получателя операции
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
            BaseDict flDrag = EscomBeanUtils.findUITableContent(loadItems, tbKey);
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
                MsgUtils.showErrors(errors);
            }
        }
        MsgUtils.errorMsg("ErrUnableDetermineID"); //не удалось определить идентификатор получателя операции
    }

    /* DRAG & DROP добавление объекта в группу */
    public void addItemToGroup(){
        if (!isItemTreeType(dropItem)){ //если бросили в treeItem
            return;
        }
        checkedItems.stream()
                .filter(dragItem -> !isItemRootType(dragItem))
                .forEach(dragItem -> {                    
                    if (((BaseExplBeanGroups)getItemBean(dragItem)).addItemToGroup(dragItem, dropItem)){
                        MsgUtils.succesFormatMsg("AddObjectToGroupComplete", new Object[]{dragItem.getName(), dropItem.getName()});
                    }
                });
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
        refreshLazyData();
    }

    /* DRAG & DROP: отработка команды на перемещение из таблицы в дерево  */
    public void moveItemToGroup(){
        BaseDict dragItem = checkedItems.get(0);         
        if (isItemTreeType(dragItem)){
            treeBean.moveGroupToGroup(dropItem, dragItem);  
            TreeNode drag = EscomBeanUtils.findTreeNode(tree, dragItem);
            TreeNode parentDragNode = drag.getParent();
            if (parentDragNode != null && CollectionUtils.isNotEmpty(parentDragNode.getChildren())){                    
                parentDragNode.getChildren().remove(drag);
            }
            dropItem.setIconTree("ui-icon-folder-collapsed");
            onSelectInTree(dropNode);
        } else {
            tableBean.moveItemToGroup(dropItem, dragItem, treeSelectedNode);
            loadItems.removeAll(checkedItems);
        }
    }
}
