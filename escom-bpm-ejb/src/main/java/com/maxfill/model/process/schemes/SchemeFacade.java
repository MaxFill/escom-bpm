package com.maxfill.model.process.schemes;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.process.schemes.Scheme;

import javax.ejb.Stateless;

/**
 * Фасад для сущности "Схема процесса"
 */
@Stateless
public class SchemeFacade extends BaseFacade{

    public SchemeFacade() {
        super(Scheme.class);
    }
}
