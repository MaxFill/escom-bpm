package com.maxfill.escom.beans;

import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/* Реализация методов для объектов с группами (пользователи, контрагенты и т.п.) */
public abstract class BaseExplBeanGroups<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T, O> {
    private static final long serialVersionUID = -2983279513793115056L;    

    public abstract BaseExplBean getGroupBean();
    
    /* Обработка перед добавлением объекта в группу  */
    @Override
    public boolean checkRightBeforeAddItemToGroup(O dropItem, T dragItem, Set<String> errors) {        
        getGroupBean().getItemFacade().actualizeRightItem(dropItem, currentUser);
        if (!getItemFacade().isHaveRightAddChild(dropItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dropItem.getName()}); 
            errors.add(error);
            return false;
        }
        getItemFacade().actualizeRightItem(dragItem, currentUser);
        if (!getItemFacade().isHaveRightEdit(dragItem)) {
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("AccessDeniedEdit"), new Object[]{dragItem.getName()}); 
            errors.add(error);
            return false;
        }
        return true;
    }
    
    /* Возвращает список объектов из группы  */
    protected List<T> getItemsFromGroup(O owner) {
        return getItemFacade().findActualDetailItems(owner);
    }
}
