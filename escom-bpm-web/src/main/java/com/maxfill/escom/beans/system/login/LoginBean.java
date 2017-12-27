package com.maxfill.escom.beans.system.login;

import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.users.User;
import com.maxfill.facade.UserFacade;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    protected static final Logger LOGGER = Logger.getLogger(LoginBean.class.getName());
    
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
    
    public void login() throws NoSuchAlgorithmException{
        Set<FacesMessage> errors = new HashSet<>(); 
        RequestContext context = RequestContext.getCurrentInstance();
                
        if (appBean.getLicence().isExpired()){
            Date termLicense = appBean.getLicence().getDateTermLicence();            
            String dateTerm = DateUtils.dateToString(termLicense, DateFormat.SHORT, null, sessionBean.getLocale());
            errors.add(EscomMsgUtils.prepFormatErrorMsg("ErrorExpireLicence", new Object[]{dateTerm}));
        }
        
        if (appBean.isNoAvailableLicence()){
            errors.add(EscomMsgUtils.prepErrorMsg("ErrorCountLogin"));
        }
        
        User user = userFacade.checkUserLogin(userName, password.toCharArray());
        
        if (user == null){
            errors.add(EscomMsgUtils.prepErrorMsg("BadUserOrPassword"));
        }
        
        if (!errors.isEmpty()){
            makeCountErrLogin(context, errors);
            EscomMsgUtils.showFacesMessages(errors);
            return;
        }
        
        /*
        if (appBean.isAlreadyLogin(user)){
            errorsKey.add("UserPreviouslyLogged");
            makeCountErrLogin(context, errorsKey);
            return showErrMsg(errorsKey, context);            
        }
        */
        if (initCurrentUser(user)){
            if (StringUtils.isBlank(targetPage) || targetPage.contains(SysParams.LOGIN_PAGE)){
                targetPage = SysParams.MAIN_PAGE;
            }
        } else {
            targetPage = SysParams.AGREE_LICENSE;
        }
        sessionBean.redirectToPage(targetPage, Boolean.FALSE);
    }               
        
    /* Инициализация текущего пользователя */
    private boolean initCurrentUser(User user){
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
                String settingsXML = EscomUtils.decompress(compressXML);
                userSettings = (UserSettings) JAXB.unmarshal(new StringReader(settingsXML), UserSettings.class);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        userSettings.setLanguage(selectedLang.getName());
        sessionBean.setUserSettings(userSettings);
        sessionBean.setPrimefacesTheme(userSettings.getTheme());
        appBean.addBusyLicence(user, httpSession);
        return userSettings.isAgreeLicense();
    }             
    
    /* Увеличивает счётчик ошибок входа и генерирует сообщение в случае превышения допустимого числа ошибок  */
    private void makeCountErrLogin(RequestContext context, Set<FacesMessage> errors){
        countErrLogin++;
        if (isLoginLock()){
            context.execute("PF('poll').start();");
            errors.add(EscomMsgUtils.prepErrorMsg("ErrorCountLogin"));
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

}