
package com.maxfill.facade;

import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.partners.groups.PartnerGroupsLog;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;

/**
 *
 * @author Maxim
 */
@Stateless
public class PartnersGroupsFacade extends BaseDictFacade<PartnerGroups, PartnerGroups, PartnerGroupsLog> {

    public PartnersGroupsFacade() {
        super(PartnerGroups.class, PartnerGroupsLog.class);
    }    

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PARTNER_GROUP.toLowerCase();
    }
         
    @Override
    public void create(PartnerGroups group) {
        getEntityManager().persist(group);
        List<Partner> partners = group.getPartnersList();
        for (Partner partner : partners) {
            partner.getPartnersGroupsList().add(group);
            getEntityManager().merge(partner);
        }
    }    
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(PartnerGroups group){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(group.getChildItems());
        return dependency;
    }
    
    @Override
    public void edit(PartnerGroups partnersGroups) { 
        PartnerGroups persistentPartnersGroups = getEntityManager().find(PartnerGroups.class, partnersGroups.getId());
        List<Partner> partnersListOld = persistentPartnersGroups.getPartnersList();
        List<Partner> partnersListNew= partnersGroups.getPartnersList();
        List<Partner> attachedItemsListNew = new ArrayList<>();
        for (Partner partnersListNewItemsToAttach : partnersListNew) {
            partnersListNewItemsToAttach = getEntityManager().getReference(partnersListNewItemsToAttach.getClass(), partnersListNewItemsToAttach.getId());
            attachedItemsListNew.add(partnersListNewItemsToAttach);
        }
        partnersListNew= attachedItemsListNew;
        partnersGroups.setPartnersList(partnersListNew);
        partnersGroups = getEntityManager().merge(partnersGroups);
        for (Partner partnersListOldItems : partnersListOld) {
            if (!partnersListNew.contains(partnersListOldItems)) {
                partnersListOldItems.getPartnersGroupsList().remove(partnersGroups);
                partnersListOldItems = getEntityManager().merge(partnersListOldItems);
            }
        }
        for (Partner partnersListNewItems : partnersListNew) {
            if (!partnersListOld.contains(partnersListNewItems)) {
                partnersListNewItems.getPartnersGroupsList().add(partnersGroups);
                partnersListNewItems = getEntityManager().merge(partnersListNewItems);
            }
        }
    }  

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_PARTNERS;
    }

    @Override
    public Map<String, Integer> replaceItem(PartnerGroups oldItem, PartnerGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }        
        
    @Override
    public void preparePasteItem(PartnerGroups pasteItem, BaseDict target){        
        pasteItem.setParent((PartnerGroups)target);
    }
}
