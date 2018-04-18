package com.maxfill.escom.beans.partners.types;

import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.escom.beans.core.BaseCardBean;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Контролер карточки "Виды контрагентов"
 * @author mfilatov
 */
@ViewScoped
@Named
public class PartnerTypesCardBean extends BaseCardBean<PartnerTypes>{
    private static final long serialVersionUID = 7691003549203571832L;

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
   
    @Override
    public PartnerTypesFacade getFacade() {
        return partnerTypesFacade;
    }

}