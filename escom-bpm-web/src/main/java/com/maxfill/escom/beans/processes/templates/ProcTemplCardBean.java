package com.maxfill.escom.beans.processes.templates;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import java.util.Set;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
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
    protected void checkItemBeforeSave(ProcTempl procTempl, FacesContext context, Set<String> errors){                
        if (procTempl.getIsDefault() == false){
            ProcTempl defaultTempl = procTempl.getOwner().getTemplates().stream()
                    .filter(templ->templ.getIsDefault())
                    .findFirst().orElse(null);
            if (defaultTempl == null){
                errors.add(MsgUtils.getMessageLabel("OneTemplateShouldBeMain"));
            }
        }
        super.checkItemBeforeSave(procTempl, context, errors);
    } 
    
    public void makeFullName(){        
        String path = getEditedItem().getPath();
        getEditedItem().setName(path);
        onItemChange();
    }
}