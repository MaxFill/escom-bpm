package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.rights.Rights;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Группы пользователей" */
@Named
@SessionScoped
public class UserGroupsBean extends BaseTreeBean<UserGroups, UserGroups> {
    private static final long serialVersionUID = -7609222014155311960L;

    @EJB
    private UserGroupsFacade itemFacade;     
    
    /* Получение прав доступа для иерархического справочника */
    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item); //получаем свои права 
        }
        
        if (item.getParent() != null) {
            return getRightItem(item.getParent()); //получаем права от родительской группы
        }                     
        
        return getDefaultRights(item);
    }
    
    @Override
    public UserGroupsFacade getItemFacade() {
        return itemFacade;
    }

    @Override
    public List<UserGroups> getGroups(UserGroups item) {
        return null;
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(UserGroups group){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(group.getChildItems());
        return dependency;
    }
    
    @Override
    public void preparePasteItem(UserGroups pasteItem, BaseDict target){
        pasteItem.setParent((UserGroups)target);    
    }
    
    /* Формирует число ссылок на userGroups в связанных объектах  */
    @Override
    public void doGetCountUsesItem(UserGroups userGroups,  Map<String, Integer> rezult){
        rezult.put("Users", userGroups.getDetailItems().size());
        rezult.put("UsersGroups", userGroups.getChildItems().size());
        rezult.put("Rights", rightFacade.findRightsByGroupId(userGroups.getId()).size());
    }
    
    /* Проверка возможности удаления группы пользователей */
    @Override
    protected void checkAllowedDeleteItem(UserGroups userGroups, Set<String> errors){
        if (!rightFacade.findRightsByGroupId(userGroups.getId()).isEmpty()){
            Object[] messageParameters = new Object[]{userGroups.getName()};
            String message = EscomBeanUtils.getMessageLabel("UserGroupsUsedInRights");
            String error = MessageFormat.format(message, messageParameters);
            errors.add(error);
        }       
    }

    /* Обработка события перемещения подчинённых объектов при перемещение группы пользователей в корзину */
    @Override
    protected void moveDetailItemsToTrash(BaseDict ownerItem, Set<String> errors) {          
        // При перемещение группы пользователей в корзину ничего с пользователями не делать!
    }
    
    /* Обработка события удаление подчинённых объектов при удалении группы пользователей */
    @Override
    protected void deleteDetails(UserGroups userGroups) {
        // При удалении группы пользователей удалять пользователей не нужно!
    }    
    
    @Override
    public Class<UserGroups> getItemClass() {
        return UserGroups.class;
    }
    
    @Override
    public Class<UserGroups> getOwnerClass() {
        return null;
    }
    
    @FacesConverter("groupsUserConvertor")
    public static class groupsUserConvertors implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {     
                 UserGroupsBean bean = EscomBeanUtils.findBean("userGroupsBean", fc);
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
                return String.valueOf(((UserGroups)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}
