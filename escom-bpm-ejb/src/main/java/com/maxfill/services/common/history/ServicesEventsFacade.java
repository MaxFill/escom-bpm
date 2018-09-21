package com.maxfill.services.common.history;

import com.maxfill.facade.BaseLazyLoadFacade;
import javax.ejb.Stateless;

/**
 *
 * @author Maxim
 */
@Stateless
public class ServicesEventsFacade extends BaseLazyLoadFacade<ServicesEvents> {

    public ServicesEventsFacade() {
        super(ServicesEvents.class);
    }

}
