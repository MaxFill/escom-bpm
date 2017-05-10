
package com.maxfill.facade;

import com.maxfill.model.folders.FolderLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import com.maxfill.utils.SysParams;
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
public class FoldersFacade extends BaseDictFacade<Folder, Folder, FolderLog> {
    @EJB
    private DocTypeFacade docTypeFacade;
    
    public FoldersFacade() {
        super(Folder.class, FolderLog.class);
    }        
     
    @Override
    public String getFRM_NAME() {
        return Folder.class.getSimpleName().toLowerCase();
    }
      
    @Override
    public void preparePasteItem(Folder pasteItem, BaseDict target){        
        pasteItem.setParent((Folder)target);
    } 
    
    /* Установка специфичных атрибутов при создании новой папки  */
    @Override
    public void setSpecAtrForNewItem(Folder folder, Map<String, Object> params) {
        folder.setModerator(folder.getAuthor());
        folder.setDocTypeDefault(docTypeFacade.find(SysParams.DEFAULT_DOC_TYPE_ID));                                        
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

    /**
     * Поиск папок по виду документа, указанного в дефолтном поле
     * @param docType
     * @return 
     */
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
    public Map<String, Integer> replaceItem(Folder oldItem, Folder newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
