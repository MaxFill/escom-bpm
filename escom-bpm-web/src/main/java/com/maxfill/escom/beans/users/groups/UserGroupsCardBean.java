package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.users.User;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/* Карточка Группы пользователей */
@Named
@ViewScoped
public class UserGroupsCardBean extends BaseCardBeanGroups<UserGroups, User> {
    private static final long serialVersionUID = -8016244075607449606L;  
                
    @EJB
    private UserGroupsFacade usGroupFacade;   
           
    @Override
    public UserGroupsFacade getItemFacade() {
        return usGroupFacade;
    }
    
    @Override
    public Class<UserGroups> getItemClass() {
        return UserGroups.class;
    }

    /* Добавление пользователя в группу пользователей */
    @Override
    protected void addItemInGroup(UserGroups userGroups, User user) {
        if (user == null || userGroups == null || userGroups.getId().equals(0)) return;
        
        List<User> groups = getGroups(userGroups);
        if (!groups.contains(user)){
            groups.add(user);
        } 
        if(!addGroups.contains(user)){
            addGroups.add(user);
        }
        onItemChange();        
    }
    
    @Override
    public List<User> getGroups(UserGroups group) {
        return group.getUsersList();
    }
}
