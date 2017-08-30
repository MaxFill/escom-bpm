package com.maxfill.escom.beans.system.counters;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.facade.CounterFacade;
import com.maxfill.model.numPuttern.counter.Counter;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.CellEditEvent;

@ViewScoped
@Named
public class CountersExplBean extends BaseDialogBean{
    private static final long serialVersionUID = 5004791826027575029L;

    private List<Counter> counters;        
    private Counter selected; 
    
    @EJB
    private CounterFacade counterFacade;
            
    @Override
    protected void initBean() {       
    }

    @Override
    public void onOpenCard(){       
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_COUNTERS;
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
    
}
