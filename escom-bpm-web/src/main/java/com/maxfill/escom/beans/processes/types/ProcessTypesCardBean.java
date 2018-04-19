package com.maxfill.escom.beans.processes.types;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.ProcessTypesFacade;
import com.maxfill.model.process.types.ProcessType;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Вид процесса"
 */
@Named
@ViewScoped
public class ProcessTypesCardBean extends BaseCardBean<ProcessType>{
    private static final long serialVersionUID = 1741713396857933255L;

    @EJB
    private ProcessTypesFacade itemsFacade;

    @Override
    public ProcessTypesFacade getFacade() {
        return itemsFacade;
    }   


}