
package com.maxfill.facade;

import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.model.partners.types.PartnerTypesLog;
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
 * Фасад для Типы организаций контрагента
 * @author mfilatov
 */
@Stateless
public class PartnerTypesFacade extends BaseDictFacade<PartnerTypes, PartnerTypes, PartnerTypesLog> {

    public PartnerTypesFacade() {
        super(PartnerTypes.class, PartnerTypesLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return PartnerTypes.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(PartnerTypes pasteItem, BaseDict target, Set<String> errors){
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PARTNER_TYPES;
    }

    @Override
    public Map<String, Integer> replaceItem(PartnerTypes oldItem, PartnerTypes newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
