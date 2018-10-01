package com.maxfill.model.core.roles;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.basedict.userGroups.UserGroups;
import javax.ejb.Stateless;

/**
 * Фасад для ролей
 */
@Stateless
public class RoleFacade extends BaseFacade<UserGroups>{
    public RoleFacade() {
        super(UserGroups.class);
    }

}
