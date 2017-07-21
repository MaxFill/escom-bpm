
package com.maxfill.facade;

import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.DocTypeLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.docs.docsTypes.DocTypeStates;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Фасад для сущности "Виды документов" */
@Stateless
public class DocTypeFacade extends BaseDictFacade<DocType, DocTypeGroups, DocTypeLog, DocTypeStates> {

    public DocTypeFacade() {
        super(DocType.class, DocTypeLog.class, DocTypeStates.class);
    }  

    @Override
    public String getFRM_NAME() {
        return DocType.class.getSimpleName().toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS_TYPES;
    }

    /* Замена вида документа в связанных объектах  */
    @Override
    public void replaceItem(DocType oldItem, DocType newItem) {
        replaceDocTypeInDocs(oldItem, newItem);
        replaceDocTypeInFolders(oldItem, newItem);
    }    
            
    /* Замена вида документа в документах */
    private int replaceDocTypeInDocs(DocType oldItem, DocType newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Doc> update = builder.createCriteriaUpdate(Doc.class);    
        Root root = update.from(Doc.class);  
        update.set("docType", newItem);
        Predicate predicate = builder.equal(root.get("docType"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
    /** Замена вида документа в папках (дефолтное значение для новых документов)  */
    private int replaceDocTypeInFolders(DocType oldItem, DocType newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);    
        Root root = update.from(Folder.class);  
        update.set("docTypeDefault", newItem);
        Predicate predicate = builder.equal(root.get("docTypeDefault"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
