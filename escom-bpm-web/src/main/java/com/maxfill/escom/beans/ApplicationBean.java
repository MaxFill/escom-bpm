package com.maxfill.escom.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.Configuration;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.licence.Licence;
import com.maxfill.model.users.User;
import com.maxfill.model.users.sessions.UsersSessions;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.MetadatesFacade;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Rights;
import com.maxfill.services.update.UpdateInfo;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@Named
@ApplicationScoped
public class ApplicationBean implements Serializable{
    private static final long serialVersionUID = 2445940557149889740L;

    private static final Logger LOGGER = Logger.getLogger(ApplicationBean.class.getName());

    private static final String ALLOW_FILE_TYPES = "/(\\.|\\/)(pdf|docx|xlsx|xls|doc|rtf|txt|odt|zip|rar|png|tiff|gif|jpe?g)$/";
    public static final String WSS_INFO_URL = "wss://escom-demo.ru:8443/escom-bpm-info-1.0-SNAPSHOT/release_info";

    private boolean needUpadateSystem;
    private Licence licence;
    private String appName;

    @EJB
    private Configuration configuration;
    @EJB
    private UpdateInfo updateInfo;

    //открытые сессии пользователей
    private final ConcurrentHashMap<String, UsersSessions> userSessions = new ConcurrentHashMap<>();
    
    //буфер открытых объектов (key - ключ ItemKey, Tuple(режим открытия(0-чтение, 1 - изменение), сам_объект)
    private final ConcurrentHashMap<String, Tuple<Integer, BaseDict>> openedItems = new ConcurrentHashMap<>();
    
    //объекты (ItemKey), заблокированные пользователем (UserId) 
    private final ConcurrentHashMap<String, User> itemsLock = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() { 
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();        
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        licence = configuration.getLicence();
        licence.setVersionNumber(ec.getInitParameter("VersionNumber"));
        licence.setReleaseNumber(ec.getInitParameter("ReleaseNumber"));
        licence.setReleaseDate(DateUtils.convertStrToDate(ec.getInitParameter("ReleaseDate"), locale));
        appName = EscomMsgUtils.getBandleLabel(SysParams.APP_NAME);
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
    
    /* ЛИЦЕНЗИРОВАНИЕ */
    
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
        return licence.getTotalLicence() <= getBasyLicence();
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
        String version = (String) releaseInfoMap.get("version");
        String number = (String) releaseInfoMap.get("number");
        String page = (String) releaseInfoMap.get("page");
        String dateStr = (String) releaseInfoMap.get("date");
        Date date = DateUtils.convertStrToDate(dateStr, locale);
        updateActualReleaseData(version, number, page, date);
    }
    public synchronized void updateActualReleaseData(String releaseVersion, String releaseNumber, String releasePage, Date releaseDate){
        licence.setActualReleaseDate(releaseDate);
        licence.setActualReleaseNumber(releaseNumber);
        licence.setActualVersionNumber(releaseVersion);
        licence.setActualReleasePage(releasePage);
    }

    /* Возвращает дату окончания лицензии */
    public Date getLicenseExpireDate(){
        return licence.getDateTermLicence();
    }

    /* Определяет что срок лицензии истёк */
    public boolean isLicenseExpire(){
        return licence.isExpired();
    }

    /* Установка признака наличия новой версии */
    public void checkNewVersionAvailable(){
        /*
        String actualReleasesJSON = EscomUtils.getReleaseInfo(licence.getLicenceNumber());

        if (StringUtils.isBlank(actualReleasesJSON)) {
            LOGGER.log(Level.SEVERE, "CheckNewVersion: Failed to connect to the service informing about new versions!");
            return;
        }
        */

        Map<String,String> releaseInfoMap = updateInfo.start(licence.getLicenceNumber(), WSS_INFO_URL);
        if (MapUtils.isEmpty(releaseInfoMap)){
            LOGGER.log(Level.SEVERE, "CheckNewVersion: Failed to connect to the service informing about new versions!");
            return;
        }

        updateActualReleaseData(releaseInfoMap);

        Date dateReleas = licence.getReleaseDate();
        Date dateActual = licence.getActualReleaseDate();

        if (dateActual == null){
            dateActual = DateUtils.clearDate(new Date());
            dateReleas = DateUtils.clearDate(DateUtils.addMounth(dateReleas, 3));
        }

        if (dateActual.after(dateReleas)){
            setNeedUpadateSystem(Boolean.TRUE);
        }
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
        return licence.getLicenceName();
    }

    /**
     * Возвращает имя программы по bundle ключу
     * @return
     */
    public String getAppName() {
        return appName;
    }

    /* GETS & SETS */
    
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
      
}
