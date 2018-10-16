package com.maxfill.escom.beans.processes.templates;

import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.processType.ProcessType;
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
public class ProcTemplBean extends BaseExplBeanGroups<ProcTempl, ProcessType>{    
    private static final long serialVersionUID = -6531285763555777301L;

    @EJB
    private ProcessTemplFacade procTemplFacade;
    @Inject
    private ProcessTypesBean processTypesBean;
    
        
    @Override
    public boolean addItemToGroup(ProcTempl procTempl, BaseDict group){ 
        //поскольку шаблон может быть только в одном виде процесса, то выполняем перемещение
        moveItemToGroup(group, procTempl); 
        return true;
    }
    
    public void moveItemToGroup(BaseDict group, ProcTempl procTempl){
        procTemplFacade.detectParentOwner(procTempl, group, group);
        getLazyFacade().edit(procTempl);
    }
        
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
    public ProcessTemplFacade getLazyFacade() {
        return procTemplFacade;
    }    

    @Override
    public BaseDetailsBean getGroupBean() {
        return processTypesBean;
    }
}
