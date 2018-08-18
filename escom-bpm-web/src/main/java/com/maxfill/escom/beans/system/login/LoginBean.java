package com.maxfill.escom.beans.system.login;

import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.authlog.AuthLogFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.UserFacade;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.services.sms.SmsService;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;

/* Контролер формы логина */
@Named
@ViewScoped
public class LoginBean extends BaseViewBean{    
    private static final long serialVersionUID = 4390983938416752289L;

    private String userName;
    private String password;
    private List<CountryFlags> languages;
    private CountryFlags selectedLang;
    private String targetPage;
    private Integer countErrLogin = 0;
    private String pinCode;             //код, вводимый пользователем на форме
    private String generatePinCode;     //код, генерируемый сервисом и отправляемый на мобильник
    private User user;

    @EJB 
    private UserFacade userFacade;
    @EJB
    private SmsService smsService;
    @EJB
    private AuthLogFacade authLogFacade;
    
    @Override
    public void initBean(){
    if (appBean.getLicence() == null) {
            sessionBean.redirectToPage(SysParams.ACTIVATE_PAGE, Boolean.FALSE);
            return;
        }
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
        if (selectedLang == null){
            selectedLang = new CountryFlags(0, "en", "English");
        }
    };
    
    @Override
    public void onBeforeOpenCard(){
       beanId = this.toString();
    }
    
    public void login() throws NoSuchAlgorithmException{
        Set <FacesMessage> errors = new HashSet <>();
       
        if (!Objects.equals(pinCode, generatePinCode)){   //оба кода должны быть равны (null если не требуется ввод кода)
            errors.add(MsgUtils.prepFormatErrorMsg("BadAccessCode", new Object[]{}));
            makeCountErrLogin(errors);
            MsgUtils.showFacesMessages(errors);
            return;
        }

        //проверка на просроченность лицензии
        if(appBean.isLicenseExpire()) {
            String dateTerm = sessionBean.getLicenseExpireAsString();
            errors.add(MsgUtils.prepFormatErrorMsg("ErrorExpireLicence", new Object[]{dateTerm}));
        }

        if(appBean.isNoAvailableLicence()) {
            errors.add(MsgUtils.prepFormatErrorMsg("ErrorCountLogin", new Object[]{}));
        }

        if (user == null) {
            user = userFacade.checkUserLogin(userName, password.toCharArray());
        }

        if(user == null) {
            errors.add(MsgUtils.prepFormatErrorMsg("BadUserOrPassword", new Object[]{}));
        }

        if(!errors.isEmpty()) {
            makeCountErrLogin(errors);
            MsgUtils.showFacesMessages(errors);
            return;
        }

        if(smsService.isActive() && StringUtils.isBlank(generatePinCode) && user.isDoubleFactorAuth() && StringUtils.isNotBlank(user.getMobilePhone())) {
            generatePinCode = smsService.generatePinCode();
            String message = MessageFormat.format(MsgUtils.getFromBundle("YourAccessCode", "msg"), new Object[]{generatePinCode});
            String smsResult = smsService.sendAccessCode(user.getMobilePhone(), message);

            if(StringUtils.isNotBlank(smsResult) && !smsResult.contains("error")) {
                PrimeFaces.current().ajax().update("loginFRM");
                MsgUtils.succesFormatMsg("SendCheckCodePhone", new Object[]{EscomUtils.makeSecureFormatPhone(user.getMobilePhone())});
                return; //код доступа отправлен, нужен ввод полученного кода, поэтому выходим
            } else {
                System.out.println("ERROR_SMS: " + smsResult == null ? "no data." : smsResult);
            }
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
        generatePinCode = null;
        //return targetPage + "?faces-redirect=true";
        //Используется ajax и редирект/ Если не использовать ajax, то в Chrome почему то сбрасывает selectedLang и далее ошибка.
        sessionBean.redirectToPage(targetPage, Boolean.FALSE);
        sessionBean.killBean(getBeanName(), beanId);        
    }
    
    /* Инициализация текущего пользователя */
    private void initCurrentUser(User user){
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("UserLogin", userName);
        authLogFacade.addAuthEnter(userName, request, isNeedPinCode());
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
    }             
    
    /* Увеличивает счётчик ошибок входа и генерирует сообщение в случае превышения допустимого числа ошибок  */
    private void makeCountErrLogin(Set<FacesMessage> errors){
        countErrLogin++;
        if (isLoginLock()){
            PrimeFaces.current().executeScript("PF('poll').start();");
            errors.add(MsgUtils.prepFormatErrorMsg("ErrorCountLogin", new Object[]{}));
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
    
    public boolean isLoginLock(){
        if (countErrLogin > 2){
            return true;
        }
        return false;
    }

    /* если флаг установлен, то пользователь должен ввести код доступа с мобильника */
    public boolean isNeedPinCode() {
        return StringUtils.isNotBlank(generatePinCode);
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

    public String getPinCode() {
        return pinCode;
    }
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    @Override
    public String getFormName() {
        return "login";
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Login");
    }
}