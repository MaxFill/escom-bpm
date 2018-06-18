package com.maxfill.facade;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.base.BaseDictWithRolesFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.process.ProcessLog;
import com.maxfill.model.process.ProcessStates;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Map;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Процессы"
 */
@Stateless
public class ProcessFacade extends BaseDictWithRolesFacade<Process, ProcessType, ProcessLog, ProcessStates>{

    @EJB
    private ProcessTypesFacade processTypesFacade;

    public ProcessFacade() {
        super(Process.class, ProcessLog.class, ProcessStates.class);
    }

    @Override
    public Class <Process> getItemClass() {
        return Process.class;
    }

    @Override
    public int replaceItem(Process oldItem, Process newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 20;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PROCESS.toLowerCase();
    }

    @Override
    public void setSpecAtrForNewItem(Process process, Map<String, Object> params) {
        ProcessType processType = process.getOwner();
        //process.setName(processType.getName());
    }

    /**
     * Получение прав доступа к процессу
     * @param item
     * @param user
     * @return 
     */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user);
        }
        if (item.getOwner() != null) {
            Rights childRight = processTypesFacade.getRightForChild(item.getOwner());
            if (childRight != null) {
                return childRight;
            }
        }
        return getDefaultRights(item);
    }
    
    @Override
     public Tuple findDublicateExcludeItem(Process item){       
        return new Tuple(false, null);        
    }
}
