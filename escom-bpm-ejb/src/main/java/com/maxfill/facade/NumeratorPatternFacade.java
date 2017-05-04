
package com.maxfill.facade;

import com.maxfill.model.numPuttern.NumeratorPatternLog;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Шаблоны нумераторв
 * @author mfilatov
 */
@Stateless
public class NumeratorPatternFacade extends BaseDictFacade<NumeratorPattern, NumeratorPattern, NumeratorPatternLog> {

    public NumeratorPatternFacade() {
        super(NumeratorPattern.class, NumeratorPatternLog.class);
    }
    
    @Override
    public String getFRM_NAME() {
        return NumeratorPattern.class.getSimpleName().toLowerCase();
    }
 
    @Override
    public void pasteItem(NumeratorPattern pasteItem, BaseDict target, Set<String> errors){
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_NUMERATOR;
    }

    @Override
    public Map<String, Integer> replaceItem(NumeratorPattern oldItem, NumeratorPattern newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
