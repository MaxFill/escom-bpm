
package com.maxfill.facade;

import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsStates;
import javax.ejb.Stateless;

/* Фасад для "Группы видов документов" */
@Stateless
public class DocTypeGroupsFacade extends BaseDictFacade<DocTypeGroups, DocTypeGroups, DocTypeGroupsLog, DocTypeGroupsStates> {

    public DocTypeGroupsFacade() {
        super(DocTypeGroups.class, DocTypeGroupsLog.class, DocTypeGroupsStates.class);
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
    public void replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
