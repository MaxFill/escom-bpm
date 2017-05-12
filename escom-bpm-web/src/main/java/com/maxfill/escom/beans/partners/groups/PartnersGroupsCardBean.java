package com.maxfill.escom.beans.partners.groups;

import com.maxfill.facade.PartnersGroupsFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.partners.Partner;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Группы контрагентов
 * @author mfilatov
 */
@Named
@ViewScoped
public class PartnersGroupsCardBean extends BaseCardBeanGroups<PartnerGroups, Partner> {
    private static final long serialVersionUID = 4638462307270732308L;
    
    @EJB
    private PartnersGroupsFacade itemsFacade;       

    @Override
    public PartnersGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected void afterCreateItem(PartnerGroups item) {        
    }

    @Override
    public Class<PartnerGroups> getItemClass() {
        return PartnerGroups.class;
    }

    @Override
    public List<Partner> getGroups(PartnerGroups group) {
        return group.getPartnersList();
    }
    
    /* Добавление контрагента в группу контрагентов */
    @Override
    protected void addItemInGroup(PartnerGroups partnerGroups, Partner partner) {
        if (partner == null){
            return;
        }
        List<Partner> groups = getGroups(partnerGroups);
        if (!groups.contains(partner)){
            groups.add(partner);
        } 
        if(!addGroups.contains(partner)){
            addGroups.add(partner);
        }
        setIsItemChange(Boolean.TRUE);        
    }
}