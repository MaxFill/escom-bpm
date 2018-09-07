package com.maxfill.escom.beans.core;

import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.BaseDict;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseDetailsBean<T , O >{
    private static final long serialVersionUID = -2983279513793115056L;
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    @Override
    public void setSpecAtrForNewItem(T item) {
        item.setInheritsAccessChilds(true);
        getFacade().makeRightForChilds(item);
    }
    
    /* Базовый метод формирования детального списка для группы  */
    public List<BaseDict> makeGroupContent(BaseDict group, Integer viewMode, int first, int pageSize, String sortField, String sortOrder){
        List<BaseDict> cnt = new ArrayList();        
        List<BaseDict> details = getDetailBean().getFacade().findActualDetailItems(group, first, pageSize, sortField,  sortOrder);
        details.stream().forEach(item -> addDetailItemInContent(item, cnt));
        return cnt;
    }
    
    public void loadChilds(BaseDict item, TreeNode node){
        if ("ui-icon-folder-collapsed".equals(item.getIconTree())){
            node.setExpanded(true);
            node.getChildren().clear();
            List<T> childs = getFacade().findActualChilds(item);
            childs.stream().forEach(itemChild -> addItemInTree(node, itemChild, "tree"));
            item.setIconTree("ui-icon-folder-open");
        }
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
        tree.setExpanded(true);
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
            doExpandTreeNode(newNode);
            //List<T> childs = getFacade().findActualChilds(item);
            //childs.stream().forEach(itemChild -> addItemInTree(newNode, itemChild, typeNode));
            rezNode = newNode;
        }
        return rezNode;
    }              
    
    protected void doExpandTreeNode(TreeNode node){}
    
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
            details.stream().forEach(detail -> getDetailBean().moveToTrash((T) detail, errors));
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
        Long count = getFacade().getCountDetails(item);
        if (count > 0) {
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public abstract BaseDetailsBean getDetailBean();

}
