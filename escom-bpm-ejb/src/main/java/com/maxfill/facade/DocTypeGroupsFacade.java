
package com.maxfill.facade;

import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Группы видов документов"
 * @author mfilatov
 */
@Stateless
public class DocTypeGroupsFacade extends BaseDictFacade<DocTypeGroups, DocTypeGroups, DocTypeGroupsLog> {

    public DocTypeGroupsFacade() {
        super(DocTypeGroups.class, DocTypeGroupsLog.class);
    }  

    @Override
    public String getFRM_NAME() {
        return DocTypeGroups.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(DocTypeGroups pasteItem, BaseDict target, Set<String> errors){
        pasteItem.setParent((DocTypeGroups)target);              
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOC_TYPE_GROUP;
    }

    @Override
    public Map<String, Integer> replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
