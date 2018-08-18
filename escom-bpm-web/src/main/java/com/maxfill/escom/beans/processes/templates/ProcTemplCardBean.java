package com.maxfill.escom.beans.processes.templates;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.process.templates.ProcTempl;
import com.maxfill.model.process.templates.ProcessTemplFacade;
import java.util.Set;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Шаблон процесса"
 * @author maksim
 */
@Named
@ViewScoped
public class ProcTemplCardBean extends BaseCardBean<ProcTempl>{
    private static final long serialVersionUID = 8630925136840883232L;

    @EJB
    private ProcessTemplFacade processTemplFacade;
    private Boolean isDefault;        
    
    @Override
    protected ProcessTemplFacade getFacade() {
        return processTemplFacade;
    }
    
    @Override
    protected void doPrepareOpen(ProcTempl item) {
        isDefault = item.getIsDefault();
    }
    
    /**
     * Обработка события изменения признака "Основной шаблон"
     * @param event
     */
    public void onDefaultChange(ValueChangeEvent event){
        onItemChange();
    }
    
    @Override
    protected void onBeforeSaveItem(ProcTempl procTempl){ 
        if (!isDefault.equals(procTempl.getIsDefault())){
            processTemplFacade.clearDefaultTemplate(procTempl);
        }
        super.onBeforeSaveItem(procTempl);
    }
    
    @Override
    protected void checkItemBeforeSave(ProcTempl procTempl, Set<String> errors){                
        if (procTempl.getIsDefault() == false && procTempl.getOwner().getTemplates().size() < 2){
            errors.add(MsgUtils.getMessageLabel("OneTemplateShouldBeMain"));
        }
        super.checkItemBeforeSave(procTempl, errors);
    } 
    
}