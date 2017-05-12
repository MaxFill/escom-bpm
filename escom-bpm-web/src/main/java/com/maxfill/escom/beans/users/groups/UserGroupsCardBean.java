package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Группы пользователей
 * @author mfilatov
 */
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
    protected void afterCreateItem(UserGroups item) {        
    }

    @Override
    public Class<UserGroups> getItemClass() {
        return UserGroups.class;
    }

    /* Добавление пользователя в группу пользователей */
    @Override
    protected void addItemInGroup(UserGroups userGroups, User user) {
        if (user == null){
            return;
        }
        List<User> groups = getGroups(userGroups);
        if (!groups.contains(user)){
            groups.add(user);
        } 
        if(!addGroups.contains(user)){
            addGroups.add(user);
        }
        setIsItemChange(Boolean.TRUE);        
    }
    
    @Override
    public List<User> getGroups(UserGroups group) {
        return group.getUsersList();
    }
}
