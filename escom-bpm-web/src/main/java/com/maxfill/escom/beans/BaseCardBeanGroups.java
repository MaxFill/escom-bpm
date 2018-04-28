package com.maxfill.escom.beans;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.model.BaseDict;
import org.primefaces.event.SelectEvent;

import java.util.ArrayList;
import java.util.List;

/* Реализация методов для объектов с группами (пользователи, контрагенты и т.п.) */

public abstract class BaseCardBeanGroups<T extends BaseDict, O extends BaseDict> extends BaseCardBean<T>{
    private static final long serialVersionUID = -3667710671312550624L;

    private List<O> checkedGroups = new ArrayList<>(); 
    protected final List<O> deleteGroups = new ArrayList<>();
    protected final List<O> addGroups = new ArrayList<>();
    
    public abstract List<O> getGroups(T item);          //возвращает список групп объекта 
    
    /* Установка владельца объекта в виде основной группы  */
    public void makeMainGroup(O group) {
        getEditedItem().setOwner(group);
        onItemChange();
    }
    
    /* Удаление отмеченных групп из редактируемого объекта. Вызов с карточки объекта */
    public void deleteFromCheckedGroups(){        
        checkedGroups.stream()
                .filter(group -> group.getId() != 0)
                .forEach(group -> deleteFromGroup(group, getEditedItem())
        );
        onItemChange();
    }    
    
    /* Удаление группы из редактируемого объекта */
    public void deleteFromGroup(O group){
        T item = getEditedItem();
        if (item == null){
            throw new NullPointerException("Edited item is null!");
        }
        deleteFromGroup(group, item);
        onItemChange();
    }
    
    /* Удаление группы из объекта */
    private void deleteFromGroup(O group, T item){
        deleteGroups.add(group);        
        getGroups(item).remove(group);
        O owner = (O) item.getOwner();
        if (group.equals(owner)){       //сброс owner, если он был удалён из списка групп
            item.setOwner(null);
        }
    }
    
    /* Добавление групп в редактируемый объект из селектора с деревом  */
    public void addGroupsFromSelector(SelectEvent event){
        if (event.getObject() == null){
            throw new NullPointerException("EscomERR: Selected object in selector is null!");
        }
        List<O> items = (List<O>) event.getObject();
        if (items.isEmpty()) return;
        onItemChange();
        items.stream().forEach(group -> addItemInGroup(getEditedItem(), group));
    }
    
    /* Добавление владельца объекта в группы объекта */
    @Override
    protected void addOwnerInGroups(T item){ 
        addItemInGroup(item, (O)item.getOwner());
    }
    
    /* Добавление группы в объект */
    protected void addItemInGroup(T item, O group) {
        if (group != null){
            List<O> groups = getGroups(item);            
            if (!groups.contains(group)){
                groups.add(group);
            } 
            if(!addGroups.contains(group)){
                addGroups.add(group);
            }
            if(item.getOwner() == null){
                getOwnerAndAddGroups(item, group);
            }
            onItemChange();
        }
    }
    
    /* Возвращает группу верхнего уровня и добавляет промежуточные группы в список групп объекта  */
    public O getOwnerAndAddGroups(T item, O group) {                      
        List<O> groups = getGroups(item);
        if (!group.getId().equals(0) && !groups.contains(group)){
            groups.add(group);
            if(!addGroups.contains(group)){
                addGroups.add(group);
            }            
        }
        if (group.getParent() != null){
            group = getOwnerAndAddGroups(item, (O) group.getParent());
        }
        return group;
    }

    @Override
    public Integer getRightColSpan(){
        return 8;
    }

    public List<O> getCheckedGroups() {
        return checkedGroups;
    }
    public void setCheckedGroups(List<O> checkedGroups) {
        this.checkedGroups = checkedGroups;
    }
}
