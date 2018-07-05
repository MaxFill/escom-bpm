package com.maxfill.model.task.result;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.Results;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.task.result.ResultLog;
import com.maxfill.model.task.result.ResultStates;
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
    public Class<Result> getItemClass() {
        return Result.class;
    }

    @Override
    public int replaceItem(Result oldItem, Result newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_RESULT;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.RESULT.toLowerCase();
    }
    
    public List<Result> findTaskResults(Results item) {
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
        
}
