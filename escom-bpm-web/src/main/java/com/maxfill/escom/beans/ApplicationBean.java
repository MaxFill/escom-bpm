package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictModules;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.core.Release;
import com.maxfill.model.core.licence.Licence;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.core.sessions.UsersSessions;
import com.maxfill.services.licenses.ActivateApp;
import com.maxfill.services.update.UpdateInfo;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import org.apache.commons.collections4.MapUtils;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

@Named
@ApplicationScoped
public class ApplicationBean implements Serializable{
    private static final long serialVersionUID = 2445940557149889740L;

    private static final Logger LOGGER = Logger.getLogger(ApplicationBean.class.getName());

    private static final String ALLOW_FILE_TYPES = "/(\\.|\\/)(pdf|docx|xlsx|xls|doc|rtf|txt|odt|zip|rar|png|tiff|gif|jpg|jpe?g)$/";
    public static final String WSS_INFO_URL = "wss://escom-demo.ru:9443/EscomServices-1.0/release_info";

    private boolean needUpadateSystem;
    private boolean useModeshape;
    
    private Licence licence = null;
    private String appName;
    private final Release release = new Release();
    
    @EJB
    private Configuration configuration;
    @EJB
    private UpdateInfo updateInfo;
    @EJB
    private ActivateApp activateApp;

    //открытые сессии пользователей
    private final ConcurrentHashMap<String, UsersSessions> userSessions = new ConcurrentHashMap<>();
    
    //буфер открытых объектов (key - ключ ItemKey, Tuple(режим открытия(0-чтение, 1 - изменение), сам_объект)
    private final ConcurrentHashMap<String, Tuple<Integer, BaseDict>> openedItems = new ConcurrentHashMap<>();
    
    //объекты (ItemKey), заблокированные пользователем (UserId) 
    private final ConcurrentHashMap<String, User> itemsLock = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        appName = MsgUtils.getBandleLabel(SysParams.APP_NAME);
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();        
        try {
            String serverURL = new URL(request.getScheme(),
                    request.getServerName(),
                    request.getServerPort(),
                    request.getContextPath()).toString();
            configuration.setServerAppURL(serverURL + "/faces/view");
        } catch (MalformedURLException ex) {
            Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException();
        }
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        release.setVersionNumber(ec.getInitParameter("VersionNumber"));
        release.setReleaseNumber(ec.getInitParameter("ReleaseNumber"));
        release.setReleaseDate(DateUtils.convertStrToDate(ec.getInitParameter("ReleaseDate"), "yyyy-MM-dd", locale));
        useModeshape = Boolean.valueOf(ec.getInitParameter("UseModeshape"));
        initLicense();
    }

    public void initLicense(){
        licence = activateApp.initLicense();
    }

    /* БЛОКИРОВКИ ОБЪЕКТОВ  */
    
    /* Добавление объекта в буфер для его блокировки и передачи во view бины
     * @param itemKey Ключ объекта
     * @param editMode Режим открытия
     * @param item  Открываемый объект
     * @param user Кто открыл объект
     * @return  Возвращает ключ открытого объекта  */
    public String addLockedItem(String itemKey, Integer editMode, BaseDict item, User user){
        Tuple<Integer, BaseDict> tuple = new Tuple(editMode, item);
        String itemOpenKey = EscomBeanUtils.makeOpenItemKey(itemKey, editMode, user);
        openedItems.put(itemOpenKey, tuple); //запись в буфер открытых объектов для передачи во view
        if (editMode.equals(DictEditMode.EDIT_MODE)){ //если объект открывается для редактирования, то он блокируется
            itemsLock.put(itemKey, user);
        }
        return itemOpenKey;
    }
    
    /* Удаление объекта из буфера открытых объектов  */
    public void deleteOpenedItem(String itemOpenKey){
        openedItems.remove(itemOpenKey);        
    }
    
    /* Удаление блокировки объекта  */
    public void deleteLockItem(String itemKey){
        itemsLock.remove(itemKey);
    }
    
    /* Получение объекта по его ключу (вызывается из view бина) */
    public Tuple<Integer, BaseDict> getOpenedItemTuple(String key){
        return openedItems.get(key);        
    }
    
    /* Возвращает Id пользователя, заблокировавшего объект (key)  */
    public User whoLockedItem(String itemKey){        
        return itemsLock.get(itemKey);
    }
    
    /* Удаляет все блокировки пользователя  */
    public void clearUserLock(User user){
        itemsLock.values().removeIf(value -> value.equals(user));        
    }

    /* Добавление занятой лицензии  */
    public void addBusyLicence(User user, HttpSession httpSession){
        UsersSessions userSession =  new UsersSessions();
        userSession.setUser(user);
        userSession.setDateConnect(new Date());
        userSession.setIpAdress(""); //TODO Нужен IP адрес!
        userSession.setHttpSession(httpSession);        
        userSessions.put(user.getLogin(), userSession);
    }
    
    /* Освобождение занятой пользователем лицензии  */
    public void clearBasyLicence(String login){        
        userSessions.remove(login);
    }
    
    /* Проверка на то что пользователь уже залогинился  */
    public Boolean isAlreadyLogin(User user){
        return userSessions.containsKey(user.getLogin());
    }
    
    /* Проверка наличия свободных лицензий  */
    public Boolean isNoAvailableLicence(){
        return licence.getTotal() <= getBasyLicence();
    }
    
    /* Возвращает число занятых лицензий */
    public Integer getBasyLicence(){
        return userSessions.size();
    }
    
    /* Отключение сессии пользователя */
    public void disconectUser(UsersSessions usersSession){
        usersSession.getHttpSession().invalidate();
        clearBasyLicence(usersSession.getUser().getLogin());        
    }

    /* Обновление данных об актуальном релизе */
    public void updateActualReleaseData(Map<String,String> releaseInfoMap){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        String version = releaseInfoMap.get("version");
        String number = releaseInfoMap.get("number");
        String page = releaseInfoMap.get("page");
        String dateStr = releaseInfoMap.get("date");
        Date date = DateUtils.convertStrToDate(dateStr, "yyyy-MM-dd", locale);
        updateActualReleaseData(version, number, page, date);
    }
    public synchronized void updateActualReleaseData(String releaseVersion, String releaseNumber, String releasePage, Date releaseDate){
        release.setActualReleaseDate(releaseDate);
        release.setActualReleaseNumber(releaseNumber);
        release.setActualVersionNumber(releaseVersion);
        release.setActualReleasePage(releasePage);
    }

    /* Определяет что срок лицензии истёк */
    public boolean isLicenseExpire(){
        return licence.isExpired();
    }

    /* Установка признака наличия новой версии */
    public void checkNewVersionAvailable(){
        Map<String,String> releaseInfoMap = updateInfo.start(licence.getNumber(), WSS_INFO_URL);
        if (MapUtils.isEmpty(releaseInfoMap)){
            LOGGER.log(Level.SEVERE, "CheckNewVersion: Failed to connect to the service informing about new versions!");
            return;
        }

        updateActualReleaseData(releaseInfoMap);

        Date dateReleas = release.getReleaseDate();
        Date dateActual = release.getActualReleaseDate();

        if (dateActual == null){
            dateActual = DateUtils.clearDate(new Date());
            dateReleas = DateUtils.clearDate(DateUtils.addMounth(dateReleas, 3));
        }

        if (dateActual.after(dateReleas)){
            setNeedUpadateSystem(Boolean.TRUE);
        }
    }

    /**
     * Возвращает дату окончания лицензии
     * @return
     */
    public Date getLicenseExpireDate(){
        return licence.getDateTerm();
    }


    /**
     * Возвращает имя лицензиата
     * @return
     */
    public String getLicensee(){
        return licence.getLicensor();
    }

    /**
     * Возвращает имя лицензии как ключ bundle
     * @return
     */
    public String getLicenseBundleName(){
        return licence.getName();
    }

    /**
     * Возвращает имя программы локализованное имя программы
     * @return
     */
    public String getAppName() {
        return appName;
    }

    public boolean isCanUsesPartners(){
        return licence.isCanUses(DictModules.MODULE_PARTNERS);
    }
    public boolean isCanUsesStaffs(){
        return licence.isCanUses(DictModules.MODULE_STAFFS);
    }
    public boolean isCanUsesProcess(){
        return licence.isCanUses(DictModules.MODULE_PROCESSES);
    }
    public boolean isCanUsesContracts(){
        return licence.isCanUses(DictModules.MODULE_CONTRACTS);
    }
    public boolean isCanUsesDelo(){
        return licence.isCanUses(DictModules.MODULE_DELO);
    }
    public boolean isCanUsesTasks(){
        return licence.isCanUses(DictModules.MODULE_TASKS);
    }
    
    /* GETS & SETS */

    public boolean isUseModeshape() {
        return useModeshape;
    }
        
    public boolean getNeedUpadateSystem() {
        return needUpadateSystem;
    }
    public void setNeedUpadateSystem(Boolean needUpadateSystem) {
        this.needUpadateSystem = needUpadateSystem;
    }    

    public Licence getLicence() {
        return licence;
    }

    public ConcurrentHashMap<String, UsersSessions> getUserSessions() {
        return userSessions;
    }
    
    public String getALLOW_FILE_TYPES() {
        return ALLOW_FILE_TYPES;
    }

    public Release getRelease() {
        return release;
    }
}
