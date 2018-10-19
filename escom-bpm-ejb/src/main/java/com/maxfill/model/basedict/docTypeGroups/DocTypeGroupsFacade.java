package com.maxfill.model.basedict.docTypeGroups;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.docType.DocTypeFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.docType.DocType_;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.user.User;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.List;

/* Фасад для сущности "Группы видов документов" */
@Stateless
public class DocTypeGroupsFacade extends BaseDictFacade<DocTypeGroups, DocTypeGroups, DocTypeGroupsLog, DocTypeGroupsStates>{

    @EJB
    private DocTypeFacade docTypeFacade;

    public DocTypeGroupsFacade() {
        super(DocTypeGroups.class, DocTypeGroupsLog.class, DocTypeGroupsStates.class);
    }    

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
            return getActualRightChildItem((DocTypeGroups) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return docTypeFacade.getDefaultRights();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOC_TYPE_GROUP;
    }

    @Override
    public List<BaseDict> findDetailItems(DocTypeGroups owner){
        em.getEntityManagerFactory().getCache().evict(DocType.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<DocType> cq = builder.createQuery(DocType.class);
        Root<DocType> c = cq.from(DocType.class);
        Predicate crit = builder.equal(c.get(DocType_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query query = em.createQuery(cq);       
        return query.getResultList();
    }

    /**
     * Замена группы вида документов на другую
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        int count = replaceDocTypeGroupInDocTypes(oldItem, newItem);
        return count;
    }

    /**
     * Замена группы видов документов в видах документов
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceDocTypeGroupInDocTypes(DocTypeGroups oldItem, DocTypeGroups newItem) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<DocType> update = builder.createCriteriaUpdate(DocType.class);
        Root root = update.from(DocType.class);
        update.set(DocType_.owner, newItem);
        Predicate predicate = builder.equal(root.get(DocType_.owner), oldItem);
        update.where(predicate);
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }
}
