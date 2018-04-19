package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process>{
    @EJB
    private ProcessFacade processFacade;

    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }
}
