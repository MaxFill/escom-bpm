
package com.maxfill.facade;

import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.filters.Filters;
import com.maxfill.model.filters.FiltersLog;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author mfilatov
 */
@Stateless
public class FiltersFacade extends BaseDictFacade<Filters, Filters, FiltersLog> {

    public FiltersFacade() {
        super(Filters.class, FiltersLog.class);
    }
    
    @Override
    public String getFRM_NAME() {
        return Filters.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(Filters pasteItem, BaseDict target , Set<String> errors){            
        doPaste(pasteItem, errors);
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FILTERS;
    }

    @Override
    public Map<String, Integer> replaceItem(Filters oldItem, Filters newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
