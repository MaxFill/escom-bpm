package com.maxfill.escom.beans.users;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.facade.FoldersFacade;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.faces.event.ValueChangeEvent;
import org.apache.commons.lang.WordUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

/* Бин для формы "Карточка пользователя */
@Named
@ViewScoped
public class UserCardBean extends BaseCardBeanGroups<User, UserGroups>{            
    private static final long serialVersionUID = 2031203859450836271L;

    private String password;

    @EJB
    private FoldersFacade folderFacade;

    @Override
    public void onOpenCard() {
        super.onOpenCard(); 
        if (getTypeEdit().equals(DictEditMode.EDIT_MODE)){
            password = "**********";
        }
    }

    /**
     * Обработка события выбора папки
     */
    public void onInboxSelected(SelectEvent event){
        List<Folder> items = (List<Folder>) event.getObject();
        if (items.isEmpty()) return;
        Folder item = items.get(0);
        onItemChange();
        getEditedItem().setInbox(item);
    }
    public void onInboxSelected(ValueChangeEvent event){
        Folder folder = (Folder) event.getNewValue();
        onItemChange();
        getEditedItem().setInbox(folder);
    }

    /* Формирование отображаемого имени пользователя */    
    public void makeName(){
        getEditedItem().setName(getEditedItem().getShortFIO());
    }

    /* Формирование логина для пользователя */
    public void makeLogin(){ 
        String name = getEditedItem().getName();
        if (StringUtils.isBlank(name)){
            name = getEditedItem().getShortFIO();
            getEditedItem().setName(name);
        }
        String login = name.replace(" ", "").replace(".", "").replace(",", "").toLowerCase();
        login = EscomBeanUtils.rusToEngTranlit(login);        
        getEditedItem().setLogin(WordUtils.capitalize(login));
    }
    
    public void onChangePassword(ValueChangeEvent event) throws NoSuchAlgorithmException{
        String newValue = (String) event.getNewValue();
        String newPwl = EscomUtils.encryptPassword(newValue);
        String oldPwl = (String) event.getOldValue();
        if (!Objects.equals(newPwl, oldPwl)){
            getEditedItem().setPassword(newPwl);
            getEditedItem().setPwl(newValue);
            onItemChange();
        }
    }
    
    @Override
    protected void addItemInGroup(User item, UserGroups group) {
        if (group == null || group.getId() == 0) return;
        super.addItemInGroup(item, group);
    }
    
    @Override
    public UserFacade getItemFacade() {
        return userFacade;
    }
         
    @Override
    protected void checkItemBeforeSave(User user, Set<String> errors) {       
        super.checkItemBeforeSave(user, errors);
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
        super.onBeforeSaveItem(user);
    }

    @Override
    public List<UserGroups> getGroups(User item) {
        return item.getUsersGroupsList();
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}