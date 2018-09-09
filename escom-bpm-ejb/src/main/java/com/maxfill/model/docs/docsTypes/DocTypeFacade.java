package com.maxfill.model.docs.docsTypes;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.folders.Folder_;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Фасад для сущности "Виды документов" */
@Stateless
public class DocTypeFacade extends BaseDictFacade<DocType, DocTypeGroups, DocTypeLog, DocTypeStates>{

    @EJB
    private DocTypeGroupsFacade docTypeGroupsFacade;

    @Override
    public Class<DocType> getItemClass() {
        return DocType.class;
    }

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

    /* Получение прав доступа для линейного, подчинённого справочника */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getOwner() != null) {
            Rights childRight = docTypeGroupsFacade.getRightForChild(item.getOwner()); //получаем права из спец.прав
            if (childRight != null){
                return childRight;
            }
        }
        return getDefaultRights(item);
    }

    /**
     * Замена вида документа в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(DocType oldItem, DocType newItem) {
        int count = replaceDocTypeInDocs(oldItem, newItem);
        count = count + replaceDocTypeInFolders(oldItem, newItem);
        return count;
    }    
            
    /* Замена вида документа в документах */
    private int replaceDocTypeInDocs(DocType oldItem, DocType newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Doc> update = builder.createCriteriaUpdate(Doc.class);    
        Root root = update.from(Doc.class);  
        update.set(Doc_.docType, newItem);
        Predicate predicate = builder.equal(root.get(Doc_.docType), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
    /** Замена вида документа в папках (дефолтное значение для новых документов)  */
    private int replaceDocTypeInFolders(DocType oldItem, DocType newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);    
        Root root = update.from(Folder.class);  
        update.set(Folder_.docTypeDefault, newItem);
        Predicate predicate = builder.equal(root.get(Folder_.docTypeDefault), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
