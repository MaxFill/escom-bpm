
package com.maxfill.facade;

import com.maxfill.model.statuses.StatusesDocLog;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для справочника статусов документов
 * @author mfilatov
 */
@Stateless
public class StatusesDocFacade extends BaseDictFacade<StatusesDoc, StatusesDoc, StatusesDocLog> {

    public StatusesDocFacade() {
        super(StatusesDoc.class, StatusesDocLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.DOC_STATUS.toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOCS_STATUS;
    }

    @Override
    public Map<String, Integer> replaceItem(StatusesDoc oldItem, StatusesDoc newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
