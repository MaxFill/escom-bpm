package com.maxfill.services.common.history;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 *
 * @author Maxim
 */
@Stateless
public class ServicesEventsFacade extends BaseFacade<ServicesEvents> {

    public ServicesEventsFacade() {
        super(ServicesEvents.class);
    }


}
