package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictFilters;
import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import java.lang.reflect.InvocationTargetException;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;

public abstract class BaseExplBean<T extends BaseDict, O extends BaseDict> extends BaseBean<T> {
    private static final long serialVersionUID = -4409411219233607045L; 
       
    public abstract List<O> getGroups(T item);          //возвращает список групп объекта   
    public abstract BaseExplBean getDetailBean();       //возвращает бин подчинённых объектов
    public abstract BaseExplBean getOwnerBean();        //возвращает бин владельца объекта 
    
    @Override
    public void onInitBean(){
    }

    /* Формирование списка детальных данных в таблице обозревателя  */
    public List<T> prepareSetDetails(List<T> sourceItems) {
        return sourceItems.stream()
                    .filter(item -> preloadCheckRightView(item))
                    .collect(Collectors.toList());
    }
    
    /* РЕДАКТИРОВАНИЕ/ПРОСМОТР ОБЪЕКТА */     

    /* Подготовка к просмотру объекта на карточке */
    public T prepViewItem(T item){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacade();        
        T editItem = findItem(item.getId());   //получаем копию объекта для просмотра 
        makeRightItem(editItem);        
        if (!isHaveRightView(editItem)){ 
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
        BaseDictFacade facade = getItemFacade();
        T editItem = findItem(item.getId());   //получаем копию объекта для редактирования 
        makeRightItem(editItem);
        if (!isHaveRightEdit(editItem)){            
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
        openItemCard(editItem, DictEditMode.EDIT_MODE, errors);
        return editItem;
    } 
    
    /* Создание объекта с открытием карточки */
    public T createItemAndOpenCard(BaseDict parent, BaseDict owner, Map<String, Object> params){
        Set<String> errors = new HashSet<>();
        T newItem = checkCanCreateItem(parent, owner, errors, params);
        openItemCard(newItem, DictEditMode.INSERT_MODE, errors);
        return newItem;
    }

    /* Проверка возможности создание объекта с открытием карточки */
    public T checkCanCreateItem(BaseDict parent, BaseDict owner, Set<String> errors, Map<String, Object> params){
        T newItem = createItem(owner, currentUser);
        prepCreate(newItem, parent, errors, params);
        return newItem;
    }
    
    /* Открытие карточки объекта*/
    public void openItemCard(BaseDict item, Integer editMode, Set<String> errors){       
        if (!errors.isEmpty()){
            EscomBeanUtils.showErrorsMsg(errors);
            return;
        }
        
        String itemKey = item.getItemKey();
        
        if (editMode.equals(DictEditMode.EDIT_MODE)){
            User user = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект        
            if (user != null){
                String objName = user.getName();
                EscomBeanUtils.ErrorFormatMessage("AccessDenied", "ObjectAlreadyOpened", new Object[]{objName});
                return;
            }
        }

        String itemOpenKey = appBean.addLockedItem(itemKey, editMode, item, currentUser);
        String cardName = item.getClass().getSimpleName().toLowerCase();
        EscomBeanUtils.openItemForm(cardName, itemOpenKey, sessionBean.getFormSize(cardName));
    }  
    
    /* Действия перед созданием объекта */
    private void prepCreate(T newItem, BaseDict parent, Set<String> errors, Map<String, Object> params){
        boolean isAllowedEditOwner = true;
        boolean isAllowedEditParent = true;
        BaseDict owner = newItem.getOwner();
        if (owner != null) {
            getOwnerBean().actualizeRightItem(owner);
            isAllowedEditOwner = isHaveRightAddChild(owner); //можно ли создавать дочерние объекты?
        }
        if (parent != null){
            actualizeRightItem(parent);
            isAllowedEditParent = isHaveRightAddChild(parent); //можно ли создавать дочерние объекты?
        }
        if (isAllowedEditOwner && isAllowedEditParent) {
            newItem.setParent(parent);            
            makeRightItem(newItem);
            if (isHaveRightCreate(newItem)) {
                setSpecAtrForNewItem(newItem, params);                
            } else {
                String objName = EscomBeanUtils.getBandleLabel(getItemFacade().getMetadatesObj().getBundleName());
                String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
                errors.add(error);
            }
        } else {
            if (owner != null){
                String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightAddChildsNo"), new Object[]{owner.getName()});
                errors.add(error);
            }
        }
    }
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    public void setSpecAtrForNewItem(T item, Map<String, Object> params) {}
    
    /* Вставка объекта */
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){       
        T pasteItem = doCopy(sourceItem, currentUser);        
        preparePasteItem(pasteItem, sourceItem, recipient);
        prepCreate(pasteItem, pasteItem.getParent(), errors, null); 
        if (!errors.isEmpty()){
            EscomBeanUtils.showErrorsMsg(errors);
            return null;
        }        
        changeNamePasteItem(sourceItem, pasteItem);
        getItemFacade().create(pasteItem);
        doPasteMakeSpecActions(sourceItem, pasteItem);
        getItemFacade().edit(pasteItem);
        return pasteItem;
    }
    
    protected void doPasteMakeSpecActions(T sourceItem, T pasteItem){        
    }
    
    /* Специфичные действия перед вставкой скопированного объекта */
    public void preparePasteItem(T pasteItem, T sourceItem, BaseDict recipient){
        pasteItem.setChildItems(null);
        pasteItem.setState(null);
        pasteItem.setId(null);                    //нужно сбросить скопированный id!
        pasteItem.setItemLogs(new ArrayList<>()); //нужно сбросить скопированный log !
        pasteItem.doSetSingleRole(DictRoles.ROLE_OWNER, currentUser);
        getItemFacade().doSetState(pasteItem, getMetadatesObj().getStateForNewObj());
    };  
    
    /* Определяется, нужно ли копировать объект при вставке */
    public boolean isNeedCopyOnPaste(T item, BaseDict recipient){
        return true;
    }
    
    /* Возвращает объект по его Id */
    public T findItem(Integer id){
        return (T)getItemFacade().find(id);
    }
    
    /* Возвращает список актуальных объектов, доступных для просмотра текущему пользователю */
    public List<T> findAll(){        
        return (List<T>) getItemFacade().findAll().stream()
                    .filter(item -> preloadCheckRightView((T)item))
                    .collect(Collectors.toList());             
    }
    
    /* Копирование объекта */
    public T doCopy(T sourceItem, User author){
        T newItem = createItem(sourceItem.getOwner());
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
    
    /* Возвращает списки зависимых объектов, необходимых для копирования при вставке объекта */
    public List<List<?>> doGetDependency(T item){
        return null;
    }           
    
    /* Обработка перед добавлением объекта в группу  */
    public boolean prepareAddItemToGroup(O dropItem, T dragItem, Set<String> errors) {        
        getOwnerBean().actualizeRightItem(dropItem);
        if (!isHaveRightEdit(dropItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()}); 
            errors.add(error);
            return false;
        }
        actualizeRightItem(dragItem);
        if (!isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()}); 
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
        actualizeRightItem(dragItem);
        if (!isHaveRightEdit(dragItem)){
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        
        actualizeRightForDropItem(dropItem);

        if (!isHaveRightAddChild(dropItem)){
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedAddChilds"), new Object[]{dropItem.getName()});
            errors.add(error);
            return false;
        }

        return true;
    }

    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (getOwnerBean() != null){
            getOwnerBean().actualizeRightItem(dropItem);
        } else {
            actualizeRightItem(dropItem);
        }        
    }
            
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в корзину  */
    public boolean prepareDropItemToTrash(T dragItem, Set<String> errors) {        
        actualizeRightItem(dragItem);
        if (!isHaveRightDelete(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightDeleteNo"), new Object[]{dragItem.getName()});
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* РЕДАКТИРОВАНИЕ: Проверка перед перемещением объекта в не актуальные  */
    public boolean prepareDropItemToNotActual(T dragItem, Set<String> errors){
        actualizeRightItem(dragItem);
        if (!isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightEditNo"), new Object[]{dragItem.getName()}); 
            errors.add(error);
            return false;
        }
        return true;        
    }
    
    /* РЕДАКТИРОВАНИЕ: Обработка события перемещения в дереве группы в группу  */
    public void moveGroupToGroup(BaseDict dropItem, T dragItem) {
        dragItem.setParent(dropItem);
        getItemFacade().edit(dragItem);
    }

    /* РЕДАКТИРОВАНИЕ: Обработка перемещения объекта в группу при drag & drop*/
    public void moveItemToGroup(BaseDict dropItem, T dragItem, TreeNode sourceNode) {
        O ownerDragItem = (O) dragItem.getOwner();    
        if (ownerDragItem != null) { //только если owner был, то его можно поменять на новый!             
            dragItem.setOwner(dropItem);
            getItemFacade().edit(dragItem);
        }
    }

    /* Обработка перемещения объекта в не актульные  */
    public void moveItemToNotActual(T item){
        item.setActual(false);
        getItemFacade().edit(item);
    }            
    
    /* УДАЛЕНИЕ  */
    
    /* Удаление выполняется сначала только в корзину! На формах кнопки только помещения в корзину!
     * Потом можно удалить из корзины или очистить корзину целиком, но для этого уже нужно быть Администратором! 
     * При помещении объекта в корзину проверяются его зависимости и при их наличии выдаётся ошибка.
     * При удалении объекта из корзины проверок не выполняется */
        
    /* УДАЛЕНИЕ: Проверка возможности удаления объекта. Переопределяется в бинах  */
    protected void checkAllowedDeleteItem(T item, Set<String> errors) {        
        if (CollectionUtils.isNotEmpty(item.getChildItems())) {
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }    
    
    /* УДАЛЕНИЕ: удаление объекта вместе с дочерними и подчинёнными  */
    public void deleteItem(T item) {
        deleteChilds(item);
        deleteDetails(item);
        preDeleteItem(item);
        numeratorService.doRollBackRegistred(item, getItemFacade().getFRM_NAME());
        getItemFacade().remove(item);
    }

    /* УДАЛЕНИЕ: удаление дочерних child объектов */
    private void deleteChilds(T parent) {
        if (parent.getChildItems() != null){
            parent.getChildItems().stream().forEach(child -> deleteItem((T) child));
        }
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
        getItemFacade().edit(item);
    }

    /* Восстановление подчинённых detail объектов из корзины */
    protected void restoreDetails(T ownerItem) {      
    }

    /* КОРЗИНА: восстановление дочерних childs объектов из корзины */
    private void restoreChilds(T parentItem) {
        parentItem.getChildItems().stream().forEach(item -> doRestoreItemFromTrash((T) item));
    }
    
    /* КОРЗНА: перемещение дочерних элементов в корзину */
    private void moveChildItemToTrash(BaseDict parent, Set<String> errors) {
        if (parent.getChildItems() != null){
            parent.getChildItems().stream().forEach(child -> moveToTrash((T) child, errors));
        }
    }

    /* КОРЗИНА: перемещение в корзину подчинённых объектов Владельца (ownerItem) */
    protected void moveDetailItemsToTrash(T item, Set<String> errors) {        
    }

    /* КОРЗИНА: перемещение объекта в корзину */
    public void moveToTrash(T item, Set<String> errors) {        
        actualizeRightItem(item);
        if (isHaveRightDelete(item)) {
            checkAllowedDeleteItem(item, errors);
            checkLockItem(item, errors);
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
            String metadateName = EscomBeanUtils.getBandleLabel(getMetadatesObj().getBundleName());
            Object[] msgParams = new Object[]{metadateName, item.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("RightDeleteNo"), msgParams);
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
        String frmName = getItemFacade().getFRM_NAME() + "-explorer";
        RequestContext.getCurrentInstance().openDialog(frmName, options, paramMap);
    }        
    
    /* ИЗБРАННОЕ */
    
    /* ИЗБРАННОЕ: отбор избранных записей для текущего пользователя     */
    public List<T> findFavorites() {        
        List<Integer> favorIds = new ArrayList<>();
        List<FavoriteObj> favorites = currentUser.getFavoriteObjList();
        favorites.stream()
                .filter(item -> (item.getMetadateObj().getId().equals(getMetadatesObj().getId())))
                .forEach(item -> favorIds.add(item.getObjId()));
        return getItemFacade().findByIds(favorIds);
    }   

    /* ИЗБРАННОЕ: удаление объекта из избранного   */
    public void delFromFavorites(BaseDict item) {
        favoriteService.delFromFavorites(item, getMetadatesObj(), currentUser);
    } 
    
    /*  ФИЛЬТРЫ */
    
    /* ФИЛЬТРЫ: формирование списка результатов для выбранного фильтра  */
    public List<T> makeFilteredContent(Filter filter) {
        List<T> result = new ArrayList<>();
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
            case DictFilters.ON_MY_EDIT: {
                result = getItemFacade().loadLockDocuments(currentUser);
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
        List<T> sourceItems = getItemFacade().getByParameters(states, paramEQ, paramLIKE, paramIN, paramDATE, addParams);
        if (searcheGroups.isEmpty()){
            return prepareSetDetails(sourceItems);
        } else {
            List<T> searcheItems = new ArrayList<>();
            for (T item : sourceItems) {
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
            return prepareSetDetails(searcheItems);
        }
    }
    
    /* СЛУЖЕБНЫЕ МЕТОДЫ  */
    
    /* Определяет доступность кнопки "Создать" на панели обозревателя */
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return getOwnerBean() != null && treeSelectedNode == null;
    }
        
    public void replaceItem(BaseDict oldItem, BaseDict newItem){
        getItemFacade().replaceItem(oldItem, newItem);
    }        
   
    /* АДМИНИСТРИРОВАНИЕ ОБЪЕКТОВ: вычисление числа ссылок на объект. */
    public void doGetCountUsesItem(T item, Map<String, Integer> rezult) {
        rezult.put("CheckObjectNotSet", 0);
    }
       
    public abstract Class<O> getOwnerClass();        
 
}