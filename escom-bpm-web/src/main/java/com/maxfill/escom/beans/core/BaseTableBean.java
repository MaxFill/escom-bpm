package com.maxfill.escom.beans.core;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.core.rights.RightFacade;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.filter.Filter;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.basedict.user.User;
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
import com.maxfill.model.core.states.State;
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
    public abstract BaseDictFacade getLazyFacade();
    public abstract BaseDetailsBean getOwnerBean();     //возвращает бин владельца объекта           
    
    /* РЕДАКТИРОВАНИЕ/ПРОСМОТР ОБЪЕКТА */     

    /* Подготовка к просмотру объекта на карточке */
    public T prepViewItem(T item, Map<String, List<String>> paramsMap, Set<String> errors){
        BaseDictFacade facade = getLazyFacade();
        T editItem = findItem(item.getId());   //получаем копию объекта для просмотра 
        if (editItem == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{item.getClass().getSimpleName() , item.getId()});
            return null;
        }
        getLazyFacade().makeRightItem(editItem, getCurrentUser());
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
        T editItem = findItem(item.getId());  //получаем копию объекта для редактирования 
        if (editItem == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{item.getClass().getSimpleName(), item.getId()});
            return null;
        }
        
        getLazyFacade().makeRightItem(editItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightEdit(editItem)){
            /*
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
            */
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
        BaseDictFacade facade = getLazyFacade();
        try {            
            T editItem =  createItem(item.getParent(), item.getOwner(), getCurrentUser(), new HashMap<>()); //создаём копию объекта
            BeanUtils.copyProperties(editItem, item);
            if (editItem.getRightItem() == null){
                facade.makeRightItem(editItem, getCurrentUser());
            }
            if (!facade.isHaveRightEdit(editItem)){
                /*
                String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
                String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
                errors.add(error);
                */ //нет смысла в сообщении об ошибке, он не даст открыться форме!
                return prepViewItem(item, paramsMap, new HashSet<>());
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
        return createItem(null, owner, getCurrentUser(), new HashMap<>());
    }

    /* СОЗДАНИЕ: Cоздание объекта */
    public T createItem(BaseDict parent, BaseDict owner, User author, Map<String, Object> params) {
        return (T) getLazyFacade().createItem(author, parent, owner, params);
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
        params.put("author", getCurrentUser());
        T newItem = createItem(parent, owner, getCurrentUser(), params);
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
    
    /* Действия перед созданием объекта. Сюда попадаем только если создание идёт через графический интерфейс пользователя */
    protected void prepCreate(T newItem, BaseDict parent, Set<String> errors){
        getLazyFacade().makeRightItem(newItem, getCurrentUser());
        if (getLazyFacade().isHaveRightCreate(newItem)) {
            setSpecAtrForNewItem(newItem);
        } else {
            String objName = MsgUtils.getBandleLabel(getLazyFacade().getMetadatesObj().getBundleName());
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
            errors.add(error);
        }
    }
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    public void setSpecAtrForNewItem(T item) {}

    /**
     * Вставка объекта, не имеющего владельца
     * Объект вставляется как новый объект (копия)
     * @param sourceItem
     * @param recipient
     * @param errors
     * @return
     */
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){       
        T pasteItem = doCopy(sourceItem);
        preparePasteItem(pasteItem, sourceItem, recipient);
        prepCreate(pasteItem, null, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return null;
        }
        changeNamePasteItem(sourceItem, pasteItem);
        getLazyFacade().create(pasteItem);
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
        pasteItem.doSetSingleRole(DictRoles.ROLE_OWNER, getCurrentUser().getId());
        getLazyFacade().setFirstState(pasteItem);
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
        return (T) getLazyFacade().find(id);
    }
    
    /* Возвращает список актуальных объектов, доступных для просмотра текущему пользователю */
    public List<T> findAll(){
        return getLazyFacade().findAll(getCurrentUser());
    }    
    
    /**
     * Формирует список дочерних объектов, доступных текущему пользователю
     * @param owner
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @return 
     */
    public List<T> findDetailItems(BaseDict owner, int first, int pageSize, String sortField, String sortOrder){
        return getLazyFacade().findActualDetailItems(owner, first, pageSize, sortField, sortOrder, getCurrentUser());
    }
    
    /* Копирование объекта */
    public T doCopy(T sourceItem){
        T newItem = createItem(null);
        try {
            BeanUtils.copyProperties(newItem, sourceItem);
            newItem.setIconTree("ui-icon-folder-collapsed");
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return newItem;
    }
    
    /* Изменение имени вставляемого объекта */
    protected void changeNamePasteItem(BaseDict sourceItem, BaseDict pasteItem){
        if (Objects.equals(sourceItem.getParent(), pasteItem.getParent())
                && Objects.equals(sourceItem.getOwner(), pasteItem.getOwner())) {
            String name = getBandleLabel("CopyItem") + " " + pasteItem.getName();
            pasteItem.setName(name);
        }
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
        getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
    }

    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в корзину  */
    public boolean prepareDropItemToTrash(T dragItem, Set<String> errors) {
        getLazyFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightDelete(dragItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightDeleteNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в не актуальные  */
    public boolean prepareDropItemToNotActual(T dragItem, Set<String> errors){
        getLazyFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("RightEditNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;        
    }

    /* Обработка перемещения объекта в не актульные  */
    public void moveItemToNotActual(T item){
        item.setActual(false);
        getLazyFacade().edit(item);
    }            
    
    /* Перед перемещением объекта в группу  */
    public boolean prepareMoveItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {
        getLazyFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightEdit(dragItem)){
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        
        actualizeRightForDropItem(dropItem);

        if (!getLazyFacade().isHaveRightAddChild(dropItem)){
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedAddChilds"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        
        return checkCanMoveItem(dragItem, errors);
    }
    
    /**
     * Проверка возможности переноса объекта
     * @param dragItem
     * @param errors
     * @return 
     */
    protected boolean checkCanMoveItem(T dragItem, Set<String> errors){
        return true;
    }
    
    /* Проверка прав перед добавлением объекта в группу  */
    public boolean checkRightBeforeAddItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {        
        getOwnerBean().getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightAddChild(dropItem)) {
            String error = MessageFormat.format(MsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        getLazyFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getLazyFacade().isHaveRightEdit(dragItem)) {
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
            getLazyFacade().edit(dragItem);
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
    @Override
    public void deleteItem(T item) {
        deleteChilds(item);
        deleteDetails(item);
        preDeleteItem(item);
        numeratorService.doRollBackRegistred(item);
        getLazyFacade().remove(item);
    }

    /* УДАЛЕНИЕ: удаление дочерних child объектов */
    private void deleteChilds(T parent) {
        getLazyFacade().findAllChilds(parent).stream().forEach(child -> deleteItem((T) child));
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
        getLazyFacade().edit(item);
    }

    /* Восстановление подчинённых detail объектов из корзины */
    protected void restoreDetails(T ownerItem) {      
    }

    /* КОРЗИНА: восстановление дочерних childs объектов из корзины */
    private void restoreChilds(T parentItem) {
        getLazyFacade().findAllChilds(parentItem).stream().forEach(item -> doRestoreItemFromTrash((T) item));
    }
    
    /* КОРЗНА: перемещение дочерних элементов в корзину */
    private void moveChildItemToTrash(BaseDict parent, Set<String> errors) {
        getLazyFacade().findAllChilds(parent).stream().forEach(child -> moveToTrash((T) child, errors));
    }

    /* КОРЗИНА: перемещение в корзину подчинённых объектов Владельца (ownerItem) */
    protected void moveDetailItemsToTrash(T item, Set<String> errors) {        
    }

    /* КОРЗИНА: перемещение объекта в корзину */
    public void moveToTrash(T item, Set<String> errors) {
        getLazyFacade().actualizeRightItem(item, getCurrentUser());
        if (getLazyFacade().isHaveRightDelete(item)) {
            checkAllowedDeleteItem(item, errors);
            checkLockItem(item, errors);
            if (errors.isEmpty()) {
                Set<String> errDetail = new HashSet<>();
                moveDetailItemsToTrash(item, errDetail);
                Set<String> errChild = new HashSet<>();
                moveChildItemToTrash(item, errChild);
                if (errChild.isEmpty() && errDetail.isEmpty()) {
                    item.setDeleted(Boolean.TRUE);
                    getLazyFacade().edit(item);
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
        String frmName = getLazyFacade().getFRM_NAME() + "-explorer";        
        sessionBean.openCloseableDialog(frmName, paramsMap);
    }        
    
    /* ИЗБРАННОЕ */
    
    /* ИЗБРАННОЕ: отбор избранных записей для текущего пользователя     */
    public List<T> findFavorites(User currentUser) {        
        List<Integer> favorIds = currentUser.getFavoriteObjList().stream()
                .filter(item -> (item.getMetadateObj().getId().equals(getMetadatesObj().getId())))
                .map(item->item.getObjId())
                .collect(Collectors.toList());
        return getLazyFacade().findByIds(favorIds, currentUser);
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
    public List<T> makeFilteredContent(Filter filter, int first, int pageSize, String sortField, String sortOrder) {
        List<T> result = new ArrayList<>();
        switch (filter.getId()) {
            case DictFilters.TRASH_ID: {
                result = getLazyFacade().loadFromTrash(first, pageSize, sortField, sortOrder, getCurrentUser());
                break;
            }           
            case DictFilters.FAVORITE_ID: {
                result = findFavorites(getCurrentUser());
                break;
            }            
            case DictFilters.USER_CREATED_ID: {
                result = getLazyFacade().findItemsCreatedByUser(getCurrentUser(), first, pageSize);
                break;
            }
            case DictFilters.LAST_CHANGE_ID: {
                result = getLazyFacade().findLastChangedItemsByUser(getCurrentUser(), first, pageSize);
                break;
            }
            case DictFilters.NOTACTUAL_ID: {
                result = getLazyFacade().loadNotActualItems(first, pageSize, getCurrentUser());
                break;
            }
            case DictFilters.ON_MY_EDIT: {
                result = getLazyFacade().loadLockDocuments(getCurrentUser());
                break;
            }
        }
        return result;
    }
    
    /* ПОИСК: Выполняет инициализацию модели данных поиска */
    public SearcheModel initSearcheModel(){
        return new SearcheModel();
    }
    
    /* ПОИСК: Выполняет поиск объектов */
    public List<T> doSearche(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams, int first, int pageSize){
       return getLazyFacade().getByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, addParams, first, pageSize, getCurrentUser());        
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
        return getLazyFacade().replaceItem(oldItem, newItem);
    }        
   
    /* АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: вычисление числа ссылок на объект. */
    public void doGetCountUsesItem(T item, Map<String, Integer> rezult) {
        rezult.put("CheckObjectNotSet", 0);
    }

    @Override
    public String getFormName() {
        return getLazyFacade().getFRM_NAME();
    }

    @Override
    public String getFormHeader() {
        return getMetadatesObj().getBundleJurnalName();
    }
    
    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getLazyFacade().getMetadatesObj();
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