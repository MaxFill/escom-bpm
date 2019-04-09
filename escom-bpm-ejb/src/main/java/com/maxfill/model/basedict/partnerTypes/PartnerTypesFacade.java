package com.maxfill.model.basedict.partnerTypes;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.model.basedict.partner.Partner_;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * Фасад для сущности "Виды контрагентов"
 */
@Stateless
public class PartnerTypesFacade extends BaseDictFacade<PartnerTypes, PartnerTypes, PartnerTypesLog, PartnerTypesStates>{

    public PartnerTypesFacade() {
        super(PartnerTypes.class, PartnerTypesLog.class, PartnerTypesStates.class);
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
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Partner> update = builder.createCriteriaUpdate(Partner.class);
        Root root = update.from(Partner.class);
        update.set(Partner_.type, newItem);
        Predicate predicate = builder.equal(root.get(Partner_.type), oldItem);
        update.where(predicate);
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }
    
    /* ПОИСК */
    
    /* Дополнения при выполнении поиска через форму поиска */
    @Override
    protected void addLikePredicates(Root root, List<Predicate> predicates,  CriteriaBuilder cb, Map<String, Object> paramLIKE){
        String param = (String) paramLIKE.get("name");
        if (StringUtils.isNotBlank(param)) {
            predicates.add(
                    cb.or(
                            cb.like(root.<String>get("name"), param),
                            cb.like(root.<String>get("fullName"), param)
                    )
            );
            paramLIKE.remove("name");
        }
        super.addLikePredicates(root, predicates, cb, paramLIKE);
    }
}
