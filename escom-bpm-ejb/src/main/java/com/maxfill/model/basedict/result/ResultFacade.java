package com.maxfill.model.basedict.result;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.Results;
import com.maxfill.model.basedict.user.User;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * Фасад для сущности "Результаты задачи"
 * @author maksim
 */
@Stateless
public class ResultFacade extends BaseDictFacade<Result, Result, ResultLog, ResultStates>{

    public ResultFacade() {
        super(Result.class, ResultLog.class, ResultStates.class);
    }

    @Override
    public int replaceItem(Result oldItem, Result newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_RESULT;
    }
    
    public List<Result> findTaskResults(Results item) {
        if (item == null) return new ArrayList<>();
        String json = item.getAvaibleResultsJSON();
        if (StringUtils.isEmpty(json)) return new ArrayList<>();
        
        Gson gson = new Gson();
        List<Integer> resultIds = gson.fromJson(json, List.class);

        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Result> cq = builder.createQuery(Result.class);
        Root<Result> root = cq.from(Result.class);             
        Predicate crit1 = root.get("id").in(resultIds);
        cq.select(root).where(builder.and(crit1));
        TypedQuery<Result> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
    
    /**
     * Отбирает все записи, кроме удалённых в корзину и не актуальных 
     * @param currentUser
     * @return 
     */ 
    @Override
    public List<Result> findAll(User currentUser) {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root c = cq.from(itemClass);        
        Predicate crit1 = builder.equal(c.get("actual"), true);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(orderBuilder(builder, c));
        TypedQuery query = getEntityManager().createQuery(cq);       
        return query.getResultList();
    }
}
