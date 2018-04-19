package com.maxfill.facade;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.process.types.ProcessTypeLog;
import com.maxfill.model.process.types.ProcessTypeStates;

import javax.ejb.Stateless;

/**
 * Фасад для сущности "Виды процессов"
 */
@Stateless
public class ProcessTypesFacade extends BaseDictFacade<ProcessType, ProcessType, ProcessTypeLog, ProcessTypeStates>{

    public ProcessTypesFacade() {
        super(ProcessType.class, ProcessTypeLog.class, ProcessTypeStates.class);
    }

    @Override
    public Class <ProcessType> getItemClass() {
        return ProcessType.class;
    }

    @Override
    public int replaceItem(ProcessType oldItem, ProcessType newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 21;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PROCESS_TYPE.toLowerCase();
    }
}
