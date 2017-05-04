package com.maxfill.escom.beans.filters;

import com.maxfill.model.filters.Filters;
import com.maxfill.facade.FiltersFacade;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Фильтры объектов
 * @author mfilatov
 */
@Named
@ViewScoped
public class FiltersCardBean extends BaseCardBean<Filters> {
    private static final long serialVersionUID = 4085679062161705562L;
                 
    @EJB
    private FiltersFacade itemsFacade;   
    
    public FiltersCardBean() {  
    }              

    @Override
    public FiltersFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected void onAfterCreateItem(Filters item) {        
    }


}
