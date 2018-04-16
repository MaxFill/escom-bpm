package com.maxfill.facade;

import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.statuses.StatusesDocLog;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.statuses.StatusesDocStates;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Cтатусы документов"
 */
@Stateless
public class StatusesDocFacade extends BaseDictFacade<StatusesDoc, StatusesDoc, StatusesDocLog, StatusesDocStates>{

    public StatusesDocFacade() {
        super(StatusesDoc.class, StatusesDocLog.class, StatusesDocStates.class);
    }

    @Override
    public Class<StatusesDoc> getItemClass() {
        return StatusesDoc.class;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.STATUS_DOCS.toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS_STATUS;
    }

    /**
     * Замена статуса документа на другой статус
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(StatusesDoc oldItem, StatusesDoc newItem) {
        return 0;
    }
    
}
