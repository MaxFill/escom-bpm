package com.maxfill.model.process.templates;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.process.types.ProcessType;
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
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<ProcTempl> update = builder.createCriteriaUpdate(ProcTempl.class);
        Root root = update.from(ProcTempl.class);
        update.set(ProcTempl_.isDefault, Boolean.FALSE);
        Predicate crit1 = builder.equal(root.get("owner"), owner);
        Predicate crit2 = builder.notEqual(root.get("id"), procTempl.getId());
        Predicate crit3 = builder.equal(root.get(ProcTempl_.isDefault), Boolean.TRUE);
        update.where(builder.and(crit1, crit2, crit3));
        Query query = getEntityManager().createQuery(update);
        query.executeUpdate();
    }
    
    /* *** *** */
    
    @Override
    public Class<ProcTempl> getItemClass() {
        return ProcTempl.class;
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
    public String getFRM_NAME() {
        return DictObjectName.PROCESS_TEMPLATE.toLowerCase();
    }
    
}
