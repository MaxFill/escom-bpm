package com.maxfill.escom.beans.core;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.rights.RightFacade;
import com.maxfill.model.users.UserFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.services.print.PrintService;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.maxfill.escom.utils.MsgUtils.getBandleLabel;
import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.states.State;
import com.maxfill.utils.Tuple;
import java.util.logging.Logger;

/**
 * Базовый бин для работы с табличными представлениями объектов (без владельцев)
 * @param <T> - класс объекта
 */
public abstract class BaseTableBean<T extends BaseDict> extends LazyLoadBean<T>{
    private static final long serialVersionUID = -3085033375091696717L;

    @EJB
    protected PrintService printService;
    @EJB
    protected FavoriteService favoriteService;
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected UserFacade userFacade;
    @EJB
    protected RightFacade rightFacade;

    public abstract BaseTableBean getDetailBean();       //возвращает бин подчинённых объектов

    private Metadates metadatesObj;                     //объект метаданных

    @Override
    public abstract BaseDictFacade getFacade();
    public abstract BaseDetailsBean getOwnerBean();     //возвращает бин владельца объекта
        
    /* Формирование списка детальных данных в таблице обозревателя  */
    public List<T> prepareSetDetails(List<T> sourceItems) {
        return sourceItems.stream()
                    .filter(item -> getFacade().preloadCheckRightView(item, getCurrentUser()))
                    .collect(Collectors.toList());
    }
    
    /* РЕДАКТИРОВАНИЕ/ПРОСМОТР ОБЪЕКТА */     

    /* Подготовка к просмотру объекта на карточке */
    public T prepViewItem(T item, Map<String, List<String>> paramsMap, Set<String> errors){
        BaseDictFacade facade = getFacade();
        T editItem = findItem(item.getId());   //получаем копию объекта для просмотра 
        if (editItem == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{item.getClass().getSimpleName() , item.getId()});
            return null;
        }
        getFacade().makeRightItem(editItem, getCurrentUser());
        if (!facade.isHaveRightView(editItem)){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightViewNo"), new Object[]{objName});
            errors.add(error);
        }
        openItemCard(editItem, DictEditMode.VIEW_MODE, paramsMap, errors);
        return editItem;
    }    
    
    /* Подготовка к редактированию объекта на карточке  */      
    public T prepEditItem(T item){
        return prepEditItem(item, getParamsMap());
    }
    public T prepEditItem(T item, Map<String, List<String>> paramsMap){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getFacade();
        T editItem = findItem(item.getId());  //получаем копию объекта для редактирования 
        if (editItem == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{item.getClass().getSimpleName(), item.getId()});
            return null;
        }
        
        facade.makeRightItem(editItem, getCurrentUser());
        if (!facade.isHaveRightEdit(editItem)){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
            return prepViewItem(item, paramsMap, errors);
        }
        openItemCard(editItem, DictEditMode.EDIT_MODE, paramsMap, errors);
        return editItem;        
    } 
    
    /**
     * Используется для открытия на редактирование дочерних объектов (у них может не быть ID!), которые сохраняются вместе с родительским 
     * @param item
     * @param paramsMap
     * @return 
     */
    public T prepEditChildItem(T item, Map<String, List<String>> paramsMap){        
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getFacade();
        try {            
            T editItem =  createItem(item.getOwner(), getCurrentUser(), new HashMap<>()); //создаём копию объекта
            BeanUtils.copyProperties(editItem, item);
            if (editItem.getRightItem() == null){
                facade.makeRightItem(editItem, getCurrentUser());
            }
            if (!facade.isHaveRightEdit(editItem)){
                String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
                String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
                errors.add(error);
                return prepViewItem(item, paramsMap, errors);
            }
            openItemCard(editItem, DictEditMode.CHILD_MODE, paramsMap, errors);
            return editItem;
        } catch (IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(BaseTableBean.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
        }
    }

    /**
     * СОЗДАНИЕ: Создание объекта с открытием карточки
     * @param parent
     * @param owner
     * @param createParams - параеметры, используемые на стадии создания объекта 
     * @param openParams - параметры, используемые при открытии карточки объекта, например, данные о бине из которого был вызов метода
     * @return 
     */
    public T createItemAndOpenCard(BaseDict parent, BaseDict owner, Map<String, Object> createParams, Map<String, List<String>> openParams){
        Set<String> errors = new HashSet<>();
        T newItem = checkCanCreateItem(parent, owner, errors, createParams);
        openItemCard(newItem, DictEditMode.INSERT_MODE, openParams, errors);
        return newItem;
    }

    /* СОЗДАНИЕ: Cоздание нового объекта   */
    public T createItem(BaseDict owner) {
        return createItem(owner, getCurrentUser(), new HashMap<>());
    }

    /* СОЗДАНИЕ: Cоздание объекта */
    public T createItem(BaseDict owner, User author, Map<String, Object> params) {
        return (T) getFacade().createItem(author, owner, params);
    }

    /**
     * Проверка возможности создания объекта
     * для того чтобы узнать, может ли пользователь создать объект нужно его создать, получить для него права и только затем проверить
     * @param parent
     * @param owner
     * @param errors Метод вернёт непустой errors если у пользователя нет права на создание
     * @param params
     * @return
     */
    public T checkCanCreateItem(BaseDict parent, BaseDict owner, Set<String> errors, Map<String, Object> params){
        T newItem = createItem(owner, getCurrentUser(), params);
        prepCreate(newItem, parent, errors);
        return newItem;
    }
    
    /**
     * Открытие карточки объекта в заданном режиме редактирования
     * @param item - открываемый объект
     * @param editMode - режим открытия DictEditMode
     * @param paramsMap - передаваемые в открываемый бин параметры
     * @param errors - список ошибок
     */
    public void openItemCard(BaseDict item, Integer editMode, Map<String, List<String>> paramsMap, Set<String> errors){       
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return;
        }
        
        String itemKey = item.getItemKey();
        
        if (editMode.equals(DictEditMode.EDIT_MODE)){
            User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект        
            if (user != null){
                String objName = user.getName();
                MsgUtils.errorFormatMsg("ObjectAlreadyOpened", new Object[]{objName});
                return;
            }
        }

        String itemOpenKey = appBean.addLockedItem(itemKey, editMode, item, getCurrentUser());
        List<String> itemKeyList = new ArrayList<>();
        itemKeyList.add(itemOpenKey);
        paramsMap.put("itemId", itemKeyList);          
        
        String formName = getFormName();
        Tuple<Integer, Integer> size = sessionBean.getFormSize(formName);
        
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("minWidth", 450);
        options.put("minHeight", 300);
        options.put("width", size.a);
        options.put("height", size.b);
        options.put("maximizable", false);
        options.put("minimizable", false);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");                           

        PrimeFaces.current().dialog().openDynamic(formName + "-card", options, paramsMap);
    }  
    
    /* Действия перед созданием объекта */
    protected void prepCreate(T newItem, BaseDict parent, Set<String> errors){
        getFacade().makeRightItem(newItem, getCurrentUser());
        if (getFacade().isHaveRightCreate(newItem)) {
            setSpecAtrForNewItem(newItem);
        } else {
            String objName = MsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName());
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
            errors.add(error);
        }
    }
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    public void setSpecAtrForNewItem(T item) {}
    
    /* Вставка объекта !!!*/
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){       
        T pasteItem = doCopy(sourceItem);
        preparePasteItem(pasteItem, sourceItem, recipient);
        prepCreate(pasteItem, null, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return null;
        }        
        changeNamePasteItem(sourceItem, pasteItem);
        getFacade().create(pasteItem);
        doPasteMakeSpecActions(sourceItem, pasteItem);
        return pasteItem;
    }
    
    protected void doPasteMakeSpecActions(T sourceItem, T pasteItem){        
    }
    
    /* Специфичные действия перед вставкой скопированного объекта */
    public void preparePasteItem(T pasteItem, T sourceItem, BaseDict recipient){
        pasteItem.setChildItems(null);
        pasteItem.setDetailItems(null);
        pasteItem.setState(null);
        pasteItem.setId(null);                    //нужно сбросить скопированный id!
        pasteItem.setItemLogs(new ArrayList<>()); //нужно сбросить скопированный log !
        pasteItem.doSetSingleRole(DictRoles.ROLE_OWNER, getCurrentUser());
        getFacade().doSetState(pasteItem, getMetadatesObj().getStateForNewObj());
    };  
    
    /**
     * Определяет, нужно ли копировать объект при вставке
     * Некоторые объекты при вставке не нужно копировать!
     * @param item
     * @param recipient
     * @return 
     */
    public boolean isNeedCopyOnPaste(T item, BaseDict recipient){
        return true;
    }
    
    /* Возвращает объект по его Id */
    public T findItem(Integer id){
        return (T) getFacade().find(id);
    }
    
    /* Возвращает список актуальных объектов, доступных для просмотра текущему пользователю */
    public List<T> findAll(){        
        return (List<T>) getFacade().findAll().stream()
                    .filter(item -> getFacade().preloadCheckRightView((T)item, getCurrentUser()))
                    .collect(Collectors.toList());
    }
    
    /**
     * Формирует список дочерних объектов, доступных текущему пользователю
     * @param owner
     * @return 
     */
    public List<T> findDetailItems(BaseDict owner){
        return (List<T>) getFacade().findActualDetailItems(owner).stream()
                .filter(item -> getFacade().preloadCheckRightView((T)item, getCurrentUser()))
                .collect(Collectors.toList());
    }
    
    /* Копирование объекта */
    public T doCopy(T sourceItem){
        T newItem = createItem(null);
        try {
            BeanUtils.copyProperties(newItem, sourceItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return newItem;
    }
    
    /* Изменение имени вставляемого объекта */
    protected void changeNamePasteItem(BaseDict sourceItem, BaseDict pasteItem){
        String name = getBandleLabel("CopyItem") + " " + pasteItem.getName();
        pasteItem.setName(name);        
    }

    /**
     * Возвращает списки зависимых объектов, необходимых для копирования при вставке объекта, кроме удалённых в корзину!
     * @param item
     * @return
     */
    public List<List<?>> doGetDependency(T item){
        return null;
    }              

    protected void checkLockItem(T item, Set<String> errors){
        String itemKey = item.getItemKey();
        User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект        
        if (user != null){
            Object[] messageParameters = new Object[]{item.getName(), user.getName()};
            String error = MessageFormat.format(getMessageLabel("ObjectIsLockUser"), messageParameters);
            errors.add(error);
        }
    }

    protected void actualizeRightForDropItem(BaseDict dropItem){
        getFacade().actualizeRightItem(dropItem, getCurrentUser());
    }

    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в корзину  */
    public boolean prepareDropItemToTrash(T dragItem, Set<String> errors) {
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightDelete(dragItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightDeleteNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в не актуальные  */
    public boolean prepareDropItemToNotActual(T dragItem, Set<String> errors){
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightEditNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;        
    }

    /* Обработка перемещения объекта в не актульные  */
    public void moveItemToNotActual(T item){
        item.setActual(false);
        getFacade().edit(item);
    }            
    
    /* Перед перемещением объекта в группу  */
    public boolean prepareMoveItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)){
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        
        actualizeRightForDropItem(dropItem);

        if (!getFacade().isHaveRightAddChild(dropItem)){
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedAddChilds"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* Проверка прав перед добавлением объекта в группу  */
    public boolean checkRightBeforeAddItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {        
        getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        if (!getFacade().isHaveRightAddChild(dropItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* Обработка перемещения объекта в группу при drag & drop*/
    public void moveItemToGroup(BaseDict dropItem, T dragItem, TreeNode sourceNode) {
        BaseDict ownerDragItem = dragItem.getOwner();    
        if (ownerDragItem != null) { //только если owner был, то его можно поменять на новый!             
            dragItem.setOwner(dropItem);
            getFacade().edit(dragItem);
        }
    }
    
    /* УДАЛЕНИЕ  */
    
    /* Удаление выполняется сначала только в корзину! На формах кнопки только помещения в корзину!
     * Потом можно удалить из корзины или очистить корзину целиком, но для этого уже нужно быть Администратором! 
     * При помещении объекта в корзину проверяются его зависимости и при их наличии выдаётся ошибка.
     * При удалении объекта из корзины проверок не выполняется */
        
    /* УДАЛЕНИЕ: Проверка возможности удаления объекта. Переопределяется в бинах  */
    protected void checkAllowedDeleteItem(T item, Set<String> errors) {
    }    
    
    /* УДАЛЕНИЕ: удаление объекта вместе с дочерними и подчинёнными  */
    public void deleteItem(T item) {
        deleteChilds(item);
        deleteDetails(item);
        preDeleteItem(item);
        numeratorService.doRollBackRegistred(item);
        getFacade().remove(item);
    }

    /* УДАЛЕНИЕ: удаление дочерних child объектов */
    private void deleteChilds(T parent) {
        getFacade().findAllChilds(parent).stream().forEach(child -> deleteItem((T) child));
    }

    /* Удаление подчинённых (связанных) объектов */
    protected void deleteDetails(T item) {
    }

    /* УДАЛЕНИЕ: Выполнение специфичных действий перед удалением объекта  */
    protected void preDeleteItem(T item) {
    } 

    /* КОРЗИНА */
    
    /* КОРЗИНА: восстановление объекта из корзины */
    public void doRestoreItemFromTrash(T item) {
        restoreDetails(item);
        restoreChilds(item);
        item.setDeleted(Boolean.FALSE);
        getFacade().edit(item);
    }

    /* Восстановление подчинённых detail объектов из корзины */
    protected void restoreDetails(T ownerItem) {      
    }

    /* КОРЗИНА: восстановление дочерних childs объектов из корзины */
    private void restoreChilds(T parentItem) {
        getFacade().findAllChilds(parentItem).stream().forEach(item -> doRestoreItemFromTrash((T) item));
    }
    
    /* КОРЗНА: перемещение дочерних элементов в корзину */
    private void moveChildItemToTrash(BaseDict parent, Set<String> errors) {
        getFacade().findAllChilds(parent).stream().forEach(child -> moveToTrash((T) child, errors));
    }

    /* КОРЗИНА: перемещение в корзину подчинённых объектов Владельца (ownerItem) */
    protected void moveDetailItemsToTrash(T item, Set<String> errors) {        
    }

    /* КОРЗИНА: перемещение объекта в корзину */
    public void moveToTrash(T item, Set<String> errors) {
        getFacade().actualizeRightItem(item, getCurrentUser());
        if (getFacade().isHaveRightDelete(item)) {
            checkAllowedDeleteItem(item, errors);
            checkLockItem(item, errors);
            if (errors.isEmpty()) {
                Set<String> errDetail = new HashSet<>();
                moveDetailItemsToTrash(item, errDetail);
                Set<String> errChild = new HashSet<>();
                moveChildItemToTrash(item, errChild);
                if (errChild.isEmpty() && errDetail.isEmpty()) {
                    item.setDeleted(Boolean.TRUE);
                    getFacade().edit(item);
                } else {
                    errors.addAll(errDetail);
                    errors.addAll(errChild);
                }
            }
        } else {
            String metadateName = MsgUtils.getBandleLabel(getMetadatesObj().getBundleName());
            Object[] msgParams = new Object[]{metadateName, item.getName()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightDeleteNo"), msgParams);
            errors.add(error);
        }
    } 

/* *** CЕЛЕКТОР ОБЪЕКТОВ *** */
    
    /* CЕЛЕКТОР: Открытие формы селектора для выбора единичного объекта */
    public void onOneSelectItem() {        
        openItemSelector(DictExplForm.SING_SELECT_MODE, getParamsMap());
    }

    /* СЕЛЕКТОР: открытие формы для выбора нескольких объектов */
    public void onManySelectItem() {
        openItemSelector(DictExplForm.MULTY_SELECT_MODE, getParamsMap());
    }

    /* СЕЛЕКТОР: открытие формы выбора */
    private void openItemSelector(Integer selectMode, Map<String, List<String>> paramsMap) {                
        List<String> paramList = Collections.singletonList(selectMode.toString());
        paramsMap.put("selectMode", paramList);                
        String frmName = getFacade().getFRM_NAME() + "-explorer";        
        sessionBean.openDialogFrm(frmName, paramsMap);
    }        
    
    /* ИЗБРАННОЕ */
    
    /* ИЗБРАННОЕ: отбор избранных записей для текущего пользователя     */
    public List<T> findFavorites() {        
        List<Integer> favorIds = new ArrayList<>();
        List<FavoriteObj> favorites = getCurrentUser().getFavoriteObjList();
        favorites.stream()
                .filter(item -> (item.getMetadateObj().getId().equals(getMetadatesObj().getId())))
                .forEach(item -> favorIds.add(item.getObjId()));
        return getFacade().findByIds(favorIds);
    }   

    /* ИЗБРАННОЕ: удаление объекта из избранного   */
    public void delFromFavorites(BaseDict item) {
        favoriteService.delFromFavorites(item, getMetadatesObj(), getCurrentUser());
    }

    /**
     * Добавление объекта в избранное
     * @param item
     */
    public void addInFavorites(BaseDict item){
        sessionBean.addInFavorites(item, getMetadatesObj());
    }

    /*  ФИЛЬТРЫ */
    
    /* ФИЛЬТРЫ: формирование списка результатов для выбранного фильтра  */
    public List<T> makeFilteredContent(Filter filter) {
        List<T> result = new ArrayList<>();
        switch (filter.getId()) {
            case DictFilters.TRASH_ID: {
                result = getFacade().loadFromTrash();
                break;
            }           
            case DictFilters.FAVORITE_ID: {
                result = findFavorites();
                break;
            }            
            case DictFilters.USER_CREATED_ID: {
                result = getFacade().findItemsCreatedByUser(getCurrentUser());
                break;
            }
            case DictFilters.LAST_CHANGE_ID: {
                result = getFacade().findLastChangedItemsByUser(getCurrentUser());
                break;
            }
            case DictFilters.NOTACTUAL_ID: {
                result = getFacade().loadNotActualItems();
                break;
            }
            case DictFilters.ON_MY_EDIT: {
                result = getFacade().loadLockDocuments(getCurrentUser());
                break;
            }
        }
        return prepareSetDetails(result);
    }
    
    /* ПОИСК: Выполняет инициализацию модели данных поиска */
    public SearcheModel initSearcheModel(){
        return new SearcheModel();
    }
    
    /* ПОИСК: Выполняет поиск объектов */
    public List<T> doSearche(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){
        List<T> sourceItems = getFacade().getByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, addParams);
        return prepareSetDetails(sourceItems);
    }
    
    /* СЛУЖЕБНЫЕ МЕТОДЫ  */

    /**
     * Определяет доступность кнопки "Создать" на панели обозревателя
     * Если метод возвращает true, то кнопка недоступна - disable(true)
     * @param treeSelectedNode
     * @return
     */
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return treeSelectedNode != null;
    }

    /**
     * Замена одного объекта на другой
     * @param oldItem
     * @param newItem
     * @return 
     */
    public int replaceItem(BaseDict oldItem, BaseDict newItem){
        return getFacade().replaceItem(oldItem, newItem);
    }        
   
    /* АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: вычисление числа ссылок на объект. */
    public void doGetCountUsesItem(T item, Map<String, Integer> rezult) {
        rezult.put("CheckObjectNotSet", 0);
    }

    @Override
    public String getFormName() {
        return getFacade().getFRM_NAME();
    }

    @Override
    public String getFormHeader() {
        return getMetadatesObj().getBundleJurnalName();
    }
    
    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getFacade().getMetadatesObj();
        }
        return metadatesObj;
    }    
    
    /**
     * Возвращает список состояний объекта
     * @return 
     */
    public List<State> getAvailableStates(){        
        Metadates metaObj = getMetadatesObj();
        return metaObj.getStatesList();                
    }
}