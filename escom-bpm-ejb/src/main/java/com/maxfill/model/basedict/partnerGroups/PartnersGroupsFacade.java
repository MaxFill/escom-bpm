package com.maxfill.model.basedict.partnerGroups;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.partner.PartnersFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.partner.Partner_;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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

    /* Получение списка пользователей в группах */
    public List<Partner> findDetails(PartnerGroups group, int first, int pageSize, String sortField, String sortOrder, User currentUser) {
        first = 0;
        pageSize = configuration.getMaxResultCount();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Partner> cq = cb.createQuery(Partner.class);
        Root<Partner> root = cq.from(Partner.class);
        cq.select(root).distinct(true).where(root.get(Partner_.partnersGroupsList).in(group)).orderBy(cb.asc(root.get("name")));
        TypedQuery<Partner> query = em.createQuery(cq);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultStream().parallel()
                .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                .collect(Collectors.toList());
    }
         
    @Override
    public void create(PartnerGroups group) {
        super.create(group);
        if (CollectionUtils.isNotEmpty(group.getPartnersList())){
            group.getPartnersList().forEach(partner-> {
                partner.getPartnersGroupsList().add(group);
                em.merge(partner);
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
        PartnerGroups persistentPartnersGroups = em.find(PartnerGroups.class, partnersGroups.getId());
        List<Partner> partnersListOld = persistentPartnersGroups.getPartnersList();
        List<Partner> partnersListNew= partnersGroups.getPartnersList();
        List<Partner> attachedItemsListNew = new ArrayList<>();
        for (Partner partnersListNewItemsToAttach : partnersListNew) {
            partnersListNewItemsToAttach = em.getReference(partnersListNewItemsToAttach.getClass(), partnersListNewItemsToAttach.getId());
            attachedItemsListNew.add(partnersListNewItemsToAttach);
        }
        partnersListNew= attachedItemsListNew;
        partnersGroups.setPartnersList(partnersListNew);
        partnersGroups = em.merge(partnersGroups);
        for (Partner partnersListOldItems : partnersListOld) {
            if (!partnersListNew.contains(partnersListOldItems)) {
                partnersListOldItems.getPartnersGroupsList().remove(partnersGroups);
                em.merge(partnersListOldItems);
            }
        }
        for (Partner partnersListNewItems : partnersListNew) {
            if (!partnersListOld.contains(partnersListNewItems)) {
                partnersListNewItems.getPartnersGroupsList().add(partnersGroups);
                em.merge(partnersListNewItems);
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
