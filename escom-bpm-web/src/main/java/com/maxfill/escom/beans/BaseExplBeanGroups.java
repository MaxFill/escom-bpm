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

}
