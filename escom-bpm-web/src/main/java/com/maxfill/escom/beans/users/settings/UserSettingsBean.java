
package com.maxfill.escom.beans.users.settings;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.User;
import com.maxfill.utils.EscomUtils;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/* Персональные настройки пользователя  */
@Named
@ViewScoped
public class UserSettingsBean extends BaseDialogBean{
    private static final long serialVersionUID = -3347099990747559193L;

    @EJB
    private UserFacade userFacade;
        
    public UserSettingsBean() {
    }

    private String newPassword;
    private String oldPassword;
    private String repeatePassword;

    @Override
    protected void initBean(){        
    }
    
    /* Сохранение настроек пользователя  */
    public void saveSettings(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    /* Изменение пароля пользователя */
    public void onChangePassword() throws NoSuchAlgorithmException{
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
        userFacade.edit(user);
        onCloseCard();
        setNewPassword(null);
        setOldPassword(null);
        setRepeatePassword(null);
        EscomBeanUtils.SuccesMsgAdd("Successfully", "PasswordIsChange");        
    }  

    @Override
    protected String onCloseCard() {
       return super.onFinalCloseCard(null);
    }
    
    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_USER_SETTINGS;
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
