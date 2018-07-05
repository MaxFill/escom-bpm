package com.maxfill.escom.beans.filters;

import com.maxfill.model.filters.Filter;
import com.maxfill.model.filters.FiltersFacade;
import com.maxfill.escom.beans.core.BaseCardBean;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Фильтры объектов
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
    public FiltersFacade getFacade() {
        return itemsFacade;
    }

}
