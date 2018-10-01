package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Сервисный бин для работы с сущностью "Процессы"
 * @author maksim
 */
@Named
@SessionScoped
public class ProcessBean extends BaseDetailsBean<Process, ProcessType>{
    private static final long serialVersionUID = 8861162094837813935L;

    private DiagramBean diagramBean;

    @EJB
    private ProcessFacade processFacade;
    @Inject
    private ProcessTypesBean processTypesBean;

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    @Override
    public ProcessFacade getLazyFacade() {
        return processFacade;
    }

    @Override
    public List getGroups(Process process) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return processTypesBean;
    }

    @Override
    public Class getOwnerClass() {
        return ProcessType.class;
    }

    @Override
    public boolean canCreateItem(TreeNode treeSelectedNode){
        return treeSelectedNode == null || ((BaseDict)treeSelectedNode.getData()).getId() == 0;
    }

    @Override
    public SearcheModel initSearcheModel() {
        return new ProcessSearche();
    }

    /* GETS & SETS */

    public DiagramBean getDiagramBean() {
        return diagramBean;
    }
    public void setDiagramBean(DiagramBean diagramBean) {
        this.diagramBean = diagramBean;
    }
}
