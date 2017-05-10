package com.maxfill.escom.beans.users;

import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.User;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Bean для Пользователей
 * @author Maxim
 */
@Named
@ViewScoped
public class UserCardBean extends BaseCardBeanGroups<User, UserGroups>{            
    private static final long serialVersionUID = 2031203859450836271L;

    @EJB 
    private UserFacade userFacade;

    /**
     * Формирование отображаемого имени пользователя
     */    
    public void makeName(){
        getEditedItem().setName(getEditedItem().getShortFIO());
    }
            
    @Override
    public UserFacade getItemFacade() {
        return userFacade;
    }
         
    /**
     * Проверка корректности пользователя перед сохранением карточки
     *
     * @param user
     * @param errors
     */
    @Override
    protected void checkItemBeforeSave(User user, Set<String> errors) {       
        String login = user.getLogin();
        Integer userId = user.getId();
        List<User> existUsers = getItemFacade().findByLoginExcludeId(login, userId);
        if (!existUsers.isEmpty()) {
            Object[] params = new Object[]{login};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("UserLoginIsExsist"), params);
            errors.add(error);
        }
    }
    
    @Override
    protected void onBeforeSaveItem(User user){
        if (StringUtils.isBlank(user.getName())){
            user.setName(user.getShortFIO());
        }
        try {
            user.setPassword(EscomUtils.encryptPassword(user.getPassword()));
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }

    @Override
    protected void onAfterCreateItem(User item) {        
    }

    @Override
    public Class<User> getItemClass() {
        return User.class;
    }
}