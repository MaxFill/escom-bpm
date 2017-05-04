package com.maxfill.facade;

import com.maxfill.services.Services;
import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 *
 * @author Maxim
 */
@Stateless
public class ServicesFacade extends BaseFacade<Services> {

    public ServicesFacade() {
        super(Services.class);
    }


}
