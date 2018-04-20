package com.maxfill.facade;

import com.maxfill.facade.base.BaseFacade;
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
