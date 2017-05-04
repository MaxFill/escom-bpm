
package com.maxfill.facade;

import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.partners.groups.PartnerGroupsLog;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.Partner;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
    public void pasteItem(PartnerGroups pasteItem, BaseDict target, Set<String> errors){        
        pasteItem.setParent((PartnerGroups)target);
        doPaste(pasteItem, errors);
    }
    
    @Override
    public void create(PartnerGroups partnersGroups) {
        getEntityManager().persist(partnersGroups);
        List<Partner> partnersListNew= partnersGroups.getPartnersList();
        for (Partner partnersListNewItems : partnersListNew) {
            partnersListNewItems.getPartnersGroupsList().add(partnersGroups);
            partnersListNewItems = getEntityManager().merge(partnersListNewItems);
        }
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
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
       
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_PARTNERS;
    }

    @Override
    public Map<String, Integer> replaceItem(PartnerGroups oldItem, PartnerGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
    
}
