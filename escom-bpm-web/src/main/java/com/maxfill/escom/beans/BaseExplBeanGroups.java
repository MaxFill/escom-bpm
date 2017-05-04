package com.maxfill.escom.beans;

import com.maxfill.model.BaseDict;
import java.util.List;

/**
 * Реализация методов для объектов с группами (пользователи, контрагенты и т.п.)
 *
 * @author mfilatov
 * @param <T>
 * @param <O>
 */
public abstract class BaseExplBeanGroups<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T, O> {
    private static final long serialVersionUID = -2983279513793115056L;    

    private BaseTreeBean ownerBean;

    /**
     * Возвращает список объектов из группы
     *
     * @param owner
     * @return
     */
    protected List<T> getItemsFromGroup(O owner) {
        return owner.getDetailItems();
    }       
    
    /* *** GET & SET *** */
    
    @Override
    public BaseTreeBean getOwnerBean() {
        return ownerBean;
    }

    public void setOwnerBean(BaseTreeBean ownerBean) {
        this.ownerBean = ownerBean;
    }
}
