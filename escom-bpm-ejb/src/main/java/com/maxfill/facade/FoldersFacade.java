package com.maxfill.facade;

import com.maxfill.model.BaseDict;
import com.maxfill.model.folders.FolderLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.folders.FolderStates;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Папки  */
@Stateless
public class FoldersFacade extends BaseDictFacade<Folder, Folder, FolderLog, FolderStates> {
    @EJB
    private DocTypeFacade docTypeFacade;
    
    public FoldersFacade() {
        super(Folder.class, FolderLog.class, FolderStates.class);
    }

    @Override
    public Class<Folder> getItemClass() {
        return Folder.class;
    }

    @Override
    public String getFRM_NAME() {
        return Folder.class.getSimpleName().toLowerCase();
    }      
       
    @Override
    public void setSpecAtrForNewItem(Folder folder, Map<String, Object> params) {
        folder.setModerator(folder.getAuthor());
        folder.setDocTypeDefault(docTypeFacade.find(SysParams.DEFAULT_DOC_TYPE_ID));
    }

    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительской группы
        }

        return getDefaultRights(item);
    }

    /* Возвращает все папки */
    public List<Folder> findAllFolders(){ 
        getEntityManager().getEntityManagerFactory().getCache().evict(Folder.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folder> cq = builder.createQuery(Folder.class);
        Root<Folder> c = cq.from(Folder.class);
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);      
        return q.getResultList(); 
    }    

    /* Поиск папок по виду документа, указанного в дефолтном поле  */
    public List<Folder> findFoldersByDocTyper(DocType docType){
        getEntityManager().getEntityManagerFactory().getCache().evict(Folder.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folder> cq = builder.createQuery(Folder.class);
        Root<Folder> c = cq.from(Folder.class);        
        Predicate crit1 = builder.equal(c.get("docTypeDefault"), docType);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }

    /*
     * Проверка возможности добавлять документы в указанной папке, указанному пользователю
     * @return true - можно, false - нельзя
     */
    public boolean checkRightAddDetail(Folder folder, User user){
        actualizeRightItem(folder, user);
        return isHaveRightAddChild(folder);
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FOLDERS;
    }

    @Override
    public void replaceItem(Folder oldItem, Folder newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
