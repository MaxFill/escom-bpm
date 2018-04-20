package com.maxfill.escom.beans.processes.types;

import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.facade.ProcessTypesFacade;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.states.State;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Контролер формы "Вид процесса"
 */
@Named
@ViewScoped
public class ProcessTypesCardBean extends BaseCardTree<ProcessType>{
    private static final long serialVersionUID = 1741713396857933255L;

    @Inject
    private ProcessTypesBean processTypesBean;
    @Inject
    private ProcessBean processBean;

    @EJB
    private ProcessTypesFacade itemsFacade;

    @Override
    public ProcessTypesFacade getFacade() {
        return itemsFacade;
    }


    @Override
    protected BaseTreeBean getTreeBean() {
        return processTypesBean;
    }

    @Override
    public List<State> getStateForChild() {
        return processBean.getMetadatesObj().getStatesList();
    }
}