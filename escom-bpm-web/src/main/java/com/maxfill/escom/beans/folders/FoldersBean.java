package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.rights.Rights;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Папки документов" */

@Named(value = "foldersBean")
@SessionScoped
public class FoldersBean extends BaseTreeBean<Folder, Folder> {
    private static final long serialVersionUID = 2678662239530806110L;
    private static final Integer ROOT_FOLDER_ID = 0;
    
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private DocFacade docFacade;     

    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item); //получаем свои права 
        }
        
        if (item.getParent() != null) {
            return getRightItem(item.getParent()); //получаем права от родительской группы
        }                     
        
        return getDefaultRights(item);
    }
    
    @Override
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        tree.setExpanded(true);       
        //To do! тут нужно получать видимо права для тек. пользователя?      
        Folder rootFolder = getItemFacade().find(ROOT_FOLDER_ID);
        if (rootFolder == null){
            throw new NullPointerException("RootFolder null in make tree metod!"); 
        }
        Rights docRight = (Rights) JAXB.unmarshal(new StringReader(rootFolder.getXmlAccessChild()), Rights.class);
        setDefaultRightsChilds(docRight);
        settingRightForChild(rootFolder, docRight); 
        addNode(tree, rootFolder);
        return tree;
    }

    /* Добавление узла в дерево при формировании дерева */
    public TreeNode addNode(TreeNode parentNode, BaseDict folder) {
        TreeNode resultNode = null;

        if (preloadCheckRightView(folder)) { //проверяем право на просмотр папки текущему пользователю
            //актуализируем права документов папки
            makeRightForChilds((Folder)folder); //получаем права документов для текущей папки
            
            TreeNode newNode;
            synchronized(this){
                newNode = new DefaultTreeNode("tree", folder, parentNode);
            }

            //получаем и рекурсивно обрабатываем дочерние папки этой папки
            getItemFacade().findChilds((Folder)folder)
                    .stream()
                    .forEach(folderChild -> addNode(newNode, folderChild));
            
            resultNode = newNode;
        }
        return resultNode;
    }
    
    /* Формирование содержимого контента папки   */ 
    @Override
    public List<BaseDict> makeGroupContent(Folder folder, Integer viewMode) {
        Rights docRights = getDefaultRightsChilds();
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент дочерние папки
        List<Folder> folders = getItemFacade().findChilds((Folder)folder);        
        folders.stream()
                .forEach(fl -> addFolderInCnt(fl, cnt)
        );        
        //загружаем в контент документы
        List<Doc> docs = getDetailBean().getItemFacade().findItemByOwner(folder);
        docs.stream().
                forEach(doc -> addDocInCnt(doc, cnt, docRights)
        );
        return cnt;
    }
    
    /* Добавляет папку в контент  */ 
    private void addFolderInCnt(BaseDict folder, List<BaseDict> cnts) {
        makeRightForChilds((Folder)folder);
        cnts.add(folder);
    }

    /* Добавляет документ в контент  */ 
    public void addDocInCnt(BaseDict doc, List<BaseDict> cnts, Rights defDocRight) {
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(getAccessMask(doc.getState(), rd, currentUser)); //получаем маску доступа для текущего пользователя  
        cnts.add(doc);
    }          
       
    /* Действия перед удалением папки  */
    @Override
    protected void preDeleteItem(Folder folder) {       
        folder.getDetailItems().clear();
        docFacade.deleteDocFromFolder(folder);        
    }

    @Override
    protected void initAddLayoutOptions(LayoutOptions layoutOptions){
        super.initAddLayoutOptions(layoutOptions);
        
        LayoutOptions centerSouth = new LayoutOptions();
        centerSouth.addOption("size", "10%");
        LayoutOptions childCenterOptions = layoutOptions.getCenterOptions().getChildOptions();
        childCenterOptions.setSouthOptions(centerSouth);
    }

    /* Формирует число ссылок на folder в связанных объектах  */
    @Override
    public void doGetCountUsesItem(Folder folder,  Map<String, Integer> rezult){
        rezult.put("Documents", folder.getDocsList().size());
        rezult.put("Folders", folder.getFoldersList().size());
    }    
    
    /* Проверка возможности удаления Папки */
    @Override
    protected void checkAllowedDeleteItem(Folder folder, Set<String> errors){
        super.checkAllowedDeleteItem(folder, errors);
    }  
    
    @Override
    public void preparePasteItem(Folder pasteItem, BaseDict target){        
        pasteItem.setParent((Folder)target);
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
    public Class<Folder> getItemClass() {
        return Folder.class;
    }

    @Override
    public Class<Folder> getOwnerClass() {
        return null;
    }
}