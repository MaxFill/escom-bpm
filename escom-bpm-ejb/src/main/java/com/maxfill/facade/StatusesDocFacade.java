
package com.maxfill.facade;

import com.maxfill.model.statuses.StatusesDocLog;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.statuses.StatusesDocStates;
import javax.ejb.Stateless;

/* Cтатусы документов */
@Stateless
public class StatusesDocFacade extends BaseDictFacade<StatusesDoc, StatusesDoc, StatusesDocLog, StatusesDocStates> {

    public StatusesDocFacade() {
        super(StatusesDoc.class, StatusesDocLog.class, StatusesDocStates.class);
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.STATUS_DOCS.toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS_STATUS;
    }

    @Override
    public void replaceItem(StatusesDoc oldItem, StatusesDoc newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
