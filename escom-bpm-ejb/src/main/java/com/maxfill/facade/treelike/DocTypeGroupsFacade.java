package com.maxfill.facade.treelike;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.DocType_;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsStates;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import com.maxfill.model.users.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    public Class<DocTypeGroups> getItemClass() {
        return DocTypeGroups.class;
    }

    @Override
    public String getFRM_NAME() {
        return DocTypeGroups.class.getSimpleName().toLowerCase();
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
    public List<BaseDict> findAllDetailItems(DocTypeGroups owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(DocType.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<DocType> cq = builder.createQuery(DocType.class);
        Root<DocType> c = cq.from(DocType.class);
        Predicate crit = builder.equal(c.get(DocType_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    @Override
    public void replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
