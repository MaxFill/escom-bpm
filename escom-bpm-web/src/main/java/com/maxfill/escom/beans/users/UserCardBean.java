package com.maxfill.escom.beans.users;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.escom.utils.EscomMsgUtils;
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
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
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

/* Контроллер формы "Карточка пользователя */
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
     * Обработка события изменения телефона
     * @param event
     */
    public void onChangePhone(ValueChangeEvent event){
        String newValue = (String) event.getNewValue();
        if (StringUtils.isNotBlank(newValue) && StringUtils.isBlank(getEditedItem().getMobilePhone())){
            getEditedItem().setMobilePhone(newValue);
            onItemChange();
        }
    }

    /**
     * Обработка события изменения подписи
     * @param event
     */
    public void onEmailSignChange(ValueChangeEvent event){
        onItemChange();
    }

    /**
     * Обработка события выбора дефолтной папки
     */
    public void onInboxSelected(SelectEvent event){
        List<Folder> items = (List<Folder>) event.getObject();
        if (items.isEmpty()) return;
        Folder folder = items.get(0);
        onItemChange();
        getEditedItem().setInbox(folder);
        checkFolder(folder);
    }

    /**
     * Проверка выбранной папки
     */
    public void checkFolder(Folder folder){
        if(!folderFacade.checkRightAddDetail(folder, currentUser)) {
            String errMsg = EscomMsgUtils.getMessageLabel("SelectedFolderCantNotAddDocs");
            String checkError = EscomMsgUtils.getValidateLabel("CHECK_ERROR");
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("user:mainTabView:item");
            input.setValid(false);
            context.addMessage(input.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, checkError));
            context.validationFailed();
        }
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

    /**
     * Обработка события изменения пароля
     * @param event
     * @throws NoSuchAlgorithmException
     */
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

    /**
     * Проверка корректности полей перед сохранением
     * @param user
     * @param errors
     */
    @Override
    protected void checkItemBeforeSave(User user, Set<String> errors) {       
        super.checkItemBeforeSave(user, errors);

        String login = user.getLogin();
        Integer userId = user.getId();
        List<User> existUsers = getItemFacade().findByLoginExcludeId(login, userId);
        if (!existUsers.isEmpty()) {
            errors.add(MessageFormat.format(EscomMsgUtils.getMessageLabel("UserLoginIsExsist"), new Object[]{login}));
        }
        if (user.isDoubleFactorAuth() && StringUtils.isBlank(user.getMobilePhone())){
            errors.add(MessageFormat.format(EscomMsgUtils.getMessageLabel("NeedSetMobilePhone"), new Object[]{}));
        }
    }

    /**
     * Действия перед сохранением пользователя
     * @param user
     */
    @Override
    protected void onBeforeSaveItem(User user){
        if (StringUtils.isBlank(user.getName())){
            user.setName(user.getShortFIO());
        }
        if (user.isNeedChangePwl()){
            String msg = EscomMsgUtils.getMessageLabel("YouNeedChangePassword");
            getItemFacade().sendSystemMsg(user, msg);
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