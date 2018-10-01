package com.maxfill.model.basedict.remark;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.Process;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.Map;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author maksim
 */
@Stateless
public class RemarkFacade extends BaseDictFacade<Remark, Doc, RemarkLog, RemarkStates>{

    public RemarkFacade() {
        super(Remark.class, RemarkLog.class, RemarkStates.class);
    }

    @Override
    public int replaceItem(Remark oldItem, Remark newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_REMARK;
    }
    
    @Override
    protected void setSpecAtrForNewItem(Remark item, Map<String, Object> params) {
        item.setDateChange(new Date());
    }
    
    /**
     * Удаление замечаний, связанных с процессом
     * @param process
     * @return 
     */
    public int removeRemarksByProcess(Process process){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Remark> cd = builder.createCriteriaDelete(Remark.class);
        Root root = cd.from(Remark.class);
        Predicate crit1 = builder.equal(root.get(Remark_.process), process);
        cd.where(crit1);
        Query query = getEntityManager().createQuery(cd);
        return query.executeUpdate();
    }
}
