package com.maxfill.escom.beans.filters;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.filters.Filter;
import com.maxfill.facade.FiltersFacade;
import com.maxfill.escom.beans.BaseCardBean;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Фильтры объектов
 * @author mfilatov
 */
@Named
@ViewScoped
public class FiltersCardBean extends BaseCardBean<Filter> {
    private static final long serialVersionUID = 4085679062161705562L;
                 
    @EJB
    private FiltersFacade itemsFacade;   
    
    public FiltersCardBean() {  
    }

    @Override
    public FiltersFacade getItemFacade() {
        return itemsFacade;
    }
    
}
