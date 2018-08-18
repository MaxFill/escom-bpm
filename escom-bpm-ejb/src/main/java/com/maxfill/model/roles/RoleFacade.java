package com.maxfill.model.roles;

import com.maxfill.facade.BaseFacade;
import com.maxfill.model.users.groups.UserGroups;
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
