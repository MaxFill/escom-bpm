package com.maxfill.escom.beans.folders;

import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.rights.Rights;
import com.maxfill.utils.SysParams;
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

/* Папки документов */

@Named(value = "foldersBean")
@ViewScoped
public class FoldersBean extends BaseTreeBean<Folder, Folder> {
    private static final long serialVersionUID = 2678662239530806110L;
    private static final String BEAN_NAME = "foldersBean";
    private static final Integer ROOT_FOLDER_ID = 0;
    
    @EJB
    private FoldersFacade foldersFacade;
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }    

    /* ДЕРЕВО: формирование дерева папок  */
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
        sessionBean.settingRightForChild(rootFolder, docRight); 
        addNode(tree, rootFolder);
        return tree;
    }

    /* ДЕРЕВО: добавление узла в дерево при формировании дерева */
    public TreeNode addNode(TreeNode parentNode, BaseDict folder) {
        TreeNode resultNode = null;

        if (sessionBean.preloadCheckRightView(folder)) { //проверяем право на просмотр папки текущему пользователю
            //актуализируем права документов папки
            sessionBean.makeRightForChilds((Folder)folder); //получаем права документов для текущей папки
            
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

    /* *** КОНТЕНТ *** */
    
    /* КОНТЕНТ: формирование контента папки   */ 
    @Override
    public List<BaseDict> makeGroupContent(Folder folder) {
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
    
    /* КОНТЕНТ: добавляет папку в контент  */ 
    private void addFolderInCnt(BaseDict folder, List<BaseDict> cnts) {
        sessionBean.makeRightForChilds((Folder)folder);
        cnts.add(folder);
    }

    /* КОНТЕНТ: добавляет документ в контент  */ 
    public void addDocInCnt(BaseDict doc, List<BaseDict> cnts, Rights defDocRight) {
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(sessionBean.getAccessMask(doc.getState(), rd, currentUser)); //получаем маску доступа для текущего пользователя  
        cnts.add(doc);
    }
    
    /* ПРАВА ДОСТУПА */
    
    /* Проверка доступа на удаление контента папки    */
    public boolean isHaveRightDeleteContent(BaseDict content){
        if (content instanceof Folder){            
            return isHaveRightDelete((Folder)content);
        } else {
            return getDetailBean().isHaveRightDelete(content);
        }
    }    
       
    /* Действия перед удалением папки  */
    @Override
    protected void preDeleteItem(Folder folder) {        
       clearDetail(folder);
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
    
    /* *** GETS & SETS *** */    

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