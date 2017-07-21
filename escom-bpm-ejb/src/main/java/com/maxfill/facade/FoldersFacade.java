
package com.maxfill.facade;

import com.maxfill.model.folders.FolderLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.folders.FolderStates;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Папки  */
@Stateless
public class FoldersFacade extends BaseDictFacade<Folder, Folder, FolderLog, FolderStates> {
    
    public FoldersFacade() {
        super(Folder.class, FolderLog.class, FolderStates.class);
    }        
     
    @Override
    public String getFRM_NAME() {
        return Folder.class.getSimpleName().toLowerCase();
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

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FOLDERS;
    }

    @Override
    public void replaceItem(Folder oldItem, Folder newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
