package com.maxfill.escom.beans.users;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.escom.utils.MsgUtils;
import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.partner.PartnersFacade;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.userGroups.UserGroups;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;

/* Сервисный бин "Пользователи" */
@Named
@SessionScoped
public class UserBean extends BaseExplBeanGroups<User, UserGroups>{
    private static final long serialVersionUID = -523024840800823503L;

    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private PartnersFacade partnersFacade;
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private FoldersFacade folderFacacde;
    
    @Inject
    private UserGroupsBean groupsBean;  
    
    private List<User> users;
            
    /* Пользователя при вставке нужно копировать только если он вставляется не в группу! */
    @Override
    public boolean isNeedCopyOnPaste(User pasteItem, BaseDict target) {
        return !(target instanceof UserGroups);
    }

    /* при перемещении пользователя drag&drop */
    @Override
    public boolean addItemToGroup(User user, BaseDict targetGroup) {
        if(user == null || targetGroup == null) return false;

        //UserGroups group = (UserGroups) targetGroup;
        if(!user.getUsersGroupsList().contains((UserGroups)targetGroup)) {
            user.getUsersGroupsList().add((UserGroups) targetGroup);
            //group.getUsersList().add---(user);
            getLazyFacade().edit(user);
        }
        return true;
    }

    @Override
    public UserFacade getLazyFacade() {
        return userFacade;
    }

    /**
     * Возвращает список пользователей, для которых не назначена штатная единица
     * @return 
     */
    public List<User> findVacantUsers(){
        return userFacade.findFreeStaffUsers(getCurrentUser());
    }
    
    /* Перемещение пользователя из одной группы в другую  */
    @Override
    public void moveItemToGroup(BaseDict targetGroup, User user, TreeNode sourceNode) {
        if(sourceNode != null) {
            UserGroups sourceGroup = (UserGroups) sourceNode.getData();
            user.getUsersGroupsList().remove(sourceGroup);
            sourceGroup.getUsersList().remove(user);
        }
        user.getUsersGroupsList().add((UserGroups) targetGroup);
        getLazyFacade().edit(user);
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

    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        groupsBean.getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
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
        if (user.getId().equals(0)){
            errors.add("OperationIsNotApplicable");
        }
        if (docFacade.findCountUserLinks(user) > 0 || attacheFacade.findCountUserLinks(user) > 0) {
            Object[] messageParameters = new Object[]{user.getName()};
            String error = MessageFormat.format(getMessageLabel("UserUsedInDocs"), messageParameters);
            errors.add(error);
        }
        if (processFacade.findCountUserLinks(user) > 0 ) {
            Object[] messageParameters = new Object[]{user.getName()};
            String error = MessageFormat.format(getMessageLabel("UserUsedInProcesses"), messageParameters);
            errors.add(error);
        }
        if (partnersFacade.findCountUserLinks(user) > 0 ) {
            Object[] messageParameters = new Object[]{user.getName()};
            String error = MessageFormat.format(getMessageLabel("UserUsedInPartneres"), messageParameters);
            errors.add(error);
        }        
        if(!staffFacade.findStaffsByUser(user).isEmpty() || staffFacade.findCountUserLinks(user) > 0 ) {
            Object[] messageParameters = new Object[]{user.getShortFIO()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("UserUsedInStaffs"), messageParameters);
            errors.add(error);
        }
        if(folderFacacde.findCountUserLinks(user) > 0 ) {
            Object[] messageParameters = new Object[]{user.getShortFIO()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("UserUsedInFolders"), messageParameters);
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

    /**
     * Список пользователей, доступный для просмотра текущему пользователя
     * чтобы каждый раз не лазить в базу за ним
     * @return 
     */
    public List<User> getUsers() {
        if (users == null){
            users = findAll();
        }
        return users;
    }
    
    public void reloadUsers(){
        users = null;
    }
    
    @Override
    protected void afterMoveToTrash(User user){
        if (users != null){
            users.remove(user);
        }
    }
}