package com.maxfill.escom.beans.processes.templates;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.model.process.templates.ProcTempl;
import com.maxfill.model.process.templates.ProcessTemplFacade;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
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
            
    @Override
    protected ProcessTemplFacade getFacade() {
        return processTemplFacade;
    }
    
}
