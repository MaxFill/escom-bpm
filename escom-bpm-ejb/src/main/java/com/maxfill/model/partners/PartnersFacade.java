package com.maxfill.model.partners;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.partners.groups.PartnersGroupsFacade;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.folders.Folder_;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.partners.PartnersLog;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.BaseDict;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.partners.PartnerStates;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class PartnersFacade extends BaseDictFacade<Partner, PartnerGroups, PartnersLog, PartnerStates>{

    @EJB
    private PartnersGroupsFacade partnersGroupsFacade;
    
    public PartnersFacade() {
        super(Partner.class, PartnersLog.class, PartnerStates.class);
    }

    @Override
    public Class<Partner> getItemClass() {
        return Partner.class;
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
    
    @Override
    protected void detectParentOwner(Partner partner, BaseDict owner){
        partner.setOwner(null);
        partner.setParent(null);
        if (owner == null) return;
        if (!partner.getPartnersGroupsList().contains((PartnerGroups)owner)){
            partner.getPartnersGroupsList().add((PartnerGroups)owner);            
        } 
    }
    
    /* Установка специфичных атрибутов контрагента при его создании */
    @Override
    public void setSpecAtrForNewItem(Partner partner, Map<String, Object> params){
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(partner, numeratorPattern, null, new Date());
        partner.setCode(number);
    }
    
    /* Возвращает обновлённый список контрагентов для группы контрагентов  */
    @Override
    public List<Partner> findActualDetailItems(PartnerGroups group){
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
    public int replaceItem(Partner oldItem, Partner newItem) {
        int count = replacePartnerInDocs(oldItem, newItem);
        count = count + replacePartnerInFolders(oldItem, newItem);
        return count;
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
        update.set(Doc_.partner, newItem);
        Predicate predicate = builder.equal(root.get(Doc_.partner), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Замена контрагента в папках
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replacePartnerInFolders(Partner oldItem, Partner newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);
        Root root = update.from(Folder.class);
        update.set(Folder_.partnerDefault, newItem);
        Predicate predicate = builder.equal(root.get(Folder_.partnerDefault), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}