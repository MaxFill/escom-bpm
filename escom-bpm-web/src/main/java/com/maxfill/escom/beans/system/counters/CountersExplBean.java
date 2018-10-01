package com.maxfill.escom.beans.system.counters;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.basedict.numeratorPattern.counter.CounterFacade;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.CellEditEvent;

@ViewScoped
@Named
public class CountersExplBean extends BaseViewBean{
    private static final long serialVersionUID = 5004791826027575029L;

    private List<Counter> counters;        
    private Counter selected; 
    
    @EJB
    private CounterFacade counterFacade;

    @Override
    public String getFormName() {
        return DictFrmName.FRM_COUNTERS;
    }
    
    public void onDeleteItem(Counter item){
        counterFacade.remove(item);
        counters.remove(item);
    }
    
    public void onNumberEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
         
        if(!Objects.equals(newValue, oldValue) && selected != null) {
            counterFacade.edit(selected);
        }
    }
     
    public void refresh(){
        counters = null;
    }
         
    /* GETS & SETS */

    public List<Counter> getCounters() {
        if (counters == null){            
            counters = counterFacade.findAll();            
        }
        return counters;
    }

    public Counter getSelected() {
        return selected;
    }
    public void setSelected(Counter selected) {
        this.selected = selected;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("CountersNumbers");
    }
}
