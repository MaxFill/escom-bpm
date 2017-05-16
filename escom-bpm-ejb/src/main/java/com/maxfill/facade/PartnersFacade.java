
package com.maxfill.facade;

import com.maxfill.model.partners.Partner;
import com.maxfill.model.partners.PartnersLog;
import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.numPuttern.NumeratorPattern;
import java.util.Date;
import java.util.HashMap;
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
import org.apache.commons.lang3.StringUtils;

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
    
    @Override
    public boolean addItemToGroup(Partner partner, BaseDict targetGroup){
        if (partner == null || targetGroup == null){
            return false;
        }
        PartnerGroups group = (PartnerGroups)targetGroup;
        if (!partner.getPartnersGroupsList().contains(group)){
            partner.getPartnersGroupsList().add(group);
            edit(partner);            
            group.getPartnersList().add(partner);
            return true;
        }
        return false;
    }    

    @Override
    protected void detectParentOwner(Partner partner, BaseDict owner){
        partner.setOwner(null);
        partner.setParent(null);
        if (!partner.getPartnersGroupsList().contains((PartnerGroups)owner)){
            partner.getPartnersGroupsList().add((PartnerGroups)owner);            
        } 
    }
    
    /**
     * Ищет контрагентов по code исключая ID указанного контрагента
     * @param code
     * @param partnerId
     * @return true если есть нет таких объектов и false если есть такие объекты
     */
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
    
    /**
     * Ищет контрагентов по наименованию и типу исключая ID указанного контрагента
     * @param name
     * @param type
     * @param partnerId
     * @return true если есть нет таких объектов и false если есть такие объекты
     */
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
    
    /**
     * Ищет контрагентов по типу 
     * @param type
     * @return true если есть нет таких объектов и false если есть такие объекты
     */
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
    
    /**
     * Возвращает обновлённый список контрагентов для группы контрагентов
     * @param group
     * @return 
     */
    @Override
    public List<Partner> findDetailItems(PartnerGroups group){
        PartnerGroups freshGroup = partnersGroupsFacade.find(group.getId());
        List<Partner> detailItems = freshGroup.getPartnersList().stream()
                .filter(partner -> !partner.isDeleted() && partner.isActual())
                .collect(Collectors.toList());        
        return detailItems;
    }
    
    /* Установка специфичных атрибутов контрагента при его создании */
    @Override
    public void setSpecAtrForNewItem(Partner item, Map<String, Object> params){
        String counterName = getFRM_NAME();
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(item, counterName, numeratorPattern, null, new Date());
        item.setCode(number);
    }
    
    /* Контрагента нужно копировать в случае если он вставляется не в группу или если в ту же группу. В других случаях только добавление ссылки */
    @Override
    public boolean isNeedCopyOnPaste(Partner sourceItem, BaseDict recipient){
        if (!(recipient instanceof PartnerGroups)){
            return true;
        }
        if (sourceItem.getPartnersGroupsList().contains((PartnerGroups)recipient)){
            return true;
        }
        return false;
    }
    
    @Override
    public void preparePasteItem(Partner pasteItem, BaseDict recipient){        
        if (!isNeedCopyOnPaste(pasteItem, recipient)){
            addItemToGroup(pasteItem, recipient);
        }
    }     
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PARTNERS;
    }

    @Override
    public Map<String, Integer> replaceItem(Partner oldItem, Partner newItem) {
        Map<String, Integer> rezultMap = new HashMap<>();
        rezultMap.put("Documents", replacePartnerInDocs(oldItem, newItem));
        return rezultMap;
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