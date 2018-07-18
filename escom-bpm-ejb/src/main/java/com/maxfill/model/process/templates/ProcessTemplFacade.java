package com.maxfill.model.process.templates;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.process.types.ProcessType;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Шаблоны процессов"
 */
@Stateless
public class ProcessTemplFacade extends BaseDictFacade<ProcTempl, ProcessType, ProcTemplLog, ProcTemplStates>{

    public ProcessTemplFacade() {
        super(ProcTempl.class, ProcTemplLog.class, ProcTemplStates.class);
    }
    
    @Override
    public Class<ProcTempl> getItemClass() {
        return ProcTempl.class;
    }

    @Override
    public int replaceItem(ProcTempl oldItem, ProcTempl newItem) {       
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PROC_TEMPL;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.PROCESS_TEMPLATE.toLowerCase();
    }
    
}
