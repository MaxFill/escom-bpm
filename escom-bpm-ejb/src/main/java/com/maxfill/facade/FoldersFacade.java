
package com.maxfill.facade;

import com.maxfill.model.folders.FoldersLog;
import com.maxfill.model.folders.Folders;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.rights.Rights;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Папки
 * @author Maxim
 */
@Stateless
public class FoldersFacade extends BaseDictFacade<Folders, Folders, FoldersLog> {
    @EJB
    private DocTypeFacade docTypeFacade;
    
    public FoldersFacade() {
        super(Folders.class, FoldersLog.class);
    }        
     
    @Override
    public String getFRM_NAME() {
        return Folders.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(Folders pasteItem, BaseDict target, Set<String> errors){        
        pasteItem.setParent((Folders)target);
        doPaste(pasteItem, errors);
    }
    
    /* Установка специфичных атрибутов при создании новой папки  */
    @Override
    public void setSpecAtrForNewItem(Folders folder, Map<String, Object> params) {
        User currentUser = folder.getAuthor();
        folder.setModerator(currentUser);
        folder.setDocTypeDefault(docTypeFacade.find(SysParams.DEFAULT_DOC_TYPE_ID));
        
        Rights rights = getRightItemFromParent(folder);
        makeRightItem(folder, rights, currentUser);
        
        Folders parentFolder = (Folders) folder.getParent();
        Rights parentRights = getRightItemFromOwner(parentFolder);
        settingRightForChild(folder, parentRights);  //сохраняем права документов в папке
    }
    
    /* Возвращает все папки */
    public List<Folders> findAllFolders(){ 
        getEntityManager().getEntityManagerFactory().getCache().evict(Folders.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folders> cq = builder.createQuery(Folders.class);
        Root<Folders> c = cq.from(Folders.class);
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);      
        return q.getResultList(); 
    }    

    /**
     * Поиск папок по виду документа, указанного в дефолтном поле
     * @param docType
     * @return 
     */
    public List<Folders> findFoldersByDocTyper(DocType docType){
        getEntityManager().getEntityManagerFactory().getCache().evict(Folders.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folders> cq = builder.createQuery(Folders.class);
        Root<Folders> c = cq.from(Folders.class);        
        Predicate crit1 = builder.equal(c.get("docTypeDefault"), docType);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
       
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FOLDERS;
    }

    @Override
    public Map<String, Integer> replaceItem(Folders oldItem, Folders newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
