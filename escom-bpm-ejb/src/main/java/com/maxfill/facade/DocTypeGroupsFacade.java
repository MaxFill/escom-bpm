
package com.maxfill.facade;

import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.BaseDict;
import java.util.Map;
import javax.ejb.Stateless;

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
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOC_TYPE_GROUP;
    }

    @Override
    public Map<String, Integer> replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void preparePasteItem(DocTypeGroups pasteItem, BaseDict target){
        pasteItem.setParent((DocTypeGroups)target);
    }
}
