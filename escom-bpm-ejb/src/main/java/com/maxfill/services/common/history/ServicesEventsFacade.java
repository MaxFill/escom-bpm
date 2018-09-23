package com.maxfill.services.common.history;

import com.maxfill.facade.BaseLazyFacade;
import javax.ejb.Stateless;

/**
 *
 * @author Maxim
 */
@Stateless
public class ServicesEventsFacade extends BaseLazyFacade<ServicesEvents> {

    public ServicesEventsFacade() {
        super(ServicesEvents.class);
    }

}
