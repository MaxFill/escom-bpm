
package com.maxfill.facade;

import com.maxfill.model.partners.Partner;
import com.maxfill.model.partners.PartnersLog;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.dictionary.DictMetadatesIds;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Контрагенты */
@Stateless
public class PartnersFacade extends BaseDictFacade<Partner, PartnerGroups, PartnersLog> {

    @EJB
    private PartnersGroupsFacade partnersGroupsFacade;    
    
    public PartnersFacade() {
        super(Partner.class, PartnersLog.class);
    }
        
    @Override
    public String getFRM_NAME() {
        return Partner.class.getSimpleName().toLowerCase();
    }          
    
    public List<Partner> findByCodeExclId(String code, Integer partnerId){
        getEntityManager().getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("code"), code);
        Predicate crit2 = builder.notEqual(c.get("id"), partnerId);
        cq.select(c).where(builder.and(crit1, crit2));        
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    public List<Partner> findByNameAndTypeExclId(String name, PartnerTypes type, Integer partnerId){
        getEntityManager().getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("name"), name);
        Predicate crit2 = builder.equal(c.get("type"), type);
        Predicate crit3 = builder.notEqual(c.get("id"), partnerId);
        cq.select(c).where(builder.and(crit1, crit2, crit3));        
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    public List<Partner> findByType(PartnerTypes type){
        getEntityManager().getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("type"), type);
        cq.select(c).where(builder.and(crit1)); 
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Возвращает обновлённый список контрагентов для группы контрагентов  */
    @Override
    public List<Partner> findDetailItems(PartnerGroups group){
        PartnerGroups freshGroup = partnersGroupsFacade.find(group.getId());
        List<Partner> detailItems = freshGroup.getPartnersList().stream()
                .filter(partner -> !partner.isDeleted() && partner.isActual())
                .collect(Collectors.toList());        
        return detailItems;
    }         
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PARTNERS;
    }

    @Override
    public void replaceItem(Partner oldItem, Partner newItem) {
        replacePartnerInDocs(oldItem, newItem);
    }
    
    /**
     * Замена контрагента в документах
     * @param oldItem
     * @param newItem
     * @return 
     */
    private int replacePartnerInDocs(Partner oldItem, Partner newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Doc> update = builder.createCriteriaUpdate(Doc.class);    
        Root root = update.from(Doc.class);  
        update.set("partner", newItem);
        Predicate predicate = builder.equal(root.get("partner"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}