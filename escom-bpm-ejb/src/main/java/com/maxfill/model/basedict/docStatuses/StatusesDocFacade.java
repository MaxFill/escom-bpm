package com.maxfill.model.basedict.docStatuses;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.statusesDoc.StatusesDocLog;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.statusesDoc.StatusesDocStates;
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
