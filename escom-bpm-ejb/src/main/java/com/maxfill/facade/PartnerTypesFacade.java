package com.maxfill.facade;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.partners.Partner_;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.model.partners.types.PartnerTypesLog;
import com.maxfill.model.partners.types.PartnerTypesStates;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Виды контрагентов"
 */
@Stateless
public class PartnerTypesFacade extends BaseDictFacade<PartnerTypes, PartnerTypes, PartnerTypesLog, PartnerTypesStates> {

    public PartnerTypesFacade() {
        super(PartnerTypes.class, PartnerTypesLog.class, PartnerTypesStates.class);
    }

    @Override
    public Class<PartnerTypes> getItemClass() {
        return PartnerTypes.class;
    }

    @Override
    public String getFRM_NAME() {
        return PartnerTypes.class.getSimpleName().toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PARTNER_TYPES;
    }

    /**
     * Замена Вида контрагента на другого в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(PartnerTypes oldItem, PartnerTypes newItem) {
       int count = replacePartnerTypeInPartners(oldItem, newItem);
       return count;
    }

    /**
     * Замена Вида контрагента в контрагентах
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replacePartnerTypeInPartners(PartnerTypes oldItem, PartnerTypes newItem) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Partner> update = builder.createCriteriaUpdate(Partner.class);
        Root root = update.from(Partner.class);
        update.set(Partner_.type, newItem);
        Predicate predicate = builder.equal(root.get(Partner_.type), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
