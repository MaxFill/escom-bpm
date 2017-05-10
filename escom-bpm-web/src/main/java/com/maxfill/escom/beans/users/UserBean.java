package com.maxfill.escom.beans.users;

import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.User;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Bean для Пользователей
 * @author Maxim
 */
@Named
@ViewScoped
public class UserBean extends BaseExplBeanGroups<User, UserGroups>{            
    private static final long serialVersionUID = -523024840800823503L;    
    private static final String BEAN_NAME = "userBean";
    
    @EJB 
    private UserFacade userFacade;
    @EJB 
    private StaffFacade staffFacade;
    
    private String newPassword;
    private String oldPassword;
    private String repeatePassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getRepeatePassword() {
        return repeatePassword;
    }

    public void setRepeatePassword(String repeatePassword) {
        this.repeatePassword = repeatePassword;
    }
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }       

    @Override
    public UserFacade getItemFacade() {
        return userFacade;
    }

    /**
     * Перемещение пользователя из одной группы в другую
     * 
     * @param targetGroup (dropItem)
     * @param user (dragItem)
     */
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

    /**
     * Возвращает список групп, в которые входит пользователь
     * @param item
     * @return 
     */
    @Override
    public List<UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }    
    
    /**
     * Открытие формы активных пользователей
     */
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
    
    /**
     * Открытие формы изменения пароля
     */
    public void openChangePassword(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("modal", true);
        options.put("width", 600);
        options.put("height", 200);
        options.put("maximizable", false);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/admin/users/change-password", options, null);  
    }
    
    /**
     * Изменение пароля пользователя
     * @throws java.security.NoSuchAlgorithmException
     */
    public void changePassword() throws NoSuchAlgorithmException{
        String newPwl = getNewPassword().trim();
        String repPwl = getRepeatePassword().trim();
        if (!Objects.equals(newPwl, repPwl)){
            EscomBeanUtils.ErrorMsgAdd("Error", "PasswordsNotMatch", "");
            return;
        }
        String oldPwlMD5 = EscomUtils.encryptPassword(getOldPassword().trim());
        String curPwlMD5 = sessionBean.getCurrentUser().getPassword();         
        if (!Objects.equals(curPwlMD5, oldPwlMD5)){
            EscomBeanUtils.ErrorMsgAdd("Error", "PasswordIncorrect", "");
            return;
        }
        User user = sessionBean.getCurrentUser();
        user.setPassword(EscomUtils.encryptPassword(newPwl));
        getItemFacade().edit(user);
        RequestContext.getCurrentInstance().closeDialog(null);
        setNewPassword(null);
        setOldPassword(null);
        setRepeatePassword(null);
        EscomBeanUtils.SuccesMsgAdd("Successfully", "PasswordIsChange");        
    }    
    
    /**
     * Формирует число ссылок на user в связанных объектах 
     * @param user
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(User user,  Map<String, Integer> rezult){
        rezult.put("Staffs", staffFacade.findStaffsByUser(user).size());
    }    
    
    /**
     * Проверка возможности удаления user
     * @param user
     */
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
       return UserGroups.class; 
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