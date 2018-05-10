package com.maxfill.facade;

import com.maxfill.services.Services;
import com.maxfill.facade.base.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Системная служба (сервис)"
 */
@Stateless
public class ServicesFacade extends BaseFacade<Services> {

    public ServicesFacade() {
        super(Services.class);
    }

}
