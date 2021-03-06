package com.maxfill.model.basedict.partner;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.partnerGroups.PartnersGroupsFacade;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.partnerTypes.PartnerTypes;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc_;
import com.maxfill.model.basedict.partner.numerator.PartnerNumerator;
import com.maxfill.model.basedict.user.User;
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
import org.apache.commons.lang3.StringUtils;

/* Контрагенты */
@Stateless
public class PartnersFacade extends BaseDictFacade<Partner, PartnerGroups, PartnersLog, PartnerStates>{

    @EJB
    private PartnersGroupsFacade partnersGroupsFacade;
    @EJB
    private PartnerNumerator partnerNumerator;
    
    public PartnersFacade() {
        super(Partner.class, PartnersLog.class, PartnerStates.class);
    }

    public List<Partner> findByCodeExclId(String code, Integer partnerId){
        em.getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("code"), code);
        Predicate crit2 = builder.notEqual(c.get("id"), partnerId);
        cq.select(c).where(builder.and(crit1, crit2));        
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }
    
    public List<Partner> findByNameAndTypeExclId(String name, PartnerTypes type, Integer partnerId){
        em.getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("name"), name);
        Predicate crit2 = builder.equal(c.get("type"), type);
        Predicate crit3 = builder.notEqual(c.get("id"), partnerId);
        cq.select(c).where(builder.and(crit1, crit2, crit3));        
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }
    
    public List<Partner> findByType(PartnerTypes type){
        em.getEntityManagerFactory().getCache().evict(Partner.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Partner> cq = builder.createQuery(Partner.class);
        Root<Partner> c = cq.from(Partner.class);        
        Predicate crit1 = builder.equal(c.get("type"), type);
        cq.select(c).where(builder.and(crit1)); 
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }
    
    @Override
    protected void dublicateCheckAddCriteria(CriteriaBuilder builder, Root<Partner> root, List<Predicate> criteries, Partner partner){
        boolean setInnKpp = false;
        
        if (StringUtils.isNotBlank(partner.getInn())){
            criteries.add(builder.equal(root.get(Partner_.inn), partner.getInn()));
            setInnKpp = true;
        }
        if (StringUtils.isNotBlank(partner.getKpp())){
            criteries.add(builder.equal(root.get(Partner_.kpp), partner.getKpp()));
            setInnKpp = true;
        }
        
        if (!setInnKpp){
            criteries.add(builder.equal(root.get("name"), partner.getName()));
        }
        
        if (partner.getId() != null){
            criteries.add(builder.notEqual(root.get("id"), partner.getId()));
        }
    } 
    
    @Override
    protected void detectParentOwner(Partner partner, BaseDict parent, BaseDict owner){
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
        String number = partnerNumerator.doRegistrNumber(partner, null, new Date());
        partner.setCode(number);
    }
    
    /* Возвращает обновлённый список контрагентов для группы контрагентов  */
    @Override
    public List<Partner> findActualDetailItems(PartnerGroups group, int first, int pageSize, String sortField, String sortOrder, User currentUser){
        PartnerGroups freshGroup = partnersGroupsFacade.find(group.getId());
        return freshGroup.getPartnersList().stream()
                .filter(partner -> !partner.isDeleted() && partner.isActual())
                .collect(Collectors.toList());        
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
        CriteriaBuilder builder = em.getCriteriaBuilder(); 
        CriteriaUpdate<Doc> update = builder.createCriteriaUpdate(Doc.class);    
        Root root = update.from(Doc.class);  
        update.set(Doc_.partner, newItem);
        Predicate predicate = builder.equal(root.get(Doc_.partner), oldItem);
        update.where(predicate);
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Замена контрагента в папках
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replacePartnerInFolders(Partner oldItem, Partner newItem){
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);
        Root root = update.from(Folder.class);
        update.set(root.get("partnerDefault"), newItem);
        Predicate predicate = builder.equal(root.get("partnerDefault"), oldItem);
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
    
    /**
     * Выполняет проверку сначала на принадлежность checkedPartner к группе exampleGroup 
     * или какой-нибудь дочерней от exampleGroup (при условии что searcheInSubGroups = true)
     * и если принадлежность существует, то выполняется проверка на наличие у пользователя прав доступа к checkedPartner
     * @param checkedPartner проверяемый объект
     * @param exampleGroup искомая группа (критерий поиска)
     * @param checkedOwner поле всегда пустое (null) т.к. у пользователя нет владельца
     * @param user пользователь, для которого проверяются права к checkedUser
     * @param searcheInSubGroups если true, то поиск выполнять также и во вложенных группах
     * @return true - если объект доступен, false - если не доступен
     */
    @Override
    protected boolean checkPreloadItem(BaseDict exampleGroup, BaseDict checkedOwner, Partner checkedPartner, User user, boolean searcheInSubGroups){        
        if (checkPartnerInGroup(checkedPartner, (PartnerGroups)exampleGroup, searcheInSubGroups)){
            return preloadCheckRightView(checkedPartner, user);
        } else {            
            return false;
        }
    } 
    
    /**
     * Выполняет проверку принадлежности user к examplUserGroup
     * @param partner проверяемый пользователь
     * @param examplUserGroup искомая группа (критерий поиска)
     * @param searcheInSubGroups если true, то поиск выполнять так же и во вложенных группах
     * @return 
     */
    public boolean checkPartnerInGroup(final Partner partner, final PartnerGroups examplUserGroup, boolean searcheInSubGroups){
        return partner.getPartnersGroupsList().stream().parallel()
                .filter(group->checkPartnerGroup(examplUserGroup, group, searcheInSubGroups))
                .findFirst()
                .isPresent();                        
    }
    
    private boolean checkPartnerGroup(final PartnerGroups examplUserGroup, PartnerGroups checkedGroup, boolean searcheInSubGroups){
        if (checkedGroup == null) return false;
        if (checkedGroup.equals(examplUserGroup)){
            return true;
        } else {
            return searcheInSubGroups ? checkPartnerGroup(examplUserGroup, checkedGroup.getParent(), searcheInSubGroups) : false;
        }
    }
}