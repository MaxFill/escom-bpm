package com.maxfill.escom.beans;

import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T , O > {
    private static final long serialVersionUID = -2983279513793115056L;    
    
    private Rights defaultRightsChilds;
    private BaseExplBean ownerBean;
    private BaseExplBean detailBean;           
        
    /* ДЕРЕВО */    
    
    /* Формирование детального списка для группы  */
    public List<BaseDict> makeGroupContent(T group){   
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
        if (sessionBean.preloadCheckRightView(item)) {

            TreeNode newNode;

            synchronized (this) {
                newNode = new DefaultTreeNode("tree", item, parentNode);
                //newNode.setExpanded(true);
            }

            List<T> childs = getItemFacade().findChilds(item);
            childs.stream()
                    .forEach(itemChild -> addItemInTree(newNode, itemChild)
            );
            rezNode = newNode;
        }
        return rezNode;
    }       
    
    protected void clearDetail(T item){
        getDetailBean().clearOwner(item);
    }
    
    /* СЕЛЕКТОР */
    
    /* СЕЛЕКТОР: обработка действия выбора элемента в дереве   */
    public String onSelectTreeItem() {        
        BaseDict selectItem = explorerBean.getCurrentItem();
        if (selectItem == null || !explorerBean.isCanSelectedItem(selectItem)){
            return "";
        }        
        List<BaseDict> groups = new ArrayList<>();
        groups.add(explorerBean.getCurrentItem());
        return doClose(groups);
    }
    
    /* СЕЛЕКТОР: обработка действия множественного выбора для дерева */
    public String onSelectTreeItems() {
        List<TreeNode> nodes = Arrays.asList(getSelectedNodes());        
        List<BaseDict> groups = nodes.stream().map(node -> (O) node.getData()).collect(Collectors.toList());
        return doClose(groups);
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
