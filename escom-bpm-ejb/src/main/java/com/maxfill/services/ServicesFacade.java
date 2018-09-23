package com.maxfill.services;

import com.maxfill.facade.BaseFacade;
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
