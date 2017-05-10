
package com.maxfill.escom.beans.partners.types;

import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Бин для видов контрагентов
 * @author mfilatov
 */
@ViewScoped
@Named
public class PartnerTypesCardBean extends BaseCardBean<PartnerTypes>{
    private static final long serialVersionUID = 7691003549203571832L;

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
   
    @Override
    public PartnerTypesFacade getItemFacade() {
        return partnerTypesFacade;
    }

    @Override
    protected void onAfterCreateItem(PartnerTypes item) {        
    }

    @Override
    public Class<PartnerTypes> getItemClass() {
        return PartnerTypes.class;
    }
}