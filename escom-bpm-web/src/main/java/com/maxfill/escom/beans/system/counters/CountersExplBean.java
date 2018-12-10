package com.maxfill.escom.beans.system.counters;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.numeratorPattern.counter.CounterFacade;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import com.maxfill.model.basedict.user.UserFacade;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;

@ViewScoped
@Named
public class CountersExplBean extends LazyLoadBean<Counter>{
    private static final long serialVersionUID = 5004791826027575029L;
    
    @EJB
    private CounterFacade counterFacade;
    
    private Company company;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params) {
        company = staffFacade.findCompanyForStaff(getCurrentStaff());
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_COUNTERS;
    }    
    
    public void onDeleteItem(Counter item){
        deleteItem(item);
    }
    
    public void onNumberEdit(RowEditEvent event) {         
        if(selected != null) {
            counterFacade.edit(selected);
        }
    }
     
    public void onRefresh(){
        refreshLazyData();
        PrimeFaces.current().ajax().update("mainFRM:itemsTbl");
    }
      
    @Override
    protected Map<String, Object> makeFilters(Map filters) {
        filters.put("company", company);
        return filters;
    }
    
    /* GETS & SETS */

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }    
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("CountersNumbers");
    }

    @Override
    protected BaseLazyFacade getLazyFacade() {
        return counterFacade;
    }
}