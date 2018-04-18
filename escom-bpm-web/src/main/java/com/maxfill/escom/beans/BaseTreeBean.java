package com.maxfill.escom.beans;

import static com.maxfill.escom.utils.EscomMsgUtils.getMessageLabel;

import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.BaseDict;

import java.text.MessageFormat;
import java.util.ArrayList;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseTableBean<T , O >{
    private static final long serialVersionUID = -2983279513793115056L;
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    @Override
    public void setSpecAtrForNewItem(T item) {
        item.setInheritsAccessChilds(true);
        getFacade().makeRightForChilds(item);
    }
    
    /* Базовый метод формирования детального списка для группы  */
    public List<BaseDict> makeGroupContent(BaseDict group, Integer viewMode){   
        List<BaseDict> cnt = new ArrayList();
        List<BaseDict> details = getDetailBean().getFacade().findActualDetailItems(group);
        details.stream().forEach(item -> addDetailItemInContent(item, cnt));
        return cnt;
    }
    
    /* Добавление подчинённого объекта в контент */
    protected void addDetailItemInContent(BaseDict item, List<BaseDict> cnt){
        if (getDetailBean().getFacade().preloadCheckRightView(item, getCurrentUser())){
            cnt.add(item);
        }
    }
    
    /* Добавление дочернего объекта в контент */
    protected void addChildItemInContent(T item, List<BaseDict> cnt){
        if (getFacade().preloadCheckRightView(item, getCurrentUser())){
            cnt.add(item);
        }
    }
    
    /* Формирование дерева */
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        List<T> rootItem = getFacade().findRootItems();
        List<BaseDict> sourceTreeItems = rootItem.stream()
                .filter(item -> getFacade().preloadCheckRightView(item, getCurrentUser()))
                .collect(Collectors.toList());
        sourceTreeItems.stream().forEach(treeItem -> addItemInTree(tree, treeItem, "tree"));
        return tree;
    }

    /* Добавление узла в дерево при его формировании  */
    public TreeNode addItemInTree(TreeNode parentNode, BaseDict item, String typeNode) {
        TreeNode rezNode = null;
        if (getFacade().preloadCheckRightView(item, getCurrentUser())) {
            TreeNode newNode = new DefaultTreeNode(typeNode, item, parentNode);

            List<T> childs = getFacade().findActualChilds(item);
            childs.stream()
                    .forEach(itemChild -> addItemInTree(newNode, itemChild, typeNode)
            );
            rezNode = newNode;
        }
        return rezNode;
    }              
    
    /* Удаление подчинённых (связанных) объектов */
    @Override
    protected void deleteDetails(T item) {
        List<BaseDict> details = getFacade().findAllDetailItems(item);
        if (details != null) {
            details.stream().forEach(child -> getDetailBean().deleteItem((T) child));
        }
    }
    
    /* Восстановление подчинённых detail объектов из корзины */
    @Override
    protected void restoreDetails(T ownerItem) {
        List<BaseDict> details = getFacade().findAllDetailItems(ownerItem);
        if (details != null){
            details.stream().forEach(item -> getDetailBean().doRestoreItemFromTrash((T) item));
        }
    }
    
    /* Перемещение в корзину подчинённых объектов Владельца */
    @Override
    protected void moveDetailItemsToTrash(T ownerItem, Set<String> errors) {        
        List<BaseDict> details = getFacade().findAllDetailItems(ownerItem);
        if (details != null){
            details.stream().forEach(detail -> getDetailBean().moveToTrash((T) detail, errors)
            );
        }
    }

    /**
     * Базовый метод проверки возможности удаления древовидного объекта
     * Древовидный объект можно удалить только если у него нет подчинённых объектов.
     * При удалении дочерних объектов быдет выполнена эта же проверка
     * @param item
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(T item, Set<String> errors) {
        List<BaseDict> details = getFacade().findAllDetailItems(item);
        if (CollectionUtils.isNotEmpty(details)) {
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public abstract BaseTableBean getDetailBean();

}
