package com.maxfill.model.basedict.process.options;

import com.google.gson.Gson;
import com.maxfill.facade.BaseFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 * Фасад для сущности "Условие процесса"
 * @author maksim
 */
@Stateless
public class RunOptionsFacade extends BaseFacade<RunOptions>{
    
    public RunOptionsFacade() {
        super(RunOptions.class);
    }
    
    public List<RunOptions> findAll() {                        
        getEntityManager().getEntityManagerFactory().getCache().evict(RunOptions.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(RunOptions.class);
        Root root = cq.from(RunOptions.class);        
        cq.select(root);
        cq.orderBy(builder.asc(root.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    public List<RunOptions> getBaseRunOptions(){
        List<RunOptions> runOptions = new ArrayList<>();
        runOptions.add(find(0));
        return runOptions;
    }
    
    /**
     * Формирует список ОпцийЗапускаПроцесса из ВидаПроцесса
     * @param item
     * @return 
     */
    public List<RunOptions> findRunOptionsByProcType(ProcessType item) {
        if (item == null) return new ArrayList<>();
        
        String json = getRunOptionsJSON(item);
        if (StringUtils.isEmpty(json)) return new ArrayList<>();
        
        Gson gson = new Gson();
        List<Integer> resultIds = gson.fromJson(json, List.class);

        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(RunOptions.class);
        Root root = cq.from(RunOptions.class);             
        Predicate crit1 = root.get("id").in(resultIds);
        cq.select(root).where(builder.and(crit1));
        TypedQuery<RunOptions> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
    
    private String getRunOptionsJSON(ProcessType processType){        
        if (processType.isInheritRunOptions() == true && processType.getParent() != null){
            return getRunOptionsJSON(processType.getParent());
        }
        return processType.getRunOptionsJSON();
    }
}

