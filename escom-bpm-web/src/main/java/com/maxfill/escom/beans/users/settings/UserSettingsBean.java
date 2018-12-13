package com.maxfill.escom.beans.users.settings;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.core.interfaces.WithDetails;
import com.maxfill.escom.beans.users.assistants.AssistantBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.assistant.Assistant;
import com.maxfill.model.basedict.assistant.AssistantFacade;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.utils.EscomUtils;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/* Персональные настройки пользователя  */
@Named
@ViewScoped
public class UserSettingsBean extends BaseViewBean<BaseView> implements WithDetails{
    private static final long serialVersionUID = -3347099990747559193L;

    @EJB
    private UserFacade userFacade;
    @EJB
    private AssistantFacade assistantFacade;
    
    @Inject
    private AssistantBean assistantBean;    

    private String oldPassword;
    private String newPassword;
    private String repeatePassword;

    private List<Assistant> checkedDetails = new ArrayList<>();
    private Assistant selectedDetail;
    private User user;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        user = sessionBean.getCurrentUser();
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("PersonalSettings");
    }
    
    @Override
    public void onAfterFormLoad() {
        if (user.isNeedChangePwl()) {
            MsgUtils.errorMsg("YouNeedChangePassword");
        }
    }
    
    /* Сохранение настроек пользователя  */
    public void onSaveAndClose(){
        userFacade.edit(user);
        onCloseCard();
    }
    
    /* Изменение пароля пользователя */
    public void onChangePassword() throws NoSuchAlgorithmException{
        Set<FacesMessage> errors = new HashSet<>();
        String newPwl = newPassword.trim();
        checkPwl(newPwl, repeatePassword.trim(), oldPassword, errors);

        if(!errors.isEmpty()) {
            MsgUtils.showFacesMessages(errors);
            return;
        }

        user.setPassword(EscomUtils.encryptPassword(newPwl));
        user.setNeedChangePwl(false);
        userFacade.edit(user);
        onCloseCard();
        setNewPassword(null);
        setOldPassword(null);
        setRepeatePassword(null);
        MsgUtils.succesMsg("PasswordIsChange");
    }

    /**
     * Проверка корректности нового пароля
     * @param newPwl
     * @param repPwl
     * @param curPwl - введенный текущий пароль
     * @param errors
     */
    private void checkPwl(String newPwl, String repPwl, String curPwl, Set<FacesMessage> errors){
        // проверка что новый пароль и его повтор совпадают
        if (!Objects.equals(newPwl, repPwl)){
            errors.add(MsgUtils.prepFormatErrorMsg("PasswordsNotMatch", new Object[]{}));
        }

        // проверка на то что введён правильный текущий пароль
        String oldPwlMD5 = EscomUtils.encryptPassword(curPwl);  //введённый текущий пароль
        String curPwlMD5 = user.getPassword();                  //пароль, в карточке пользователя
        if (!Objects.equals(curPwlMD5, oldPwlMD5)){
            errors.add(MsgUtils.prepFormatErrorMsg("PasswordIncorrect", new Object[]{}));
        }

        // проверка на длину пароля
        if (newPwl.length() < 8 ){
            errors.add(MsgUtils.prepFormatErrorMsg("PasswordLengthIncorrect", new Object[]{}));
        }

        // проверка на то что новый пароль отличается от старого
        String newPwlMd5 = EscomUtils.encryptPassword(newPwl);
        if (Objects.equals(newPwlMd5, oldPwlMD5)) {
            errors.add(MsgUtils.prepFormatErrorMsg("PassordMustDifferent", new Object[]{}));
        }

        // проверка на требования по сложности пароля
        if (!isStrong(newPwl)) {
            errors.add(MsgUtils.prepFormatErrorMsg("PasswordSecurityRequirements", new Object[]{}));
        }
    }

    /**
     * Проверка что пароль сложный
     * @param password
     * @return
     */
    private boolean isStrong(String password){
        /*
            ^                         Start anchor
            (?=.*[A-Z].*[A-Z])        Ensure string has two uppercase letters.
            (?=.*[!@#$&*])            Ensure string has one special case letter.
            (?=.*[0-9].*[0-9])        Ensure string has two digits.
            (?=.*[a-z].*[a-z].*[a-z]) Ensure string has three lowercase letters.
            .{8,16}                   Ensure string is of length 8.
            $                         End anchor.
        */
        return password.matches("(?=.*[A-ZА-Я])(?=.*[!@#$%^&+=_*])(?=.*[0-9].*[0-9])(?=.*[a-zа-я])(?=\\S+$).{8,32}");
    }

    @Override
    public String getFormName(){
        return DictFrmName.FRM_USER_SETTINGS;
    }
    
    /* Assistants */

    public boolean isCanChangeAssists(){
        return !isReadOnly();
    }
    
    @Override
    public List<Assistant> getDetails(){
        return user.getAssistants();
    }

    @Override
    public List<Assistant> getCheckedDetails() {
        return checkedDetails;
    }
    @Override
    public void setCheckedDetails(List checkedDetails) {
        this.checkedDetails = checkedDetails;
    }

    @Override
    public void onDeleteDetail(BaseDict item) {
        Assistant assistant = (Assistant)item;
        getDetails().remove(assistant);
        
        if (assistant.getId() != null){
            assistantFacade.remove(assistant);
        }
        onItemChange();
    }    
    
    @Override
    public void onDeleteCheckedDetails(){
        getDetails().removeAll(checkedDetails);
        onItemChange();
    }
    
    @Override
    public void onCreateDetail(){
        selectedDetail = assistantFacade.createItem(getCurrentUser(), null, user, new HashMap<>());  
        StringBuilder sb = new StringBuilder(getLabelFromBundle("ForChief"));
        sb.append(": ").append(user.getShortFIO());
        selectedDetail.setName(sb.toString());
        onOpenDetail(selectedDetail);
    }
    
    @Override
    public void onOpenDetail(BaseDict item){
        setSourceItem(item); 
        setSelectedDetail(item);
        assistantBean.prepEditChildItem((Assistant)item, getParamsMap());
    }
    
    @Override
    public void afterCloseDetailItem(SelectEvent event){
        if (event.getObject() == null) return;        
        switch ((String) event.getObject()){
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{                
                if (selectedDetail.getId() == null){
                    getDetails().add(selectedDetail);
                }
                onItemChange();
                break;
            }
        }         
    }           

    @Override
    public Assistant getSelectedDetail() {
        return selectedDetail;
    }
    @Override
    public void setSelectedDetail(BaseDict selectedDetail) {
        this.selectedDetail = (Assistant)selectedDetail;
    }
    
    /* GETS & SETS */
    
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
}
