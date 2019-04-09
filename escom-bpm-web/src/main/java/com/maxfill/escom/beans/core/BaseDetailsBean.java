package com.maxfill.escom.beans.core;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
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
    private static final long serialVersionUID = 6547409445062031636L;

    public abstract List<O> getGroups(T item);          //возвращает список групп объекта

    /**
     * Проверки на возможность создания дочернего/подчинённого объекта
     * @param newItem
     * @param parent
     * @param errors
     */
    @Override
    protected void prepCreate(T newItem, BaseDict parent, Set<String> errors){
        boolean isAllowedEditOwner = true;
        boolean isAllowedEditParent = true;
        BaseDict owner = newItem.getOwner();
        if (owner != null) {
            getOwnerBean().getLazyFacade().actualizeRightItem(owner, getCurrentUser());
            isAllowedEditOwner = getLazyFacade().isHaveRightAddDetail(owner); //можно ли создавать подчинённые объекты?
            if (!isAllowedEditOwner){
                String error = MessageFormat.format(MsgUtils.getMessageLabel("RightAddDetailsNo"), new Object[]{owner.getName(), MsgUtils.getBandleLabel(getLazyFacade().getMetadatesObj().getBundleName())});
                errors.add(error);
            }
        }
        if (parent != null){
            getLazyFacade().actualizeRightItem(parent, getCurrentUser());
            isAllowedEditParent = getLazyFacade().isHaveRightAddChild(parent); //можно ли создавать дочерние объекты?
            if (!isAllowedEditParent){
                String error = MessageFormat.format(MsgUtils.getMessageLabel("RightAddChildsNo"), new Object[]{parent.getName(), MsgUtils.getBandleLabel(getLazyFacade().getMetadatesObj().getBundleName())});
                errors.add(error);
            }
        }
        if (isAllowedEditOwner && isAllowedEditParent) {
            newItem.setParent(parent);
            super.prepCreate(newItem, parent, errors);
        }
    }

    /**
     * Вставка объекта, имеющего владельца
     * @param sourceItem
     * @param recipient
     * @param errors
     * @return
     */
    @Override
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){
        T pasteItem = doCopy(sourceItem); //создан новый объект (копия)
        preparePasteItem(pasteItem, sourceItem, recipient);
        prepCreate(pasteItem, pasteItem.getParent(), errors);
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
            return null;
        }
        changeNamePasteItem(sourceItem, pasteItem);
        getLazyFacade().create(pasteItem);
        doPasteMakeSpecActions(sourceItem, pasteItem);
        return pasteItem;
    }

    /**
     * Добавление объекта в группу
     * Выполняется в бине группы!
     * @param item
     * @param group
     */
    public void addItemInGroup(BaseDict item, T group){
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
            newItem.setIconTree("ui-icon-folder-collapsed");
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return newItem;
    }

    /* Добавление объекта в группу. Вызов из drag & drop */
    public boolean addItemToGroup(T item, BaseDict targetGroup){ 
        return false;
    }    

    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (getOwnerBean() != null){
            getOwnerBean().getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
        }
    }
    
    /* Обработка события перемещения в дереве группы в группу  */
    public void moveGroupToGroup(BaseDict dropItem, T dragItem) {
        dragItem.setParent(dropItem);
        getLazyFacade().edit(dragItem);
    }    

    /**
    * Определяет доступность кнопки "Создать" на панели обозревателя
    * Если метод возвращает true то кнопка не доступна!
     * @return 
    */
    @Override
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return getOwnerBean() != null && treeSelectedNode == null;
    }
       
    public abstract Class<O> getOwnerClass();

    
}