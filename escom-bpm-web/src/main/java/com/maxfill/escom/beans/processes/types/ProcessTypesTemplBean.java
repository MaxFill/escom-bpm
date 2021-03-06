package com.maxfill.escom.beans.processes.types;

import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.processes.templates.ProcTemplBean;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Сервисный бин для работы с сущностью "Виды Процессов" для использования в обозревателе шаблонов процессов
 * @author maksim
 */
@Named
@SessionScoped
public class ProcessTypesTemplBean extends BaseTreeBean<ProcessType, ProcessType> {
    private static final long serialVersionUID = -6084661748115239310L;

    @Inject
    private ProcTemplBean procTemplBean;
    @EJB
    private ProcessTypesFacade processTypesFacade;

    @Override
    public BaseDetailsBean getDetailBean() {
        return procTemplBean;
    }

    @Override
    public ProcessTypesFacade getLazyFacade() {
        return processTypesFacade;
    }

    @Override
    public List<ProcessType> getGroups(ProcessType item) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public Class <ProcessType> getOwnerClass() {
        return null;
    }
}