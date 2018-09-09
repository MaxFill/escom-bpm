package com.maxfill.model.partners.groups;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.partners.PartnersFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.partners.groups.PartnerGroupsLog;
import com.maxfill.model.partners.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.partners.groups.PartnerGroupsStates;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;

/* Группы контрагентов */
@Stateless
public class PartnersGroupsFacade extends BaseDictFacade<PartnerGroups, PartnerGroups, PartnerGroupsLog, PartnerGroupsStates>{

    @EJB
    private PartnersFacade partnersFacade;

    public PartnersGroupsFacade() {
        super(PartnerGroups.class, PartnerGroupsLog.class, PartnerGroupsStates.class);
    }

    @Override
    public Class<PartnerGroups> getItemClass() {
        return PartnerGroups.class;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PARTNER_GROUP.toLowerCase();
    }
         
    @Override
    public void create(PartnerGroups group) {
        super.create(group);
        if (CollectionUtils.isNotEmpty(group.getPartnersList())){
            group.getPartnersList().forEach(partner-> {
                partner.getPartnersGroupsList().add(group);
                getEntityManager().merge(partner);
            });
        }
    }

    /* Получение прав доступа для иерархического справочника */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительской группы
        }

        return getDefaultRights(item);
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((PartnerGroups) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return partnersFacade.getDefaultRights();
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
                getEntityManager().merge(partnersListOldItems);
            }
        }
        for (Partner partnersListNewItems : partnersListNew) {
            if (!partnersListOld.contains(partnersListNewItems)) {
                partnersListNewItems.getPartnersGroupsList().add(partnersGroups);
                getEntityManager().merge(partnersListNewItems);
            }
        }
    }  

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_PARTNERS;
    }

    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> addParams) {
        predicates.add(builder.notEqual(root.get("id"), 0));
    }

    /**
     * Выполняет замену Группы контрагента на другую Группу
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(PartnerGroups oldItem, PartnerGroups newItem) {
        int count = 0;
        return count;
    }
}
