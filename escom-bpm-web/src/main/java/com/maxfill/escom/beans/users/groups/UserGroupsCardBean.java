package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Группы пользователей
 * @author mfilatov
 */
@Named
@ViewScoped
public class UserGroupsCardBean extends BaseCardBean<UserGroups> {
    private static final long serialVersionUID = -8016244075607449606L;  
    
    @EJB
    private UserGroupsFacade usGroupFacade;   
           
    @Override
    public UserGroupsFacade getItemFacade() {
        return usGroupFacade;
    }

    @Override
    protected void onAfterCreateItem(UserGroups item) {        
    }

    @Override
    public Class<UserGroups> getItemClass() {
        return UserGroups.class;
    }
}
