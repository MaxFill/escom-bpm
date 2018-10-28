package com.maxfill.escom.beans.partners.groups;

import com.maxfill.model.basedict.partnerGroups.PartnersGroupsFacade;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.basedict.partner.Partner;
import java.util.List;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/* Бин карточки "Группы контрагентов" */
@Named
@ViewScoped
public class PartnersGroupsCardBean extends BaseCardBeanGroups<PartnerGroups, Partner> {
    private static final long serialVersionUID = 4638462307270732308L;
    
    @EJB
    private PartnersGroupsFacade itemsFacade;

    @Override
    public PartnersGroupsFacade getFacade() {
        return itemsFacade;
    }

    @Override
    public List<Partner> getGroups(PartnerGroups group) {
        return group.getPartnersList();
    }
    
    /* Добавление контрагента в группу контрагентов */
    @Override
    protected void addItemInGroup(PartnerGroups partnerGroups, Partner partner) {
        if (partner == null || partnerGroups == null || partnerGroups.getId().equals(0)) return; 
        List<Partner> groups = getGroups(partnerGroups);
        if (!groups.contains(partner)){
            groups.add(partner);
        } 
        onItemChange();        
    }

}