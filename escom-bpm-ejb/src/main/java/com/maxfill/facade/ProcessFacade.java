package com.maxfill.facade;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.facade.base.BaseDictWithRolesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.ProcessLog;
import com.maxfill.model.process.ProcessStates;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.users.User;

import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Фасад для сущности "Процессы"
 */
@Stateless
public class ProcessFacade extends BaseDictWithRolesFacade<Process, ProcessType, ProcessLog, ProcessStates>{

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
        process.setName(processType.getName());
    }

}
