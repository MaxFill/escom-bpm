package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.model.basedict.userGroups.UserGroupsFacade;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.basedict.user.User;
import java.util.List;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/* Контроллер формы "Группа пользователей" */
@Named
@ViewScoped
public class UserGroupsCardBean extends BaseCardBeanGroups<UserGroups, User> {
    private static final long serialVersionUID = -8016244075607449606L;  
                
    @EJB
    private UserGroupsFacade usGroupFacade;

    @Override
    public UserGroupsFacade getFacade() {
        return usGroupFacade;
    }

    /* Добавление пользователя в группу пользователей */
    @Override
    protected void addItemInGroup(UserGroups userGroups, User user) {
        if (user == null || userGroups == null) return;                
        
        List<User> groups = getGroups(userGroups);
        if (!groups.contains(user)){
            groups.add(user);
        } 
        onItemChange();
    }
    
    @Override
    public List<User> getGroups(UserGroups group) {
        return group.getUsersList();
    }
}
