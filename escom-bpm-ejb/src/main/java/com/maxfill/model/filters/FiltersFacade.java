package com.maxfill.model.filters;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/* Фасад для сущности "Фильтры" */
@Stateless
public class FiltersFacade extends BaseDictFacade<Filter, Filter, FilterLog, FiltersStates>{

    public FiltersFacade() {
        super(Filter.class, FilterLog.class, FiltersStates.class);
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FILTERS;
    }

    @Override
    public int replaceItem(Filter oldItem, Filter newItem) {
        return 0;
    }

    public List<Filter> findChildsFilters(Filter parent, Metadates metadate){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Filter> cq = builder.createQuery(Filter.class);
        Root<Filter> root = cq.from(Filter.class);
        Predicate crit1 = builder.or(builder.equal(root.get("metadates"), metadate), builder.isNull(root.get("metadates")));
        Predicate crit2 = builder.equal(root.get("deleted"), false);
        Predicate crit3 = builder.equal(root.get("parent"), parent);
        cq.select(root).where(builder.and(crit1, crit2, crit3));
        //cq.orderBy(builder.asc(root.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* К фильтрам пока права не настроены, поэтому всегда true */
    @Override
    protected Boolean preloadCheckRightView(BaseDict item, User user) {    
        return Boolean.TRUE;
    }
}
