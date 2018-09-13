package com.maxfill.escom.beans;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.BaseDict;
import java.text.MessageFormat;
import java.util.Set;

/* Реализация методов для объектов с группами (пользователи, контрагенты и т.п.) */
public abstract class BaseExplBeanGroups<T extends BaseDict, O extends BaseDict> extends BaseDetailsBean<T, O>{
    private static final long serialVersionUID = -2983279513793115056L;    

    public abstract BaseDetailsBean getGroupBean();
    
    /* Обработка перед добавлением объекта в группу  */
    @Override
    public boolean checkRightBeforeAddItemToGroup(BaseDict dropItem, T dragItem, Set<String> errors) {        
        getGroupBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
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

    @Override
    public T doPasteItem(T sourceItem, BaseDict recipient, Set<String> errors){
        if(!isNeedCopyOnPaste(sourceItem, recipient)) { //если объект вставляется в группу как сссылка
            getGroupBean().addItemInGroup(sourceItem, recipient);
            if (sourceItem.getId() != 0) {
                addItemToGroup(sourceItem, recipient);
            }
            return sourceItem;
        } else { //если объект необходимо вставлять как копию
            T pasteItem = doCopy(sourceItem); //создан новый объект (копия)
            preparePasteItem(pasteItem, sourceItem, recipient);
            prepCreate(pasteItem, pasteItem.getParent(), errors);
            if (!errors.isEmpty()) {
                MsgUtils.showErrors(errors);
                return null;
            }
            changeNamePasteItem(sourceItem, pasteItem);
            getFacade().create(pasteItem);
            doPasteMakeSpecActions(sourceItem, pasteItem);
            return pasteItem;
        }
    }

}
