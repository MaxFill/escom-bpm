
package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.model.BaseDict;
import com.maxfill.model.licence.Licence;
import com.maxfill.model.users.User;
import com.maxfill.model.users.sessions.UsersSessions;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.utils.SysParams;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.MetadatesFacade;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Rights;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.Tuple;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@Named
@ApplicationScoped
public class ApplicationBean implements Serializable{
    private static final long serialVersionUID = 2445940557149889740L;
    
    private Boolean needUpadateSystem = false;
    private Licence licence;    
    
    @EJB
    private RightFacade rightFacade;
    @EJB
    private MetadatesFacade metadatesFacade;
    @EJB
    private Configuration configuration;
    
    //открытые сессии пользователей
    private final ConcurrentHashMap<String, UsersSessions> userSessions = new ConcurrentHashMap<>();
    
    //буфер открытых объектов (key - ключ ItemKey, Tuple(режим открытия(0-чтение, 1 - изменение), сам_объект)
    private final ConcurrentHashMap<String, Tuple<Integer, BaseDict>> openedItems = new ConcurrentHashMap<>();
    
    //объекты (ItemKey), заблокированные пользователем (UserId) 
    private final ConcurrentHashMap<String, Integer> itemsLock = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Rights> defRights = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() { 
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        licence = configuration.getLicence();
        licence.setVersionNumber("1.01");
        licence.setReleaseNumber("001");
        Date releaseDate = DateUtils.convertStrToDate("2017-05-01", locale);
        licence.setReleaseDate(releaseDate);
        licence.setTotalLicence(5);
        licence.setLicenceName(EscomBeanUtils.getBandleLabel("LicenceBaseType")); //TODO надо откуда то получать!
        loadDefaultRights();
    }
    
    /* Получение дефолтных прав доступа объектов */
    private void loadDefaultRights(){
        List<Metadates> metadates = metadatesFacade.findAll();
        metadates.stream().forEach(metadatesObj -> {
            Rights rights = rightFacade.getObjectDefaultRights(metadatesObj);
            defRights.put(metadatesObj.getObjectName(), rights);
        });                
    }
            
    /* Возвращает дефолтные права доступа */
    public Rights getDefaultRights(String metadateId){
        Rights rights = defRights.get(metadateId);
        if (rights == null){
            throw new NullPointerException("Escom: for object " + metadateId + " no have default rights!");
        }
        return rights;
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
            itemsLock.put(itemKey, user.getId());
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
    public Integer whoLockedItem(String itemKey){        
        return itemsLock.get(itemKey);
    }
    
    /* Удаляет все блокировки пользователя  */
    public void clearUserLock(User user){
        itemsLock.values().removeIf(value -> value.equals(user.getId()));        
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
    public synchronized void updateActualReleaseData(String releaseVersion, String releaseNumber, String releasePage, Date releaseDate){
        licence.setActualReleaseDate(releaseDate);
        licence.setActualReleaseNumber(releaseNumber);
        licence.setActualVersionNumber(releaseVersion);
        licence.setActualReleasePage(releasePage);
    }
    
    public String getAPP_NAME() {
        return SysParams.APP_NAME;
    }
                
    /* GET & SET */
    
    public Boolean getNeedUpadateSystem() {
        return needUpadateSystem;
    }
    public void setNeedUpadateSystem(Boolean needUpadateSystem) {
        this.needUpadateSystem = needUpadateSystem;
    }    

    public Licence getLicence() {
        return licence;
    }
    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    public ConcurrentHashMap<String, UsersSessions> getUserSessions() {
        return userSessions;
    }
}
