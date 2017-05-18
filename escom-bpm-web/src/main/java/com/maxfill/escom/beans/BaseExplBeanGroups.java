package com.maxfill.escom.beans;

import com.maxfill.model.BaseDict;
import java.util.List;

/* Реализация методов для объектов с группами (пользователи, контрагенты и т.п.) */
public abstract class BaseExplBeanGroups<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T, O> {
    private static final long serialVersionUID = -2983279513793115056L;    

    /* Возвращает список объектов из группы  */
    protected List<T> getItemsFromGroup(O owner) {
        return owner.getDetailItems();
    }
}
