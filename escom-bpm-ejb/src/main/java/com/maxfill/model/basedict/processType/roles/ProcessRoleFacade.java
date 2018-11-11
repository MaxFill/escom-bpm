package com.maxfill.model.basedict.processType.roles;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для ролей
 */
@Stateless
public class ProcessRoleFacade extends BaseFacade<ProcessRole>{
    public ProcessRoleFacade() {
        super(ProcessRole.class);
    }

}
