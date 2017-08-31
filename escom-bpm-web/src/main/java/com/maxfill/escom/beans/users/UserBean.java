package com.maxfill.escom.beans.users;

import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.User;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

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
    public boolean isNeedCopyOnPaste(User pasteItem, BaseDict target){
        return !(target instanceof UserGroups);
    }
    
    @Override
    protected void detectParentOwner(User user, BaseDict owner){
        user.setOwner(null);
        user.setParent(null);
        if (owner == null) return;
        if (!user.getUsersGroupsList().contains((UserGroups)owner)){
            user.getUsersGroupsList().add((UserGroups)owner);            
        } 
    }
        
    @Override
    public void preparePasteItem(User pasteItem, User sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        if (!isNeedCopyOnPaste(pasteItem, target)){
            addItemToGroup(pasteItem, target);        
        }
    }  
    
    /* при перемещении пользователя drag&drop */
    @Override
    public boolean addItemToGroup(User user, BaseDict targetGroup){
        if (user == null || targetGroup == null) return false;
        
        UserGroups group = (UserGroups)targetGroup;
        if (!user.getUsersGroupsList().contains((UserGroups)targetGroup)){
            user.getUsersGroupsList().add((UserGroups)targetGroup);
            group.getUsersList().add(user);
            getItemFacade().edit(user);
        }
        return true;
    }      

    @Override
    public UserFacade getItemFacade() {
        return userFacade;
    }

    /* Перемещение пользователя из одной группы в другую  */
    @Override
    public void moveItemToGroup(BaseDict targetGroup, User user, TreeNode sourceNode) {        
        if (sourceNode != null){
            UserGroups sourceGroup = (UserGroups)sourceNode.getData();
            user.getUsersGroupsList().remove(sourceGroup);
        }                             
        user.getUsersGroupsList().add((UserGroups)targetGroup);
        getItemFacade().edit(user);
    }    
    
    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    /* Возвращает список групп, в которые входит пользователь  */
    @Override
    public List<UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }    
    
    /* Открытие формы активных пользователей */
    public void onActiveUsersFormShow(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 800);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/admin/users/sessions", options, null); 
    }                
    
    /* Формирует число ссылок на user в связанных объектах */
    @Override
    public void doGetCountUsesItem(User user,  Map<String, Integer> rezult){
        rezult.put("Staffs", staffFacade.findStaffsByUser(user).size());
    }    
    
    /* Проверка возможности удаления user */
    @Override
    protected void checkAllowedDeleteItem(User user, Set<String> errors){
        super.checkAllowedDeleteItem(user, errors);
        if (!staffFacade.findStaffsByUser(user).isEmpty()){
            Object[] messageParameters = new Object[]{user.getShortFIO()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("UserUsedInStaffs"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public Class<User> getItemClass() {
        return User.class;
    }

    @Override
    public Class<UserGroups> getOwnerClass() {
       return null; 
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }
    
    @Override
    public BaseExplBean getGroupBean() {
        return groupsBean;
    }
    
    @FacesConverter("usersConvertor")
    public static class usersConvertors implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {       
                 UserBean bean = EscomBeanUtils.findBean("userBean", fc);
                 return bean.getItemFacade().find(Integer.parseInt(value));
             } catch(NumberFormatException e) {
                 throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
             }
         }
         else {
             return null;
         }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((User)object).getId());
            }
            else {
                return "";
            }
        }      
    }
    
}