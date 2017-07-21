
package com.maxfill.facade;

import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.model.partners.types.PartnerTypesLog;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.partners.types.PartnerTypesStates;
import javax.ejb.Stateless;

/* Типы организаций контрагента */
@Stateless
public class PartnerTypesFacade extends BaseDictFacade<PartnerTypes, PartnerTypes, PartnerTypesLog, PartnerTypesStates> {

    public PartnerTypesFacade() {
        super(PartnerTypes.class, PartnerTypesLog.class, PartnerTypesStates.class);
    }

    @Override
    public String getFRM_NAME() {
        return PartnerTypes.class.getSimpleName().toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PARTNER_TYPES;
    }

    @Override
    public void replaceItem(PartnerTypes oldItem, PartnerTypes newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
