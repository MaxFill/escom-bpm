package com.maxfill.escom.beans;

import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.services.numerator.NumeratorService;
import com.maxfill.services.print.PrintService;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.users.User;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.utils.FileUtils;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Базовый бин
 * @author mfilatov
 * @param <T>
 */
public abstract class BaseBean <T extends BaseDict> implements Serializable{
    static final private long serialVersionUID = 3914263308813029722L;
    static final protected Logger LOGGER = Logger.getGlobal();
    
    @Inject
    protected SessionBean sessionBean;
    
    @EJB
    protected PrintService printService;
    @EJB
    protected FavoriteService favoriteService;
    @EJB
    protected NumeratorService numeratorService;
    @EJB
    protected AttacheService attacheService;
    @EJB
    protected RightFacade rightFacade;
    
    private boolean isItemChange;               //признак изменения записи  

    protected User currentUser;
    private List<User> users;
    private Metadates metadatesObj;             //объект метаданных
    public abstract BaseDictFacade getItemFacade(); //установка фасада объекта    
    
    @PostConstruct
    public void init() {
        System.out.println("Создан бин =" + this.toString());
        setCurrentUser(sessionBean.getCurrentUser());
        onSetUserSettings(currentUser);
        onInitBean();
    }
    
    @PreDestroy
    private void destroy(){
        System.out.println("Удалён бин =" + this.toString());
    }
    
    /**
     * ИНИЦИАЛИЗАЦИЯ: действия при создании бина
     */
    public abstract void onInitBean();
    
    /**
     * Установка пользовательских настроек форм и прочего
     *
     * @param user
     */
    protected void onSetUserSettings(User user) {
        //TODO !
        //model.setRowsInPage(userSettings.getRowInPage());
    }
    
    /**
     * СОЗДАНИЕ: создание нового объекта
     *
     * @param owner
     * @return
     */
    public BaseDict createItem(BaseDict owner) {
        return getItemFacade().createItem(owner, currentUser);
    }
    
    /* *** ПРАВА ДОСТУПА *** */    
    
    /**
     * ПРАВА ДОСТУПА: проверка права доступа к объекту с актуализацией
     * @param item
     */
    public void actualizeRightItem(BaseDict item){       
        if (item == null){
            throw new NullPointerException("Item null in actualize right metod!");
        }
        sessionBean.actualizeRightItem(item);
    }
    
    /**
     * ПРАВА ДОСТУПА: Проверяет право текущего пользователя на удаление объекта
     *
     * @param item
     * @return
     */
    public Boolean isHaveRightDelete(BaseDict item) {
        return sessionBean.checkMaskDelete(item);
    }

    /**
     * ПРАВА ДОСТУПА: проверяет право текущего пользователя на изменение прав доступа к объекту
     *
     * @param item
     * @return
     */
    public boolean isHaveRightChangeRight(T item) {
        return sessionBean.checkMaskRightChangeRight(item);
    }          
    
    /**
     * СПИСКИ ОБЪЕКТОВ: предварительная обработка списка объектов
     *
     * @param sourceItems
     * @return
     */
    public List<BaseDict> prepareItems(List<BaseDict> sourceItems) {
        List<BaseDict> target = sourceItems.stream()
                .filter(item -> sessionBean.preloadCheckRightView(item))
                .collect(Collectors.toList());
        return target;
    }    
    
    /**
     * ИЗБРАННОЕ: добавление объекта в избранное
     * @param item
     */
    public void addInFavorites(BaseDict item){
        Object[] msg = new Object[]{item.getName()};
        if (favoriteService.addInFavorites(item, metadatesObj, currentUser)){
            EscomBeanUtils.SuccesFormatMessage("Successfully", "ObjectAddedToFavorites", msg);
        } else {
            EscomBeanUtils.WarnFormatMessage("NotExecuted", "ObjectAlreadyAddedFavorites", msg);
        }
    }    

    /* *** ВЛОЖЕНИЯ *** */
    
    /**
     * Просмотр вложения
     *
     * @param attache
     */
    public void onViewAttache(Attaches attache) {
        FileUtils.viewAttache(attache);
    }
    
    /**
     * Скачивание вложения
     * @param attache 
     */
    public void attacheDownLoad(Attaches attache){
       FileUtils.attacheDownLoad(attache);
    }
    
    public Integer getMaxFileSize(){
        return FileUtils.MAX_FILE_SIZE;
    }
    
    /* *** СЛУЖЕБНЫЕ МЕТОДЫ *** */
    
    /**
     * Установка признака изменения объекта. 
     */
    public void onItemChange() {
        isItemChange = true;
    }
    
    /**
     * Признак изменения объекта
     *
     * @return
     */
    public boolean isItemChange() {
        return isItemChange;
    }    
    
    /* *** GET & SET *** */
        
    public List<User> getUsers() {
        if (users == null){
            users = sessionBean.getUserFacade().findAll().stream()
                .filter(item -> sessionBean.preloadCheckRightView(item))
                .collect(Collectors.toList());             
        }
        return users;
    }
        
    public Boolean getIsItemChange() {
        return isItemChange;
    }
    public void setIsItemChange(Boolean isItemChange) {
        this.isItemChange = isItemChange;
    }  
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }  
    
    /**
     * Получение ссылки на объект метаданных
     *
     * @return
     */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getItemFacade().getMetadatesObj();
        }
        return metadatesObj;
    }
}