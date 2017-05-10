package com.maxfill.escom.beans.users.groups;

import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.utils.EscomBeanUtils;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Группы пользователей
 * @author mfilatov
 */
@Named
@ViewScoped
public class UserGroupsBean extends BaseTreeBean<UserGroups, UserGroups> {
    private static final long serialVersionUID = -7609222014155311960L;
    private static final String BEAN_NAME = "userGroupsBean";

    @EJB
    private UserGroupsFacade usGroupFacade;   
    
    public UserGroupsBean() { 
    }              
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }    
    
    @Override
    public UserGroupsFacade getItemFacade() {
        return usGroupFacade;
    }

    @Override
    public List<UserGroups> getGroups(UserGroups item) {
        return null;
    }
    
    /**
     * Формирует число ссылок на userGroups в связанных объектах 
     * @param userGroups
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(UserGroups userGroups,  Map<String, Integer> rezult){
        rezult.put("Users", userGroups.getDetailItems().size());
        rezult.put("UsersGroups", userGroups.getChildItems().size());
    }    
    
    /**
     * Проверка возможности удаления userGroups
     * @param userGroups
     */
    @Override
    protected void checkAllowedDeleteItem(UserGroups userGroups, Set<String> errors){
        super.checkAllowedDeleteItem(userGroups, errors);       
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
