package com.maxfill.escom.beans.partners.groups;

import com.maxfill.facade.PartnersGroupsFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Группы контрагентов
 * @author mfilatov
 */
@Named
@ViewScoped
public class PartnersGroupsCardBean extends BaseCardBean<PartnerGroups> {
    private static final long serialVersionUID = 4638462307270732308L;
    
    @EJB
    private PartnersGroupsFacade itemsFacade;       

    @Override
    public PartnersGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected void onAfterCreateItem(PartnerGroups item) {        
    }

    @Override
    public Class<PartnerGroups> getItemClass() {
        return PartnerGroups.class;
    }
}