
package com.maxfill.facade;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.filters.Filter;
import com.maxfill.model.filters.FilterLog;
import java.util.Map;
import javax.ejb.Stateless;

/**
 *
 * @author mfilatov
 */
@Stateless
public class FiltersFacade extends BaseDictFacade<Filter, Filter, FilterLog> {

    public FiltersFacade() {
        super(Filter.class, FilterLog.class);
    }
    
    @Override
    public String getFRM_NAME() {
        return Filter.class.getSimpleName().toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FILTERS;
    }

    @Override
    public Map<String, Integer> replaceItem(Filter oldItem, Filter newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
