
package com.maxfill.escom.beans.system.login;

import com.maxfill.Configuration;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.model.users.User;
import com.maxfill.facade.UserFacade;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.services.ldap.LdapUtils;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

/* Bean формы входа  */
@Named
@ViewScoped
public class LoginBean implements Serializable{    
    private static final long serialVersionUID = 4390983938416752289L;
    protected static final Logger LOG = Logger.getLogger(LoginBean.class.getName());
    
    private String userName;
    private String password;
    private List<CountryFlags> languages;
    private CountryFlags selectedLang;
    private String targetPage;
    private Integer countErrLogin = 0;
    
    @Inject
    private ApplicationBean appBean;
    @Inject
    private SessionBean sessionBean;
    @EJB 
    private UserFacade userFacade;
    @EJB 
    private Configuration configuration;
        
    @PostConstruct
    public void init(){
        languages = new ArrayList<>();        
        languages.add(new CountryFlags(0, "en", "English"));
        languages.add(new CountryFlags(1, "ru", "Русский"));
        Locale locale = sessionBean.getLocale(); 
        String localeLang = locale.getLanguage();
        for (CountryFlags language : languages){            
            if (Objects.equals(language.getName(), localeLang)){
                selectedLang = language;
                break;
            }
        }
    }
    
    public String login() throws NoSuchAlgorithmException{
        RequestContext context = RequestContext.getCurrentInstance();
        Set<FacesMessage> errors = new HashSet<>(); 
        
        if (appBean.getLicence().isExpired()){     
            errors.add(EscomBeanUtils.prepFormatErrorMsg("ErrorExpireLicence", new Object[]{appBean.getLicence().getStrTermLicence()}));
        }
        
        if (appBean.isNoAvailableLicence()){
            errors.add(EscomBeanUtils.prepErrorMsg("ErrorCountLogin"));
        }
        
        User user = null;
        List<User> users = userFacade.findByLogin(userName);
        if (users.isEmpty()){
            errors.add(EscomBeanUtils.prepErrorMsg("ERR_USER_NOT_REGISTRED"));
            makeCountErrLogin(context, errors);
        } else {
            user = users.get(0);        
            if (StringUtils.isNotBlank(user.getLDAPname())){
                checkLdapUser(errors, context);                                        
            } else {
                if (isIncorrectPassword(user)){
                    makeCountErrLogin(context, errors);
                    errors.add(EscomBeanUtils.prepErrorMsg("BadUserOrPassword"));
                }
            }
        }    
        
        if (!errors.isEmpty()){
            EscomBeanUtils.showFacesMessages(errors);
            return "";
        }
        
        /*
        if (appBean.isAlreadyLogin(user)){
            errorsKey.add("UserPreviouslyLogged");
            makeCountErrLogin(context, errorsKey);
            return showErrMsg(errorsKey, context);            
        }
        */
        initCurrentUser(user);
        if (StringUtils.isBlank(targetPage) || targetPage.contains(SysParams.LOGIN_PAGE)){
            targetPage = SysParams.MAIN_PAGE;
        }
        return targetPage;
    }
    
    /* Инициализация текущего пользователя */
    private void initCurrentUser(User user){
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();      
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("UserLogin", userName);
        user.getUsersGroupsList().size();
        sessionBean.setCurrentUser(user);        
        UserSettings userSettings = new UserSettings();
        byte[] compressXML = user.getUserSettings();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomBeanUtils.decompress(compressXML);
                userSettings = (UserSettings) JAXB.unmarshal(new StringReader(settingsXML), UserSettings.class);
            } catch (IOException ex) {
                Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        userSettings.setLanguage(selectedLang.getName());
        sessionBean.setUserSettings(userSettings);
        sessionBean.setPrimefacesTheme(userSettings.getTheme());
        appBean.addBusyLicence(user, httpSession);
        LOG.log(Level.INFO, "User is login = {0}", userName);
    }
    
    /* Проверка подключения к LDAP серверу  */
    private void checkLdapUser(Set<FacesMessage> errors, RequestContext context){
        try {      
            LdapUtils.initLDAP(userName, password, configuration.getLdapServer());
        } catch (AuthenticationException e){
            errors.add(EscomBeanUtils.prepErrorMsg("BadUserOrPassword"));
            makeCountErrLogin(context, errors);
        } catch (Exception ex){
            errors.add(EscomBeanUtils.prepErrorMsg("ConnectLDAPFailed"));
            EscomBeanUtils.ErrorMessage(ex.getLocalizedMessage());
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    /* Проверка корректности пароля */
    private boolean isIncorrectPassword(User user) throws NoSuchAlgorithmException{
        password = EscomUtils.encryptPassword(password.trim());
        return !Objects.equals(password, user.getPassword());
    }
    
    /* Увеличивает счётчик ошибок входа и генерирует сообщение в случае превышения допустимого числа ошибок  */
    private void makeCountErrLogin(RequestContext context, Set<FacesMessage> errors){
        countErrLogin++;
        if (isLoginLock()){
            context.execute("PF('poll').start();");
            errors.add(EscomBeanUtils.prepErrorMsg("ErrorCountLogin"));
        } 
    }
    
    /* Сброс счётчика количества неудачных попыток входа  */
    public void resetLoginLock(){
        countErrLogin = 0;
    }
      
    /* Установка локали */
    public void changeLocale(){
        sessionBean.changeLocale(selectedLang.getName());
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoginLock(){
        if (countErrLogin > 2){
            return true;
        }
        return false;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public List<CountryFlags> getLanguages() {
        return languages;
    }

    public CountryFlags getSelectedLang() {
        return selectedLang;
    }
    public void setSelectedLang(CountryFlags selectedLang) {
        this.selectedLang = selectedLang;
    }    

    public String getTargetPage() {
        return targetPage;
    }
    public void setTargetPage(String targetPage) {
        this.targetPage = targetPage;
    }
 
    @FacesConverter("langConverter")
    public static class LngConverter implements Converter {    
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
            if(value != null && value.trim().length() > 0) {
                try {
                    LoginBean bean = EscomBeanUtils.findBean("loginBean", fc);
                    Object searcheObj = bean.getLanguages().get(Integer.parseInt(value));
                    return searcheObj;
                } catch(NumberFormatException e) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Некорректное значение."));
                }
            } else {
                return null;
            }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((CountryFlags) object).getId());
            }
            else {
                return "";
            }
        } 
    }
}