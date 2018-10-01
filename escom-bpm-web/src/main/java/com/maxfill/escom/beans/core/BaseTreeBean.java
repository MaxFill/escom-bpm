package com.maxfill.escom.beans.core;

import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.basedict.BaseDict;
import java.text.MessageFormat;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.List;
import java.util.Set;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseDetailsBean<T , O >{
    private static final long serialVersionUID = -2983279513793115056L;

    /* Установка специфичных атрибутов при создании объекта  */ 
    @Override
    public void setSpecAtrForNewItem(T item) {
        item.setInheritsAccessChilds(true);
        getLazyFacade().makeRightForChilds(item);
    }
    
    /* Базовый метод формирования детального списка для группы  */
    public List<BaseDict> makeGroupContent(BaseDict group, BaseTableBean tableBean, Integer viewMode, int first, int pageSize, String sortField, String sortOrder){        
        return getDetailBean().getLazyFacade().findActualDetailItems(group, first, pageSize, sortField,  sortOrder, getCurrentUser());        
    }
    
    public void loadChilds(BaseDict item, TreeNode node){
        if ("ui-icon-folder-collapsed".equals(item.getIconTree())){
            node.setExpanded(true);
            node.getChildren().clear();
            getLazyFacade().findActualChilds(item, getCurrentUser()).forEach(itemChild -> addItemInTree(node, (BaseDict)itemChild, "tree"));
            item.setIconTree("ui-icon-folder-open");
        }
    }         
    
    /* Формирование дерева */
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        tree.setExpanded(true);
        getLazyFacade().findRootItems(getCurrentUser()).forEach(treeItem -> addItemInTree(tree, (BaseDict)treeItem, "tree"));
        return tree;
    }

    /* Добавление узла в дерево при его формировании  */
    public TreeNode addItemInTree(TreeNode parentNode, BaseDict item, String typeNode) {
        return new DefaultTreeNode(typeNode, item, parentNode);                
    }
    
    /* Удаление подчинённых (связанных) объектов */
    @Override
    protected void deleteDetails(T item) {
        List<BaseDict> details = getLazyFacade().findDetailItems(item);
        if (details != null) {
            details.stream().forEach(child -> getDetailBean().deleteItem((T) child));
        }
    }
    
    /* Восстановление подчинённых detail объектов из корзины */
    @Override
    protected void restoreDetails(T ownerItem) {
        List<BaseDict> details = getLazyFacade().findDetailItems(ownerItem);
        if (details != null){
            details.stream().forEach(item -> getDetailBean().doRestoreItemFromTrash((T) item));
        }
    }
    
    /* Перемещение в корзину подчинённых объектов Владельца */
    @Override
    protected void moveDetailItemsToTrash(T ownerItem, Set<String> errors) {        
        List<BaseDict> details = getLazyFacade().findDetailItems(ownerItem);
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
        Long count = getLazyFacade().getCountDetails(item);
        if (count > 0) {
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public abstract BaseDetailsBean getDetailBean();

}
