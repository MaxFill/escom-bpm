
package com.maxfill.facade;

import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.DocTypeLog;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folders;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *  Фасад для сущности "Виды документов"
 * @author mfilatov
 */
@Stateless
public class DocTypeFacade extends BaseDictFacade<DocType, DocTypeGroups, DocTypeLog> {

    public DocTypeFacade() {
        super(DocType.class, DocTypeLog.class);
    }  

    @Override
    public String getFRM_NAME() {
        return DocType.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(DocType pasteItem, BaseDict target, Set<String> errors){      
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS_TYPES;
    }

    /**
     * Замена вида документа в связанных объектах
     * @param oldItem
     * @param newItem
     * @return 
     */
    @Override
    public Map<String, Integer> replaceItem(DocType oldItem, DocType newItem) {
        Map<String, Integer> rezultMap = new HashMap<>();
        rezultMap.put("Documents", replaceDocTypeInDocs(oldItem, newItem));
        rezultMap.put("Folders", replaceDocTypeInFolders(oldItem, newItem));
        return rezultMap;
    }    
            
    /**
     * Замена вида документа в документах
     * @param oldItem
     * @param newItem
     * @return 
     */
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
    
    /**
     * Замена вида документа в папках (дефолтное значение для новых документов)
     * @param oldItem
     * @param newItem
     * @return 
     */
    private int replaceDocTypeInFolders(DocType oldItem, DocType newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Folders> update = builder.createCriteriaUpdate(Folders.class);    
        Root root = update.from(Folders.class);  
        update.set("docTypeDefault", newItem);
        Predicate predicate = builder.equal(root.get("docTypeDefault"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
