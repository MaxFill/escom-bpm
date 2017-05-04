package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folders;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.rights.Rights;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Папки
 *
 * @author Maxim
 */

@Named(value = "foldersBean")
@ViewScoped
public class FoldersBean extends BaseTreeBean<Folders, Folders> {
    private static final long serialVersionUID = 2678662239530806110L;
    private static final String BEAN_NAME = "foldersBean";
    private static final Integer ROOT_FOLDER_ID = 0;
    
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private DocTypeFacade docTypeFacade;
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }    

    /**
     * ДЕРЕВО: формирование дерева папок
     *
     * @return
     */
    @Override
    public TreeNode makeTree() {
        TreeNode tree = new DefaultTreeNode("Root", null);
        tree.setExpanded(true);       
        //To do! тут нужно получать видимо права для тек. пользователя?      
        Folders rootFolder = getItemFacade().find(ROOT_FOLDER_ID);
        if (rootFolder == null){
            throw new NullPointerException("RootFolder null in make tree metod!"); 
        }
        Rights docRight = (Rights) JAXB.unmarshal(new StringReader(rootFolder.getAccessDocs()), Rights.class);
        setChildRights(docRight);
        getItemFacade().settingRightForChild(rootFolder, docRight); 
        addNode(tree, rootFolder, docRight);
        return tree;
    }

    /* ДЕРЕВО: добавление узла в дерево при формировании дерева */
    public TreeNode addNode(TreeNode parentNode, BaseDict folder, Rights parRightDoc) {
        TreeNode resultNode = null;

        if (sessionBean.preloadCheckRightView(folder)) { //проверяем право на просмотр папки текущему пользователю
            Rights folderRight = folder.getRightItem();
            //актуализируем права документов папки
            Rights rightDoc = makeRightChild((Folders)folder, parRightDoc); //получаем права документов для текущей папки
            folder.setRightForChild(rightDoc); //сохраняем права документов в папку
            
            TreeNode newNode;
            synchronized(this){
                newNode = new DefaultTreeNode("tree", folder, parentNode);
            }

            //получаем и рекурсивно обрабатываем дочерние папки этой папки
            getItemFacade().findChilds((Folders)folder)
                    .stream()
                    .forEach(folderChild -> addNode(newNode, folderChild, rightDoc));
            
            resultNode = newNode;
        }
        return resultNode;
    }

    /* *** КОНТЕНТ *** */
    
    /**
    * КОНТЕНТ: формирование контента папки
     * @param folder
     * @return 
    */ 
    @Override
    public List<BaseDict> makeGroupContent(Folders folder) {
        Rights docRights = getChildRights();
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент дочерние папки
        List<Folders> folders = getItemFacade().findChilds((Folders)folder);        
        folders.stream()
                .forEach(fl -> addFolderInCnt(fl, cnt, docRights)
        );        
        //загружаем в контент документы
        List<Doc> docs = getDetailBean().getItemFacade().findItemByOwner(folder);
        docs.stream().
                forEach(doc -> addDocInCnt(doc, cnt, docRights)
        );
        return cnt;
    }
    
    /**
     * КОНТЕНТ: добавляет папку в контент
     * @param folder
     * @param cnts
     * @param defDocRight
     * @return 
     */ 
    private void addFolderInCnt(BaseDict folder, List<BaseDict> cnts, Rights defDocRight) {
        Rights rights = makeRightChild((Folders)folder, defDocRight);
        getItemFacade().settingRightForChild((Folders)folder, rights); //сохраняем права к документам
        cnts.add(folder);
    }

    /**
     * КОНТЕНТ: добавляет документ в контент
     * @param doc
     * @param cnts
     * @param defDocRight
     */ 
    public void addDocInCnt(BaseDict doc, List<BaseDict> cnts, Rights defDocRight) {
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(getItemFacade().getAccessMask(doc.getState(), rd, currentUser)); //получаем маску доступа для текущего пользователя  
        cnts.add(doc);
    }
    
    /* *** ПРАВА ДОСТУПА *** */
    
    /**
     * Проверка доступа на удаление контента папки
     * @param content
     * @return 
     */
    public boolean isHaveRightDeleteContent(BaseDict content){
        if (content instanceof Folders){            
            return isHaveRightDelete((Folders)content);
        } else {
            return getDetailBean().isHaveRightDelete(content);
        }
    }

    /* *** ПРАВА ДОСТУПА К ДОКУМЕНТАМ ***  */
    
    //TODO Права доступа из родителя получаются некорректно!!!
    //получение кэшированных прав документов папки (используется при загрузке дерева) без подгрузки прав из базы
    public Rights makeRightChild(Folders folder, Rights parentRight) {
        Rights docRight;
        if (folder == null || folder.isInherits() || folder.getAccessDocs().isEmpty()) { //если права наследуются или у папки нет прав для документов
            docRight = parentRight; //то берём права от родителя 
        } else { //если права не наследуются
            docRight = (Rights) JAXB.unmarshal(new StringReader(folder.getAccessDocs()), Rights.class); //Демаршаллинг прав из строки!
        }
        return docRight;
    }    

    /* *** Cоздание и редактирование папки *** */
    
    /**
     * Действия перед удалением папки
     * @param folder 
     */
    @Override
    protected void preDeleteItem(Folders folder) {        
       clearDetail(folder);
    }

    @Override
    protected void postDeleteItem(Folders folder) {
        TreeNode delNode = EscomBeanUtils.findTreeNode(explorerBean.getTree(), folder);
        TreeNode parNode = delNode.getParent();
        parNode.getChildren().remove(delNode);
        Folders parentFolder = folder.getParent();        

        TreeNode selected = getSelectedNode();
        if (delNode.equals(selected)) { //если удалена текущая папка
            explorerBean.makeSelectedGroup(parentFolder); //установка новой текушей папки
            parNode.setSelected(true);
        }
    }  

    @Override
    protected void initAddLayoutOptions(LayoutOptions layoutOptions){
        super.initAddLayoutOptions(layoutOptions);
        
        LayoutOptions centerSouth = new LayoutOptions();
        centerSouth.addOption("size", "10%");
        LayoutOptions childCenterOptions = layoutOptions.getCenterOptions().getChildOptions();
        childCenterOptions.setSouthOptions(centerSouth);
    }

    @Override
    public FoldersFacade getItemFacade() {
        return foldersFacade;
    }

    /**
     * Формирует число ссылок на folder в связанных объектах 
     * @param folder
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(Folders folder,  Map<String, Integer> rezult){
        rezult.put("Documents", folder.getDocsList().size());
        rezult.put("Folders", folder.getFoldersList().size());
    }    
    
    /**
     * Проверка возможности удаления Папки
     * @param folder
     */
    @Override
    protected void checkAllowedDeleteItem(Folders folder, Set<String> errors){
        super.checkAllowedDeleteItem(folder, errors);
    }  
    
    /* *** GETS & SETS *** */    

    @Override
    public List<Folders> getGroups(Folders item) {
        return null;
    }       

    @Override
    public Class<Folders> getItemClass() {
        return Folders.class;
    }

    @Override
    public Class<Folders> getOwnerClass() {
        return null;
    }
}