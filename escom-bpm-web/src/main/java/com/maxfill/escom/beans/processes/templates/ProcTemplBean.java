package com.maxfill.escom.beans.processes.templates;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.model.process.templates.ProcessTemplFacade;
import com.maxfill.model.process.templates.ProcTempl;
import com.maxfill.model.process.types.ProcessType;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Сервисный бин сущности "Шаблон процесса"
 */
@Named
@SessionScoped
public class ProcTemplBean extends BaseDetailsBean<ProcTempl, ProcessType>{    
    private static final long serialVersionUID = -6531285763555777301L;

    @EJB
    private ProcessTemplFacade procTemplFacade;
    @Inject
    private ProcessTypesBean processTypesBean;
    
    @Override
    public List<ProcessType> getGroups(ProcTempl item) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return processTypesBean;
    }

    @Override
    public Class<ProcessType> getOwnerClass() {
        return ProcessType.class;
    }

    @Override
    public BaseTableBean getDetailBean() {
         return null;
    }

    @Override
    public ProcessTemplFacade getFacade() {
        return procTemplFacade;
    }    
}
