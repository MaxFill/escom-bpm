package com.maxfill.escom.beans.users.groups;

import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.treelike.UserGroupsFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.groups.UserGroups;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* Сервисный бин "Группы пользователей" */
@Named
@SessionScoped
public class UserGroupsBean extends BaseTreeBean<UserGroups, UserGroups> {
    private static final long serialVersionUID = -7609222014155311960L;

    @Inject
    private UserBean userBean;
    
    @EJB
    private UserGroupsFacade itemFacade;     
    
    public List<UserGroups> findOnlyGroups(){
        return getFacade().findGroupsByType(DictRights.ACTUALISE_IN_GROUP).stream()
                    .filter(item -> itemFacade.preloadCheckRightView(item, getCurrentUser()))
                    .collect(Collectors.toList());
    }
    
    public List<UserGroups> findOnlyRoles(){
        return getFacade().findGroupsByType(DictRights.ACTUALISE_IN_CARD).stream()
                    .filter(item -> itemFacade.preloadCheckRightView(item, getCurrentUser()))
                    .collect(Collectors.toList());
    }
        
    @Override
    public UserGroupsFacade getFacade() {
        return itemFacade;
    }

    @Override
    public List<UserGroups> getGroups(UserGroups item) {
        return null;
    }

    /**
     * Возвращает списки зависимых объектов, необходимых для копирования
     * @param group
     * @return
     */
    @Override
    public List<List<?>> doGetDependency(UserGroups group){
        List<List<?>> dependency = new ArrayList<>();
        List<UserGroups> userGroups = itemFacade.findActualChilds(group);
        if (!userGroups.isEmpty()) {
            dependency.add(userGroups);
        }
        return dependency;
    }
    
    @Override
    public void preparePasteItem(UserGroups pasteItem, UserGroups sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        pasteItem.setParent((UserGroups)target);    
    }
    
    /* Формирует число ссылок на userGroups в связанных объектах  */
    @Override
    public void doGetCountUsesItem(UserGroups userGroups,  Map<String, Integer> rezult){
        rezult.put("Users", userGroups.getDetailItems().size());
        rezult.put("UsersGroups", userGroups.getChildItems().size());
        rezult.put("Rights", rightFacade.findRightsByGroupId(userGroups.getId()).size());
    }

    /**
     * Проверка возможности удаления группы пользователей
     * Группу можно удалить если она не используется в настройках прав доступа
     * @param userGroups
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(UserGroups userGroups, Set<String> errors){
        if (!rightFacade.findRightsByGroupId(userGroups.getId()).isEmpty()){
            Object[] messageParameters = new Object[]{userGroups.getName()};
            String message = MsgUtils.getMessageLabel("UserGroupsUsedInRights");
            String error = MessageFormat.format(message, messageParameters);
            errors.add(error);
        }       
    }

    /* Обработка события перемещения подчинённых объектов при перемещение группы пользователей в корзину */
    @Override
    protected void moveDetailItemsToTrash(UserGroups item, Set<String> errors) {          
        // При перемещение группы пользователей в корзину ничего с пользователями не делать!
    }
    
    /* Обработка события удаление подчинённых объектов при удалении группы пользователей */
    @Override
    protected void deleteDetails(UserGroups userGroups) {
        // При удалении группы пользователей удалять пользователей не нужно!
    }
    
    @Override
    public Class<UserGroups> getOwnerClass() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return userBean;
    }

}
