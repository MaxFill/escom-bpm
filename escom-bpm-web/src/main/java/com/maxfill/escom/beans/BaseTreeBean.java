package com.maxfill.escom.beans;

import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.List;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T , O > {
    private static final long serialVersionUID = -2983279513793115056L;    
    
    private Rights defaultRightsChilds;
    private BaseExplBean ownerBean;
    private BaseExplBean detailBean;           
            
    /* Формирование детального списка для группы  */
    public List<BaseDict> makeGroupContent(T group, Integer viewMode){   
        List<BaseDict> details = getDetailBean().getItemFacade().findDetailItems(group);
        return details;
    }
        
    /* Формирование дерева */
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        List<T> sourceTreeItems = (List<T>) getRootItems();
        sourceTreeItems.stream()
                .filter(treeItem -> treeItem.getParent() == null)
                .forEach(treeItem -> addItemInTree(tree, treeItem));
        return tree;
    }
    
    /* Формирует список объектов нулевого уровня (parent = 0)  */
    protected List<BaseDict> getRootItems() {
        List<BaseDict> rootItems = prepareItems(getItemFacade().findDetailItems(null));
        return rootItems;
    }
    
    /* Добавление узла в дерево при его формировании  */
    protected TreeNode addItemInTree(TreeNode parentNode, BaseDict item) {           
        TreeNode rezNode = null;
        if (preloadCheckRightView(item)) {
            TreeNode newNode = new DefaultTreeNode("tree", item, parentNode);

            List<T> childs = getItemFacade().findChilds(item);
            childs.stream()
                    .forEach(itemChild -> addItemInTree(newNode, itemChild)
            );
            rezNode = newNode;
        }
        return rezNode;
    }              
        
    /* GETS & SETS */
    
    public Rights getDefaultRightsChilds() {
        return defaultRightsChilds;
    }
    public void setDefaultRightsChilds(Rights childRights) {
        this.defaultRightsChilds = childRights;
    }
    
    @Override
    public BaseExplBean getDetailBean() {
        return (BaseExplBean)detailBean;
    }    
    public void setDetailBean(BaseExplBean detailBean) {
        this.detailBean = detailBean;
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return ownerBean;
    }
    public void setOwnerBean(BaseExplBean ownerBean) {
        this.ownerBean = ownerBean;
    }  
}
