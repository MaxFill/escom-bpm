package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictRoles;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import java.util.ArrayList;
import java.util.Collections;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * Сервисный бин для работы с сущностью "Процессы"
 * @author maksim
 */
@Named
@SessionScoped
public class ProcessBean extends BaseDetailsBean<Process, ProcessType>{
    private static final long serialVersionUID = 8861162094837813935L;
    
    @EJB
    private ProcessFacade processFacade;
    @Inject
    private ProcessTypesBean processTypesBean;

    private final List<SelectItem> ROLES = Collections.unmodifiableList(
        new ArrayList<SelectItem>() {
            private static final long serialVersionUID = 3109256773218160485L;
            {
                add(new SelectItem(DictRoles.ROLE_CHIEF, MsgUtils.getBandleLabel(DictRoles.ROLE_CHIEF)));
                add(new SelectItem(DictRoles.ROLE_CONCORDER, MsgUtils.getBandleLabel(DictRoles.ROLE_CONCORDER)));
                add(new SelectItem(DictRoles.ROLE_CONTROLLER, MsgUtils.getBandleLabel(DictRoles.ROLE_CONTROLLER)));
                add(new SelectItem(DictRoles.ROLE_CURATOR, MsgUtils.getBandleLabel(DictRoles.ROLE_CURATOR)));
                add(new SelectItem(DictRoles.ROLE_EDITOR, MsgUtils.getBandleLabel(DictRoles.ROLE_EDITOR)));
                add(new SelectItem(DictRoles.ROLE_EXECUTOR, MsgUtils.getBandleLabel(DictRoles.ROLE_EXECUTOR)));
                add(new SelectItem(DictRoles.ROLE_INSPECTOR, MsgUtils.getBandleLabel(DictRoles.ROLE_INSPECTOR)));
                add(new SelectItem(DictRoles.ROLE_OWNER, MsgUtils.getBandleLabel(DictRoles.ROLE_OWNER)));
                add(new SelectItem(DictRoles.ROLE_REGISTRATOR, MsgUtils.getBandleLabel(DictRoles.ROLE_REGISTRATOR)));                
            }
        });
    
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
        SearcheModel sm = new ProcessSearche();
        sm.setAuthorSearche(getCurrentUser());
        return sm;
    }

    public List<SelectItem> getROLES() {
        return ROLES;
    }        
}
