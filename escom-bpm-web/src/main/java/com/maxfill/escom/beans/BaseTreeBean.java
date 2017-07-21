package com.maxfill.escom.beans;

import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import org.apache.commons.collections.CollectionUtils;

/* Реализация методов для древовидных объектов (подразделения, группы и т.п.) */
public abstract class BaseTreeBean<T extends BaseDict, O extends BaseDict> extends BaseExplBean<T , O > {
    private static final long serialVersionUID = -2983279513793115056L;                
        
    /* Формирование прав дочерних объектов */
    public Rights makeRightForChilds(T item){
        Rights childRights = getRightForChild(item);
        item.setRightForChild(childRights);
        return childRights;
    }
    
    /* Получение прав для дочерних объектов */
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;
        
        if (!item.isInheritsAccessChilds()) {
            return getActualRightChildItem((T)item);
        } 

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }
                    
        if (item.getOwner() != null) {
            return getRightForChild(item.getOwner()); //получаем права от владельца
        }

        return getDetailBean().getDefaultRights();
    }
    
    /* Получение актуальных прав дочерних объектов от объекта */
    private Rights getActualRightChildItem(T item) {
        Rights actualRight = null;
        byte[] compressXML = item.getAccessChild();
        if (compressXML != null && compressXML.length >0){
            try {
                String accessXML = EscomUtils.decompress(compressXML);            
                StringReader access = new StringReader(accessXML);         
                actualRight = (Rights) JAXB.unmarshal(access, Rights.class);
            } catch (IOException ex) {
                Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return actualRight;
    }
    
    /* Установка специфичных атрибутов при создании объекта  */ 
    @Override
    public void setSpecAtrForNewItem(T item, Map<String, Object> params) {
        item.setInheritsAccessChilds(true);
        makeRightForChilds(item);
    }
    
    /* Базовый метод формирования детального списка для группы  */
    public List<BaseDict> makeGroupContent(BaseDict group, Integer viewMode){   
        List<BaseDict> cnt = new ArrayList();
        List<BaseDict> details = getDetailBean().getItemFacade().findDetailItems(group);
        details.stream().forEach(item -> addDetailItemInContent(item, cnt));
        return cnt;
    }
    
    /* Добавление подчинённого объекта в контент */
    protected void addDetailItemInContent(BaseDict item, List<BaseDict> cnt){
        if (getDetailBean().preloadCheckRightView(item)){
            cnt.add(item);
        }
    }
    
    /* Добавление  дочернего объекта в контент */
    protected void addChildItemInContent(T item, List<BaseDict> cnt){
        if (preloadCheckRightView(item)){
            cnt.add(item);
        }
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
    protected List<T> getRootItems() {
        List<T> rootItems = prepareItems(getItemFacade().findDetailItems(null));
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
    
    /* Удаление подчинённых (связанных) объектов */
    @Override
    protected void deleteDetails(T item) {
        if (item.getDetailItems() != null) {
            item.getDetailItems().stream().forEach(child -> getDetailBean().deleteItem((T) child));
        }
    }
    
    /* Восстановление подчинённых detail объектов из корзины */
    @Override
    protected void restoreDetails(T ownerItem) {
        if (ownerItem.getDetailItems() != null){
            ownerItem.getDetailItems().stream()
                    .forEach(item -> getDetailBean().doRestoreItemFromTrash((T) item)
            );
        }
    }
    
    /* Перемещение в корзину подчинённых объектов Владельца (ownerItem) */
    @Override
    protected void moveDetailItemsToTrash(T ownerItem, Set<String> errors) {                
        if (ownerItem.getDetailItems() != null){
            ownerItem.getDetailItems().stream()
                    .forEach(detail -> getDetailBean().moveToTrash((T) detail, errors)
            );
        }
    }
    
    @Override
    protected void checkAllowedDeleteItem(T item, Set<String> errors) {
        if (CollectionUtils.isNotEmpty(item.getDetailItems()) || CollectionUtils.isNotEmpty(item.getChildItems())) {
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }
        
    @Override
    public abstract BaseExplBean getDetailBean();

}
