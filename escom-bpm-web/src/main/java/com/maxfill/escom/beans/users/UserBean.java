package com.maxfill.escom.beans.users;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* Сервисный бин "Пользователи" */
@Named
@SessionScoped
public class UserBean extends BaseExplBeanGroups<User, UserGroups>{
    private static final long serialVersionUID = -523024840800823503L;

    @Inject
    private UserGroupsBean groupsBean;

    @EJB
    private StaffFacade staffFacade;

    /* Пользователя при вставке нужно копировать только если он вставляется не в группу! */
    @Override
    public boolean isNeedCopyOnPaste(User pasteItem, BaseDict target) {
        return !(target instanceof UserGroups);
    }

    @Override
    public void preparePasteItem(User pasteItem, User sourceItem, BaseDict target) {
        super.preparePasteItem(pasteItem, sourceItem, target);
        if(!isNeedCopyOnPaste(pasteItem, target)) {
            addItemToGroup(pasteItem, target);
        }
    }

    /* при перемещении пользователя drag&drop */
    @Override
    public boolean addItemToGroup(User user, BaseDict targetGroup) {
        if(user == null || targetGroup == null) return false;

        UserGroups group = (UserGroups) targetGroup;
        if(!user.getUsersGroupsList().contains((UserGroups) targetGroup)) {
            user.getUsersGroupsList().add((UserGroups) targetGroup);
            group.getUsersList().add(user);
            getFacade().edit(user);
        }
        return true;
    }

    @Override
    public UserFacade getFacade() {
        return userFacade;
    }

    /* Перемещение пользователя из одной группы в другую  */
    @Override
    public void moveItemToGroup(BaseDict targetGroup, User user, TreeNode sourceNode) {
        if(sourceNode != null) {
            UserGroups sourceGroup = (UserGroups) sourceNode.getData();
            user.getUsersGroupsList().remove(sourceGroup);
        }
        user.getUsersGroupsList().add((UserGroups) targetGroup);
        getFacade().edit(user);
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    /* Возвращает список групп, в которые входит пользователь  */
    @Override
    public List <UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }

    /* Открытие формы активных пользователей */
    //ToDo переделать на стандартный алгоритм открытия диалогов!
    public void onActiveUsersFormShow() {
        Map <String, Object> options = new HashMap <>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 800);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        PrimeFaces.current().dialog().openDynamic("/view/admin/users/sessions", options, null);
    }

    /* Формирует число ссылок на user в связанных объектах */
    @Override
    public void doGetCountUsesItem(User user, Map <String, Integer> rezult) {
        rezult.put("Staffs", staffFacade.findStaffsByUser(user).size());
    }

    /* Проверка возможности удаления user */
    @Override
    protected void checkAllowedDeleteItem(User user, Set <String> errors) {
        super.checkAllowedDeleteItem(user, errors);
        if(!staffFacade.findStaffsByUser(user).isEmpty()) {
            Object[] messageParameters = new Object[]{user.getShortFIO()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("UserUsedInStaffs"), messageParameters);
            errors.add(error);
        }
    }

    /**
     * Возвращает список логинов пользователей с учётом прав доступа пользователя, выполняющего запрос
     *
     * @return
     */
    public List <String> findAllLogins() {
        return findAll().stream().map(user -> user.getLogin()).sorted().collect(Collectors.toList());
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
    public BaseDetailsBean getGroupBean() {
        return groupsBean;
    }

    @Override
    public SearcheModel initSearcheModel() {
        return new UsersSearche();
    }

}