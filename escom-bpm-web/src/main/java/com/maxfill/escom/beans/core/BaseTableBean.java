package com.maxfill.escom.beans.core;

import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.RightFacade;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomMsgUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomMsgUtils.getMessageLabel;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import java.lang.reflect.InvocationTargetException;

import com.maxfill.services.attaches.AttacheService;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.services.print.PrintService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtils;

import javax.ejb.EJB;

/**
 * Базовый бин для работы с табличными представлениями объектов
 * @param <T>
 * @param <O>
 */
public abstract class BaseTableBean<T extends BaseDict, O extends BaseDict> extends LazyLoadBean<T>{
    private static final long serialVersionUID = -4409411219233607045L;

    @EJB
    protected PrintService printService;
    @EJB
    protected FavoriteService favoriteService;
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected AttacheService attacheService;
    @EJB
    protected UserFacade userFacade;
    @EJB
    protected RightFacade rightFacade;

    public abstract List<O> getGroups(T item);          //возвращает список групп объекта   
    public abstract BaseTableBean getDetailBean();       //возвращает бин подчинённых объектов
    public abstract BaseTableBean getOwnerBean();        //возвращает бин владельца объекта

    private Metadates metadatesObj;             //объект метаданных

    public abstract BaseDictFacade getFacade();

    /* Формирование списка детальных данных в таблице обозревателя  */
    public List<T> prepareSetDetails(List<T> sourceItems) {
        return sourceItems.stream()
                    .filter(item -> getFacade().preloadCheckRightView(item, getCurrentUser()))
                    .collect(Collectors.toList());
    }
    
    /* РЕДАКТИРОВАНИЕ/ПРОСМОТР ОБЪЕКТА */     

    /* Подготовка к просмотру объекта на карточке */
    public T prepViewItem(T item, Set<String> errors){
        BaseDictFacade facade = getFacade();
        T editItem = findItem(item.getId());   //получаем копию объекта для просмотра 
        getFacade().makeRightItem(editItem, getCurrentUser());
        if (!facade.isHaveRightView(editItem)){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightViewNo"), new Object[]{objName});
            errors.add(error);
        }
        openItemCard(editItem, DictEditMode.VIEW_MODE, errors);
        return editItem;
    }
    
    /* Подготовка к редактированию объекта на карточке  */      
    public T prepEditItem(T item){        
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getFacade();
        T editItem = findItem(item.getId());   //получаем копию объекта для редактирования 
        getFacade().makeRightItem(editItem, getCurrentUser());
        if (!facade.isHaveRightEdit(editItem)){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
            return prepViewItem(item, errors);
        }
        openItemCard(editItem, DictEditMode.EDIT_MODE, errors);
        return editItem;
    } 
    
    /* СОЗДАНИЕ: Создание объекта с открытием карточки */
    public T createItemAndOpenCard(BaseDict parent, BaseDict owner, Map<String, Object> params){
        Set<String> errors = new HashSet<>();
        T newItem = checkCanCreateItem(parent, owner, errors, params);
        openItemCard(newItem, DictEditMode.INSERT_MODE, errors);
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
     * для того чтобы узнать, может ли пользователь создать объект нужно его создать, получить для него права и только затем проверить*
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
    
    /* Открытие карточки объекта*/
    public void openItemCard(BaseDict item, Integer editMode, Set<String> errors){       
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return;
        }
        
        String itemKey = item.getItemKey();
        
        if (editMode.equals(DictEditMode.EDIT_MODE)){
            User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект        
            if (user != null){
                String objName = user.getName();
                EscomMsgUtils.errorFormatMsg("ObjectAlreadyOpened", new Object[]{objName});
                return;
            }
        }

        String itemOpenKey = appBean.addLockedItem(itemKey, editMode, item, getCurrentUser());
        String cardName = item.getClass().getSimpleName().toLowerCase();
        EscomBeanUtils.openItemForm(cardName, itemOpenKey, sessionBean.getFormSize(cardName));
    }  
    
    /* Действия перед созданием объекта */
    protected void prepCreate(T newItem, BaseDict parent, Set<String> errors){
        boolean isAllowedEditOwner = true;
        boolean isAllowedEditParent = true;
        BaseDict owner = newItem.getOwner();
        if (owner != null) {
            getOwnerBean().getFacade().actualizeRightItem(owner, getCurrentUser());
            isAllowedEditOwner = getFacade().isHaveRightAddDetail(owner); //можно ли создавать подчинённые объекты?
            if (!isAllowedEditOwner){
                String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightAddDetailsNo"), new Object[]{owner.getName(), EscomMsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName())});
                errors.add(error);
            }
        }
        if (parent != null){
            getFacade().actualizeRightItem(parent, getCurrentUser());
            isAllowedEditParent = getFacade().isHaveRightAddChild(parent); //можно ли создавать дочерние объекты?
            if (!isAllowedEditParent){
                String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightAddChildsNo"), new Object[]{parent.getName(), EscomMsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName())});
                errors.add(error);
            }
        }
        if (isAllowedEditOwner && isAllowedEditParent) {
            newItem.setParent(parent);
            getFacade().makeRightItem(newItem, getCurrentUser());
            if (getFacade().isHaveRightCreate(newItem)) {
                setSpecAtrForNewItem(newItem);                
            } else {
                String objName = EscomMsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName());
                String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
                errors.add(error);
            }
        }
    }
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    public void setSpecAtrForNewItem(T item) {}
    
    /* Вставка объекта !!!*/
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){       
        T pasteItem = doCopy(sourceItem);
        preparePasteItem(pasteItem, sourceItem, recipient);
        prepCreate(pasteItem, pasteItem.getParent(), errors); 
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return null;
        }        
        changeNamePasteItem(sourceItem, pasteItem);
        getFacade().create(pasteItem);
        doPasteMakeSpecActions(sourceItem, pasteItem);
        //getFacade().edit(pasteItem);
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
    
    /* Копирование объекта !*/
    public T doCopy(T sourceItem){
        BaseDict ownership = sourceItem.getOwner();
        if (ownership == null){
            ownership = sourceItem.getParent();
        }
        T newItem = createItem(ownership);
        try {
            BeanUtils.copyProperties(newItem, sourceItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return newItem;
    }

    /* Добавление объекта в группу. Вызов из drag & drop */
    public boolean addItemToGroup(T item, BaseDict targetGroup){ 
        return false;
    }   
    
    /* Изменение имени вставляемого объекта */
    private void changeNamePasteItem(BaseDict sourceItem, BaseDict pasteItem){          
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
    
    /* Проверка прав перед добавлением объекта в группу  */
    public boolean checkRightBeforeAddItemToGroup(O dropItem, T dragItem, Set<String> errors) {        
        getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        if (!getFacade().isHaveRightAddChild(dropItem)) {
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
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
        
    /* РЕДАКТИРОВАНИЕ: Перед перемещением объекта в группу  */
    public boolean prepareMoveItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)){
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        
        actualizeRightForDropItem(dropItem);

        if (!getFacade().isHaveRightAddChild(dropItem)){
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("AccessDeniedAddChilds"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }

        return true;
    }

    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (getOwnerBean() != null){
            getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            getFacade().actualizeRightItem(dropItem, getCurrentUser());
        }
    }
            
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в корзину  */
    public boolean prepareDropItemToTrash(T dragItem, Set<String> errors) {
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightDelete(dragItem)) {
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightDeleteNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в не актуальные  */
    public boolean prepareDropItemToNotActual(T dragItem, Set<String> errors){
        getFacade().actualizeRightItem(dragItem, getCurrentUser());
        if (!getFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightEditNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;        
    }
    
    /* РЕДАКТИРОВАНИЕ: Обработка события перемещения в дереве группы в группу  */
    public void moveGroupToGroup(BaseDict dropItem, T dragItem) {
        dragItem.setParent(dropItem);
        getFacade().edit(dragItem);
    }

    /* РЕДАКТИРОВАНИЕ: Обработка перемещения объекта в группу при drag & drop*/
    public void moveItemToGroup(BaseDict dropItem, T dragItem, TreeNode sourceNode) {
        O ownerDragItem = (O) dragItem.getOwner();    
        if (ownerDragItem != null) { //только если owner был, то его можно поменять на новый!             
            dragItem.setOwner(dropItem);
            getFacade().edit(dragItem);
        }
    }

    /* Обработка перемещения объекта в не актульные  */
    public void moveItemToNotActual(T item){
        item.setActual(false);
        getFacade().edit(item);
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
            String metadateName = EscomMsgUtils.getBandleLabel(getMetadatesObj().getBundleName());
            Object[] msgParams = new Object[]{metadateName, item.getName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightDeleteNo"), msgParams);
            errors.add(error);
        }
    } 

    /* CЕЛЕКТОР ОБЪЕКТОВ */
    
    /* CЕЛЕКТОР: Открытие формы селектора для выбора единичного объекта */
    public void onOneSelectItem() {
        openItemSelector(DictExplForm.SING_SELECT_MODE);
    }

    /* СЕЛЕКТОР: открытие формы для выбора нескольких объектов */
    public void onManySelectItem() {
        openItemSelector(DictExplForm.MULTY_SELECT_MODE);
    }

    /* СЕЛЕКТОР: открытие формы выбора */
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
        String frmName = getFacade().getFRM_NAME() + "-explorer";
        PrimeFaces.current().dialog().openDynamic(frmName, options, paramMap);
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
     * Добавление документа в избранное
     * @param doc
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
    public List<T> doSearche(List<Integer> states, Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<O> searcheGroups, Map<String, Object> addParams){
        List<T> sourceItems = getFacade().getByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, addParams);
        if (searcheGroups.isEmpty()){
            return prepareSetDetails(sourceItems);
        } else {
            List<T> searcheItems = new ArrayList<>();
            for (T item : sourceItems) {
                boolean include = false;

                List<O> itemGroups = getGroups((T)item);
                if (itemGroups != null) {
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
            return prepareSetDetails(searcheItems);
        }
    }
    
    /* СЛУЖЕБНЫЕ МЕТОДЫ  */
    
    /* Определяет доступность кнопки "Создать" на панели обозревателя */
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return getOwnerBean() != null && treeSelectedNode == null;
    }

    /**
     * Замена одного объекта на другой
     * @param oldItem
     * @param newItem
     */
    public int replaceItem(BaseDict oldItem, BaseDict newItem){
        return getFacade().replaceItem(oldItem, newItem);
    }        
   
    /* АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: вычисление числа ссылок на объект. */
    public void doGetCountUsesItem(T item, Map<String, Integer> rezult) {
        rezult.put("CheckObjectNotSet", 0);
    }
       
    public abstract Class<O> getOwnerClass();

    @Override
    public String getFormName() {
        return getFacade().getFRM_NAME();
    }

    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getFacade().getMetadatesObj();
        }
        return metadatesObj;
    }
}