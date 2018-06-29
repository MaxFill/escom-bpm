package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.task.Task;
import java.util.Date;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@SessionScoped
public class ProcessBean extends BaseDetailsBean<Process, ProcessType>{
    private static final long serialVersionUID = 8861162094837813935L;

    @EJB
    private ProcessFacade processFacade;
    @Inject
    private ProcessTypesBean processTypesBean;

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getFacade() {
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
    
    /**
     * Формирование статуса задачи в зависимости от её выполнения
     * @param task
     * @return 
     */
    public String getTaskStatus(Task task){
        if (task == null) return "";
        if (task.getBeginDate() == null) return MsgUtils.getBandleLabel("NotStarted");
        if (task.isCompleted()) return MsgUtils.getBandleLabel("Сompleted");        
        return EscomBeanUtils.makeDateDiffStatus(new Date(), task.getPlanExecDate());
    }
}
