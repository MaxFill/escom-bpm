package com.maxfill.escom.beans.core;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import com.maxfill.services.attaches.AttacheService;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Базовый бин для работы с табличными подчинёнными объектами (имеющими владельца)
 * @param <T> - класс объекта
 * @param <O> - класс владельца
 */
public abstract class BaseDetailsBean<T extends BaseDict, O extends BaseDict> extends BaseTableBean<T>{

    @EJB
    protected AttacheService attacheService;

    public abstract List<O> getGroups(T item);          //возвращает список групп объекта
    public abstract BaseDetailsBean getOwnerBean();     //возвращает бин владельца объекта

    
    /* Действия перед созданием объекта */
    @Override
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
            prepCreate(newItem, parent, errors);
        }
    }

    
    /* Вставка объекта !!!*/
    @Override
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
        return pasteItem;
    }

    /* Копирование объекта */
    @Override
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


    /* Перед перемещением объекта в группу  */
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

    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (getOwnerBean() != null){
            getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            getFacade().actualizeRightItem(dropItem, getCurrentUser());
        }
    }

    
    /* Обработка события перемещения в дереве группы в группу  */
    public void moveGroupToGroup(BaseDict dropItem, T dragItem) {
        dragItem.setParent(dropItem);
        getFacade().edit(dragItem);
    }

    /* Обработка перемещения объекта в группу при drag & drop*/
    public void moveItemToGroup(BaseDict dropItem, T dragItem, TreeNode sourceNode) {
        O ownerDragItem = (O) dragItem.getOwner();    
        if (ownerDragItem != null) { //только если owner был, то его можно поменять на новый!             
            dragItem.setOwner(dropItem);
            getFacade().edit(dragItem);
        }
    }


    /* ПОИСК: Выполняет поиск объектов c учётом групп */
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

    /* Определяет доступность кнопки "Создать" на панели обозревателя */
    @Override
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return getOwnerBean() != null && treeSelectedNode == null;
    }

       
    public abstract Class<O> getOwnerClass();

}