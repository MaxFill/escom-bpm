package com.maxfill.model.basedict.procTempl;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.processType.ProcessType;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Шаблоны процессов"
 */
@Stateless
public class ProcessTemplFacade extends BaseDictFacade<ProcTempl, ProcessType, ProcTemplLog, ProcTemplStates>{

    public ProcessTemplFacade() {
        super(ProcTempl.class, ProcTemplLog.class, ProcTemplStates.class);
    }
    
    /**
     * Сброс у всех шаблонов признак "Основной шаблон" кроме указанного
     * @param procTempl 
     */
    public void clearDefaultTemplate(ProcTempl procTempl){
        ProcessType owner = procTempl.getOwner();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<ProcTempl> update = builder.createCriteriaUpdate(ProcTempl.class);
        Root root = update.from(ProcTempl.class);
        update.set(ProcTempl_.isDefault, Boolean.FALSE);
        Predicate crit1 = builder.equal(root.get("owner"), owner);
        Predicate crit2 = builder.notEqual(root.get("id"), procTempl.getId());
        Predicate crit3 = builder.equal(root.get(ProcTempl_.isDefault), Boolean.TRUE);
        update.where(builder.and(crit1, crit2, crit3));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }            
    
    @Override
    public void detectParentOwner(ProcTempl procTempl, BaseDict parent, BaseDict target){
        procTempl.setOwner((ProcessType)target);
        procTempl.setParent(null);
    }
    
    @Override
    public int replaceItem(ProcTempl oldItem, ProcTempl newItem) {       
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PROC_TEMPL;
    }

    @Override
    public void setSpecAtrForNewItem(ProcTempl procTempl, Map<String, Object> params) {
        procTempl.setName(procTempl.getPath());
    }
}
