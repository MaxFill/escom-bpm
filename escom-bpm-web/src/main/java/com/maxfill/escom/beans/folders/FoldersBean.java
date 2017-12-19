package com.maxfill.escom.beans.folders;

import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.rights.Rights;
import com.maxfill.escom.utils.EscomBeanUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/* Сервисный бин "Папки документов" */

@Named(value = "foldersBean")
@SessionScoped
public class FoldersBean extends BaseTreeBean<Folder, Folder> {
    private static final long serialVersionUID = 2678662239530806110L;
    private static final Integer ROOT_FOLDER_ID = 0;
    
    @Inject
    private DocBean docBean;
    
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private DocFacade docFacade;
        
    @Override
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        tree.setExpanded(true);       
        //To do! тут нужно получать видимо права для тек. пользователя?      
        Folder rootFolder = getItemFacade().find(ROOT_FOLDER_ID);
        if (rootFolder == null){
            throw new NullPointerException("RootFolder null in make tree metod!"); 
        }
        //makeRightForChilds(rootFolder); //получаем права документов для текущей папки
        addNode(tree, rootFolder);
        return tree;
    }

    /* Добавление узла в дерево при формировании дерева */
    public TreeNode addNode(TreeNode parentNode, BaseDict folder) {
        TreeNode resultNode = null;

        if (getItemFacade().preloadCheckRightView(folder, currentUser)) { //проверяем право на просмотр папки текущему пользователю
            //актуализируем права документов папки
            
            TreeNode newNode = new DefaultTreeNode("tree", folder, parentNode);

            //получаем и рекурсивно обрабатываем дочерние папки этой папки
            getItemFacade().findActualChilds((Folder)folder)
                    .stream()
                    .forEach(folderChild -> addNode(newNode, folderChild));
            
            resultNode = newNode;
        }
        return resultNode;
    }
    
    /* Формирование содержимого контента папки   */ 
    @Override
    public List<BaseDict> makeGroupContent(BaseDict folder, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент дочерние папки
        List<Folder> folders = getItemFacade().findActualChilds((Folder)folder);        
        folders.stream().forEach(fl -> addChildItemInContent(fl, cnt));        
        //загружаем в контент документы
        List<Doc> docs = getDetailBean().getItemFacade().findItemByOwner(folder);
        docs.stream().forEach(doc -> addDetailItemInContent(doc, cnt));
        return cnt;
    }
       
    /* Действия перед удалением папки  */
    @Override
    protected void preDeleteItem(Folder folder) {       
        folder.getDetailItems().clear();
        docFacade.deleteDocFromFolder(folder);        
    }

    /* Формирует число ссылок на folder в связанных объектах  */
    @Override
    public void doGetCountUsesItem(Folder folder,  Map<String, Integer> rezult){
        rezult.put("Documents", folder.getDetailItems().size());
        rezult.put("Folders", folder.getChildItems().size());
    }    
    
    /* Проверка возможности удаления Папки */
    @Override
    protected void checkAllowedDeleteItem(Folder folder, Set<String> errors){
        //super.checkAllowedDeleteItem(folder, errors);
    }  
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Folder folder){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(docFacade.findAllDetailItems(folder));
        dependency.add(foldersFacade.findAllChilds(folder));
        return dependency;
    }
    
    @Override
    public void preparePasteItem(Folder pasteItem, Folder sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        if (target == null){
            target = sourceItem.getParent();
        }
        pasteItem.setParent((Folder)target);
    } 
    
    public String getTypeName(Folder folder){
        return EscomBeanUtils.getBandleLabel("FolderType");
    }
        
    /* GETS & SETS */    

    @Override
    public FoldersFacade getItemFacade() {
        return foldersFacade;
    }
    
    @Override
    public List<Folder> getGroups(Folder item) {
        return null;
    }

    @Override
    public Class<Folder> getOwnerClass() {
        return null;
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return docBean;
    }
}