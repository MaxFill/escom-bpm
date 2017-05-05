package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictDetailSource;
import com.maxfill.dictionary.DictRights;
import com.maxfill.utils.SysParams;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.escom.beans.explorer.ExplorerBean;
import com.maxfill.model.filters.Filters;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.escom.utils.EscomBeanUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.TreeNode;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Базовый bean для справочников
 *
 * @author mfilatov
 * @param <T> класс объекта
 * @param <O> класс владельца
 */
public abstract class BaseExplBean<T extends BaseDict, O extends BaseDict> extends BaseBean<T> {
    private static final long serialVersionUID = -4409411219233607045L;       
    
    private BaseDataModel model;
    private Integer defaultMaskAccess;          //дефолтная маска доступа текущего пользователя          

    private TreeNode selectedNode;              //текущий элемент в дереве

    private final LayoutOptions layoutOptions = new LayoutOptions();
    
    protected Boolean northVisible = true;
    protected Boolean southVisible = true;
    protected Boolean westVisible = true;
    protected Boolean eastVisible = true;
    protected Integer widthCardDlg = 800;
    protected Integer heightCardDlg = 600;

    protected ExplorerBean explorerBean;  
    
    private TreeNode[] selectedNodes;
    
    /* *** ИНИЦИАЛИЗАЦИЯ *** */   

    public abstract List<O> getGroups(T item);          //возвращает список групп объекта   

    protected abstract String getBeanName();

    public abstract BaseExplBean getDetailBean();
    public abstract BaseExplBean getOwnerBean();      //установка бина владельца объекта 


    /**
     * ИНИЦИАЛИЗАЦИЯ: действия при создании бина
     */
    @Override
    public void onInitBean(){        
        model = createModel();
        EscomBeanUtils.initLayoutOptions(layoutOptions);
        initAddLayoutOptions(layoutOptions);
        northVisible = false;
    }
            
    /**
     * ИНИЦИАЛИЗАЦИЯ: создание модели данных бина
     *
     * @return
     */
    protected BaseDataModel createModel() {
        return new BaseDataModel();
    }

    /* *** ПРАВА ДОСТУПА ***  */
    
    /**
     * ИНФО: Объекты, имеющие владельца (owner), при включенном наследовании,
     * получают права от владельца. Например, контрагент, получает права от
     * своей группы. Поскольку контрагент может быть в разных группах, то в
     * каждой группе к одному и тому же контрагенту могут быть разные права!
     */

    /**
     * ПРАВА ДОСТУПА: можно ли создавать объект
     * @return 
     */
    public boolean isHaveRightCreate() {
        boolean flag = true;
        if (explorerBean.getTreeSelectedNode() != null) {
            O ownerItem = (O) explorerBean.getTreeSelectedNode().getData();
            if (ownerItem != null) {
                flag = getItemFacade().isHaveRightEdit(ownerItem); //можно ли редактировать owner?
            } else {
                flag = false;
            }
        }
        if (flag) {
            flag = getItemFacade().checkMaskAccess(getDefaultMaskAccess(), DictRights.RIGHT_CREATE); //можно ли создавать объект данного типа
        }
        return flag;
    }
    
    /* ПРАВА ДОСТУПА: возвращает дефолтную маску доступа к объекту для текущего пользователя  */
    public Integer getDefaultMaskAccess() {
        if (defaultMaskAccess == null) {
            Rights rights = getItemFacade().getDefaultRights();
            State state = getMetadatesObj().getStateForNewObj();
            defaultMaskAccess = getItemFacade().getAccessMask(state, rights, currentUser);
        }
        return defaultMaskAccess;
    }     
    
    /**
     * КАРТОЧКА: выполнение специфичных действий после закрытия карточки
     */ 
    public void afterCloseItemCard(){
    }
        
    /* *** РЕДАКТИРОВАНИЕ/ПРОСМОТР ОБЪЕКТА *** */
        

    /**
     * РЕДАКТИРОВАНИЕ: перед добавлением объекта в группу
     * @param dropItem (owner)
     * @param dragItem
     * @param errors
     * @return 
     */
    public boolean prepareAddItemToGroup(O dropItem, T dragItem, Set<String> errors) {        
        getOwnerBean().actualizeRightItem(dropItem);
        if (!getItemFacade().isHaveRightEdit(dropItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()}); 
            errors.add(error);
            return false;
        }
        actualizeRightItem(dragItem);
        if (!getItemFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()}); 
            errors.add(error);
            return false;
        }
        return true;
    }

    /**
     * РЕДАКТИРОВАНИЕ: Перед перемещением объекта в группу
     *
     * @param dropItem (owner)
     * @param dragItem
     * @param errors
     * @return
     */
    public boolean prepareMoveItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {
        actualizeRightItem(dropItem);

        if (!getItemFacade().isHaveRightEdit(dropItem)){
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        actualizeRightItem(dragItem);
        if (!getItemFacade().isHaveRightEdit(dragItem)){
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }

    /**
     * РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в корзину 
     *     
     * @param dragItem
     * @param errors
     * @return 
     */
    public boolean prepareDropItemToTrash(T dragItem, Set<String> errors) {        
        actualizeRightItem(dragItem);
        if (!isHaveRightDelete(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightDeleteNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /**
     * РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в
     * @param errors
     * @return не актуальные
     *     
     * @param dragItem
     */
    public boolean prepareDropItemToNotActual(T dragItem, Set<String> errors){
        actualizeRightItem(dragItem);
        if (!getItemFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightEditNo"), new Object[]{dragItem.getName()}); 
            errors.add(error);
            return false;
        }
        return true;        
    }
    
    /**
     * РЕДАКТИРОВАНИЕ: Обработка события перемещения в дереве группы в группу
     * Выполняется в бине дерева
     * @param dropItem
     * @param dragItem
     */
    public void moveGroupToGroup(BaseDict dropItem, T dragItem) {
        dragItem.setParent(dropItem);
        getItemFacade().edit(dragItem);
    }

    /**
     * РЕДАКТИРОВАНИЕ: Обработка перемещения объекта в группу Выполняется в бине
     * таблицы
     * @param dropItem
     * @param dragItem
     * @param sourceNode
     */
    public void moveItemToGroup(BaseDict dropItem, T dragItem, TreeNode sourceNode) {
        O ownerDragItem = (O) dragItem.getOwner();    
        if (ownerDragItem != null) { //только если owner был, то его можно поменять на новый!             
            dragItem.setOwner(dropItem);
            getItemFacade().edit(dragItem);
        }
    }

    /**
     * РЕДАКТИРОВАНИЕ: обработка перемещения объекта в не актульные
     * @param item 
     */
    public void moveItemToNotActual(T item){
        item.setActual(false);
        getItemFacade().edit(item);
    }            
    
    /* *** УДАЛЕНИЕ ***  */
    
    /* Инфо: Удаление выполняется сначала только в корзину! На формах кнопки только помещения в корзину!
       Потом можно удалить из корзины или очистить корзину целиком, но для этого уже нужно быть Администратором! 
       При помещении объекта в корзину проверяются его зависимости и при их наличии выдаётся ошибка.
       При удалении объекта из корзины проверок не выполняется */
        
    /**
     * Проверка возможности удаления объекта. Переопределяется в бинах
     *
     * @param item
     * @param errors
     */
    protected void checkAllowedDeleteItem(T item, Set<String> errors) {
        EscomBeanUtils.checkAllowedDeleteItem(item, errors);
    }
    
    /**
     * Удаление из владельца всех связанных detail объектов
     * @param owner 
     */
    protected void clearOwner(O owner){
        owner.getDetailItems().clear();
    }
    
    /**
     * УДАЛЕНИЕ: удаление объекта вместе с дочерними и подчинёнными
     *
     * @param item
     */
    public void deleteItem(T item) {
        deleteChilds(item);
        deleteDetails(item);
        preDeleteItem(item);
        numeratorService.doRollBackRegistred(item, getItemFacade().getFRM_NAME());
        getItemFacade().remove(item);
        postDeleteItem(item);
    }

    /**
     * УДАЛЕНИЕ: удаление дочерних child объектов
     */
    private void deleteChilds(T parent) {
        if (parent.getChildItems() != null){
            parent.getChildItems().stream().forEach(child -> deleteItem((T) child));
        }
    }

    /**
     * УДАЛЕНИЕ: удаление подчинённых (связанных) объектов
     */
    private void deleteDetails(T item) {
        if (item.getDetailItems() != null) {
            item.getDetailItems().stream().forEach(child -> getDetailBean().deleteItem((T) child));
        }
    }

    /**
     * УДАЛЕНИЕ: Переопределяется для выполнения специфичных действий перед
     * удалением объекта
     *
     * @param item
     */
    protected void preDeleteItem(T item) {
    }

    /**
     * УДАЛЕНИЕ: Переопределяется для выполнения специфичных действий после удаления объекта
     */
    protected void postDeleteItem(T item) {
    }   

    /* *** КОРЗИНА *** */
    
    /**
     * КОРЗИНА: восстановление объекта из корзины
     *
     * @param item
     */
    public void doRestoreItemFromTrash(T item) {
        restoreDetails(item);
        restoreChilds(item);
        item.setDeleted(Boolean.FALSE);
        getItemFacade().edit(item);
    }

    /**
     * КОРЗИНА: восстановление подчинённых detail объектов из корзины
     * @param ownerItem
     */
    private void restoreDetails(T ownerItem) {
        if (ownerItem.getDetailItems() != null){
            ownerItem.getDetailItems().stream()
                    .forEach(item -> getDetailBean().doRestoreItemFromTrash((T) item)
            );
        }
    }

    /**
     * КОРЗИНА: восстановление дочерних childs объектов из корзины
     *  @param parentItem
     */
    private void restoreChilds(T parentItem) {
        parentItem.getChildItems().stream().forEach(item -> doRestoreItemFromTrash((T) item));
    }
    
    /**
     * КОРЗНА: перемещение дочерних элементов в корзину
     */
    private void moveChildItemToTrash(BaseDict parent, Set<String> errors) {
        if (parent.getChildItems() != null){
            parent.getChildItems().stream().forEach(child -> moveToTrash((T) child, errors));
        }
    }

    /**
     * КОРЗИНА: перемещение в корзину подчинённых объектов Владельца (ownerItem)
     */
    private void moveDetailItemsToTrash(BaseDict ownerItem, Set<String> errors) {        
        if (ownerItem.getDetailItems() != null){
            ownerItem.getDetailItems().stream()
                    .forEach(detail -> getDetailBean().moveToTrash((T) detail, errors)
            );
        }
    }

    /**
     * КОРЗИНА: перемещение объекта в корзину
     *
     * @param item
     * @param errors
     */
    public void moveToTrash(BaseDict item, Set<String> errors) {
        actualizeRightItem(item);
        if (isHaveRightDelete(item)) {
            checkAllowedDeleteItem((T)item, errors);
            if (errors.isEmpty()) {
                Set<String> errDetail = new HashSet<>();
                moveDetailItemsToTrash(item, errDetail);
                Set<String> errChild = new HashSet<>();
                moveChildItemToTrash(item, errChild);
                if (errChild.isEmpty() && errDetail.isEmpty()) {
                    item.setDeleted(Boolean.TRUE);
                    getItemFacade().edit(item);                    
                } else {
                    errors.addAll(errDetail);
                    errors.addAll(errChild);
                }
            }
        } else {
            Object[] msgParams = new Object[]{item.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getBandleLabel("RightDeleteNo"), msgParams);
            errors.add(error);
        }
    } 

    /* *** CЕЛЕКТОР ОБЪЕКТОВ *** */
    
    /**
     * CЕЛЕКТОР: Открытие формы селектора для выбора единичного объекта
     */
    public void onOneSelectItem() {
        openItemSelector(DictExplForm.SING_SELECT_MODE);
    }

    /**
     * СЕЛЕКТОР: открытие формы для выбора нескольких объектов
     */
    public void onManySelectItem() {
        openItemSelector(DictExplForm.MULTY_SELECT_MODE);
    }

    /**
     * СЕЛЕКТОР: открытие формы выбора
     */
    private void openItemSelector(Integer selectMode) {
        Map<String, Object> options = new HashMap<>();
        //options.put("headerElement", "explorer_south");
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 1350);
        options.put("height", 700);
        options.put("maximizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        Map<String, List<String>> paramMap = new HashMap<>();     
        List<String> paramList = new ArrayList<>();
        paramList.add(selectMode.toString());
        paramMap.put("selectMode", paramList);
        String frmName = getItemFacade().getFRM_NAME() + "-explorer";
        RequestContext.getCurrentInstance().openDialog(frmName, options, paramMap);
    }
        
    /**
     * CЕЛЕКТОР: выбор объекта по двойному клику
     *
     * @param event
     */
    public void onRowDblClckSelector(SelectEvent event) {
        T item = (T) event.getObject();
        onSelect(item);
    }
    
    /**
     * СЕЛЕКТОР: обработка действия в селекторе по нажатию кнопки единичного
     * выбора
     *
     * @param item
     * @return 
     */
    public String onSelect(BaseDict item) {
        if (item == null){return "";}
        explorerBean.getCheckedItems().clear();
        explorerBean.getCheckedItems().add(item);        
        return doClose(explorerBean.getCheckedItems());
    }
    
    /**
     * СЕЛЕКТОР: закрытие селектора без выбора объектов
     * @return 
     */
    public String onClose() {
        explorerBean.getCheckedItems().clear();
        return doClose(explorerBean.getCheckedItems());
    }
    
    /**
     * СЕЛЕКТОР: действие по нажатию кнопки множественного выбора для списка
     * @return 
     */
    public String onMultySelect() {
        return doClose(explorerBean.getCheckedItems());
    }        
    
    /**
     * СЕЛЕКТОР: закрытие формы селектора
     * @param selected
     * @return 
     */
    protected String doClose(List<BaseDict> selected){
        RequestContext.getCurrentInstance().closeDialog(selected);
        return "/view/index?faces-redirect=true";
    }
    
    /* *** СПИСКИ ОБЪЕКТОВ *** */

    /**
     * СПИСКИ ОБЪЕКТОВ: Установка списка детальных данных (таблица обозревателя)
     * @param sourceItems 
     * @param source 
     */
    public void doSetDetails(List<BaseDict> sourceItems, int source) {        
        List<BaseDict> details = sourceItems.stream()
                    .filter(item -> sessionBean.preloadCheckRightView(item))
                    .collect(Collectors.toList());
        explorerBean.setDetails(details, source);
    }

    /*  *** ФИЛЬТРЫ *** */
    
    /**
     * ФИЛЬТРЫ: формирование списка результатов для выбранного фильтра
     *
     * @param filter
     */
    public void makeFilteredContent(Filters filter) {
        List<BaseDict> result = new ArrayList<>();
        switch (filter.getId()) {
            case DictFilters.TRASH_ID: {
                result = getItemFacade().loadFromTrash();
                break;
            }           
            case DictFilters.FAVORITE_ID: {
                result = findFavorites();
                break;
            }            
            case DictFilters.USER_CREATED_ID: {
                result = getItemFacade().findItemsCreatedByUser(currentUser);
                break;
            }
            case DictFilters.LAST_CHANGE_ID: {
                result = getItemFacade().findLastChangedItemsByUser(currentUser);
                break;
            }
            case DictFilters.NOTACTUAL_ID: {
                result = getItemFacade().loadNotActualItems();
                break;
            }            
        }
        doSetDetails(result, DictDetailSource.FILTER_SOURCE);
    }   
    
    /* *** СЛУЖЕБНЫЕ МЕТОДЫ *** */
    
    /**
     * Инициализация дополнительных областей формы обозревателя
     *
     * @param layoutOptions
     */
    protected void initAddLayoutOptions(LayoutOptions layoutOptions) {
        EscomBeanUtils.initAddLayoutOptions(layoutOptions);
    }

    /**
     * Возвращает ширину диалога карточки объекта
     *
     * @return
     */
    public Integer getWidthCardDlg() {
        return widthCardDlg;
    } 
    
    /* *** АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ *** */
   
    /**
     * АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: вычисление числа ссылок на объект. Переопределяется
     * @param item
     * @param rezult
     */
    public void doGetCountUsesItem(T item, Map<String, Integer> rezult) {
        rezult.put("CheckObjectNotSet", 0);
    }

    /* *** ГЕТТЕРЫ и СЕТТЕРЫ ***  */

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }

    public Boolean getNorthVisible() {
        return northVisible;
    }
    public Boolean getWestVisible() {
        return westVisible;
    }
    public Boolean getEastVisible() {
        return eastVisible;
    }
    public Boolean getSouthVisible() {
        return southVisible;
    }

    public BaseDataModel getModel() {
        return model;
    }

    public ExplorerBean getExplorerBean() {
        return explorerBean;
    }
    public void setExplorerBean(ExplorerBean explorerBean) {
        this.explorerBean = explorerBean;
    }

    public Integer getHeightCardDlg() {
        return heightCardDlg;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }
    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }
    
    public abstract Class<T> getItemClass();
    public abstract Class<O> getOwnerClass();
        
    /* *** ИЗБРАННОЕ *** */
    
    /**
     * ИЗБРАННОЕ: отбор избранных записей для текущего пользователя
     *
     * @return
     */
    public List<BaseDict> findFavorites() {        
        List<Integer> favorIds = new ArrayList<>();
        List<FavoriteObj> favorites = currentUser.getFavoriteObjList();
        favorites.stream()
                .filter(item -> (item.getMetadateObj().getId().equals(getMetadatesObj().getId())))
                .forEach(item -> favorIds.add(item.getObjId()));
        return getItemFacade().findByIds(favorIds);
    }   

    /**
     * ИЗБРАННОЕ: удаление объекта из избранного
     *
     * @param item
     */
    public void delFromFavorites(BaseDict item) {
        favoriteService.delFromFavorites(item, getMetadatesObj(), currentUser);
    }

    /* *** ПОИСК ***  */   

    /**
     * ПОИСК:
     */
    public void dateCreateStartChange() {
        Date dateCreateEnd = getModel().getDateCreateEnd();
        if (dateCreateEnd == null) {
            getModel().setDateCreateEnd(getModel().getDateCreateStart());
        }
    }

    /**
     * ПОИСК:
     */
    public void dateChangeStartChange() {
        Date dateChangeEnd = getModel().getDateChangeEnd();
        if (dateChangeEnd == null) {
            getModel().setDateChangeEnd(getModel().getDateChangeStart());
        }
    }

    /**
     * ПОИСК: формирует список объектов с учётом критериев поиска
     *
     */
    public void doSearcheItems() {
        List<BaseDict> searcheItems = new ArrayList<>();
        List<O> searcheGroups = new ArrayList<>();
        Map<String, Object> paramEQ = new HashMap<>();
        Map<String, Object> paramLIKE = new HashMap<>();
        Map<String, Object> paramIN = new HashMap<>();
        Map<String, Date[]> paramDATE = new HashMap<>();

        //готовим группы в которых будет поиск
        if (getModel().isSearcheInGroups()) {
            TreeNode ownerNode = explorerBean.getTreeSelectedNode();
            if (ownerNode != null) {
                O owner = (O) ownerNode.getData();
                searcheGroups.addAll(owner.getChildItems());
                if (!searcheGroups.contains(owner)) {
                    searcheGroups.add(owner);
                }
            }
        }

        //добавление в запрос точных критериев
        if (getModel().isOnlyActualItem()) {
            paramEQ.put("actual", true);
        }
        if (getModel().getAuthorSearche() != null) {
            paramEQ.put("author", getModel().getAuthorSearche());
        }
                
        //добавление в запрос не точных критериев
        String name = getModel().getNameSearche().trim();
        if (name.equals("*")) {
            name = "%";
        } else {
            name = name + "%";
        }
        if (StringUtils.isNotEmpty(name) && !SysParams.ALL.equals(name.trim())) {
            paramLIKE.put("name", name);
        }

        //добавление в запрос критериев на вхождение
        List<Integer> states = getModel().getStateSearche();
        if (states != null && !states.isEmpty()) {
            paramIN.put("state", states);
        }

        //добавление в запрос даты создания
        if (getModel().isDateCreateSearche()) {
            Date[] dateArray = new Date[2];
            dateArray[0] = getModel().getDateCreateStart();
            dateArray[1] = getModel().getDateCreateEnd();
            paramDATE.put("dateCreate", dateArray);
        }

        //добавление в запрос даты изменения
        if (getModel().isDateChangeSearche()) {
            Date[] dateArray = new Date[2];
            dateArray[0] = getModel().getDateChangeStart();
            dateArray[1] = getModel().getDateChangeEnd();
            paramDATE.put("dateChange", dateArray);
        }

        List<BaseDict> sourceItems = getItemFacade().getByParameters(paramEQ, paramLIKE, paramIN, paramDATE, getModel());
        
        if (!getModel().isSearcheInGroups()){            
            doSetDetails(sourceItems, DictDetailSource.SEARCHE_SOURCE);
        } else {            
            for (BaseDict item : sourceItems) {
                boolean include = false;

                List<O> itemGroups = getGroups((T)item);
                if (!searcheGroups.isEmpty() && itemGroups != null) {
                    for (O group : searcheGroups) {
                        if (itemGroups.contains(group)) {
                            include = true;
                            break;
                        }
                    }                    
                } else {
                    include = true;
                }

                if (include){
                    searcheItems.add(item);
                }
            }
            doSetDetails(searcheItems, DictDetailSource.SEARCHE_SOURCE);
        }
        explorerBean.setCurrentViewModeDetail();
        explorerBean.makeJurnalHeader(EscomBeanUtils.getBandleLabel(getMetadatesObj().getBundleJurnalName()), EscomBeanUtils.getBandleLabel("SearcheResult"));
    }
   
}