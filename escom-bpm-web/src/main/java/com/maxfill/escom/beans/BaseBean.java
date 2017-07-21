package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.EscomFileUtils;
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
import com.maxfill.facade.UserFacade;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.states.State;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.bind.JAXB;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/* Базовый бин */
public abstract class BaseBean <T extends BaseDict> implements Serializable{
    static final private long serialVersionUID = 3914263308813029722L;
    static final protected Logger LOGGER = Logger.getGlobal();
    
    @Inject
    protected ApplicationBean appBean;
    @Inject
    protected SessionBean sessionBean;
    @EJB
    protected Configuration conf;
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
    @EJB
    protected UserFacade userFacade;
    @EJB
    protected UserGroupsFacade userGroupsFacade;
    
    private boolean isItemChange;               //признак изменения записи  

    protected User currentUser;
    private Metadates metadatesObj;             //объект метаданных
    
    public abstract BaseDictFacade getItemFacade(); //установка фасада объекта    
    
        
    @PostConstruct
    public void init() {
        //System.out.println("Создан бин =" + this.toString());
        setCurrentUser(sessionBean.getCurrentUser());
        onInitBean();
    }
    
    @PreDestroy
    private void destroy(){
        //System.out.println("Удалён бин =" + this.toString());
    }
    
    public abstract void onInitBean();
    public abstract Class<T> getItemClass();    
    
    /* СОЗДАНИЕ: создание нового объекта   */
    public T createItem(BaseDict owner) {
        return createItem(owner, currentUser);
    }
    
    /* СОЗДАНИЕ: cоздание объекта */
    public T createItem(BaseDict owner, User author) {
        T item = (T) getItemFacade().createItem(author);
        detectParentOwner(item, owner);
        return item;
    }
    
    protected void detectParentOwner(T item, BaseDict owner){
        item.setOwner(owner);
    } 
    
    /* ПРАВА ДОСТУПА  */    
    
    /* ПРАВА ДОСТУПА: Установка и проверка прав при загрузке объекта */
    public Boolean preloadCheckRightView(BaseDict item) {
        Rights rights = getRightItem(item);
        settingRightItem(item, rights);
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);
    }
    
    /* ПРАВА ДОСТУПА: Установка прав объекту для текущего пользователя в текущем состоянии объекта с актуализацией маски доступа  */
    public void settingRightItem(BaseDict item, Rights newRight) {
        if (item != null) {
            item.setRightItem(newRight);
            Integer mask;            
            mask = getAccessMask(item, newRight, currentUser);                         
            item.setRightMask(mask);            
        }
    } 
    
    /* ПРАВА ДОСТУПА: Получение прав объекта.
     * Актуально для линейных не подчинённых объектов  */
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item);
        } 

        return getDefaultRights(item);
    }
    
    /* ПРАВА ДОСТУПА: получение дефолтных прав объекта */
    public Rights getDefaultRights(BaseDict item){
        return appBean.getDefaultRights(item.getClass().getSimpleName());
    }
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на просмотр объекта */
    public boolean isHaveRightView(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);         
    }     
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на создание объекта  */
    public boolean isHaveRightCreate(BaseDict item) {        
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CREATE);
    }        
    
    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на редактирование объекта  */
    public Boolean isHaveRightEdit(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_EDIT);                 
    }
    
    /* ПРАВА ДОСТУПА: Проверяет право текущего пользователя на удаление объекта  */
    public Boolean isHaveRightDelete(BaseDict item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_DELETE);
    }

    /* ПРАВА ДОСТУПА: проверяет право текущего пользователя на изменение прав доступа к объекту  */
    public boolean isHaveRightChangeRight(T item) {
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CHANGE_RIGHT);
    }        
    
    /* ПРАВА ДОСТУПА: возвращает дефолтную маску доступа к объекту для текущего пользователя  */
    /*
    public Integer getDefaultMaskAccess() {
        if (defaultMaskAccess == null) {
            Rights rights = getDefaultRights();
            State state = getMetadatesObj().getStateForNewObj();
            defaultMaskAccess = getAccessMask(state, rights, currentUser);
        }
        return defaultMaskAccess;
    }
      */  
    /* ПРАВА ДОСТУПА: возвращает маску доступа пользователя  */
    public Integer getAccessMask(BaseDict item, Rights sourcesRight, User user) {
        State state = item.getState().getCurrentState();
        Integer userId = user.getId();
        Integer accessMask = 0;
        for (Right right : sourcesRight.getRights()) {  //распарсиваем права 
            if (right.getState().equals(state)) {
                switch (right.getObjType()) {
                    case DictRights.TYPE_USER: {    //права указаны для пользователя
                        if (right.getObjId().equals(userId)) { //это текущий пользователь, добавляем ему права
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case DictRights.TYPE_GROUP: {    //права указаны для группы
                        if (checkUserInGroup(right.getObjId(), user)) {
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case DictRights.TYPE_ROLE: {    //права указаны для роли
                        if (checkUserInRole(item, right.getObjId(), user)) {
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                }
                if (accessMask == 248) {
                    break; //дальше проверять права не нужно, т.к. установлены максимальные права
                }
            }
        }
        return accessMask;
    } 
    
    /* ПРАВА ДОСТУПА: формирование битовой маски доступа */
    private Integer makeAccessMask(Right right, Integer accessMask) {
        if (right.isRead()) {
            accessMask = accessMask | DictRights.RIGHT_VIEW;
        }
        if (right.isUpdate()) {
            accessMask = accessMask | DictRights.RIGHT_EDIT;
        }
        if (right.isCreate()) {
            accessMask = accessMask | DictRights.RIGHT_CREATE;
        }
        if (right.isDelete()) {
            accessMask = accessMask | DictRights.RIGHT_DELETE;
        }
        if (right.isChangeRight()) {
            accessMask = accessMask | DictRights.RIGHT_CHANGE_RIGHT;
        }
        return accessMask;
    }
    
    /* ПРАВА ДОСТУПА: проверяет вхождение текущего пользователя в роль */
    private boolean checkUserInRole(BaseDict item, Integer groupId, User user) {
        UserGroups group = userGroupsFacade.find(groupId);
        String roleName = group.getRoleFieldName();
        if (StringUtils.isBlank(roleName)) return false;
        return getItemFacade().checkUserInRole(item, roleName, user);
    }
    
    /* ПРАВА ДОСТУПА: проверяет вхождение текущего пользователя в группу */
    private boolean checkUserInGroup(Integer groupId, User user) {
        if (groupId == 0) return true; //группа ВСЕ
        
        for (UserGroups userGroup : user.getUsersGroupsList()) {
            if (userGroup.getId().equals(groupId)) {
                return true;
            }
        }        
        return false;
    }
    
    /* ПРАВА ДОСТУПА: формирование прав для объекта  */
    public Rights makeRightItem(BaseDict item) {
        Rights rights = getRightItem(item);
        settingRightItem(item, rights); 
        return rights;
    }  
    
    /* ПРАВА ДОСТУПА: акутализация прав доступа объекта */
    public void actualizeRightItem(BaseDict item){
        BaseDict freshItem = (BaseDict)getItemFacade().find(item.getId()); //актуализируем объект, т.к. он может быть удалён!
        if (freshItem == null){
            item.setRightItem(null); //обнулим права у объекта так как он скорее всего удалён ...
            item.setRightMask(null);
            return;
        }
        Rights freshRights = getRightItem(freshItem);
        settingRightItem(item, freshRights);
    }
    
    /* ПРАВА ДОСТУПА: проверка маски доступа к объекту */
    public boolean checkMaskAccess(Integer mask, Integer right) {
        if (mask == null) return false;
        Integer m = mask & right;
        return m.equals(right);
    }
    
    /* ПРАВА ДОСТУПА: получение актуальных прав объекта  */
    protected Rights getActualRightItem(BaseDict item) {        
        if (item.getRightItem() != null){
            return item.getRightItem();
        }
        Rights actualRight = null;
        byte[] compressXML = item.getAccess();
        if (compressXML != null && compressXML.length >0){
            try {
                String accessXML = EscomUtils.decompress(compressXML);            
                StringReader access = new StringReader(accessXML);         
                actualRight = (Rights) JAXB.unmarshal(access, Rights.class);  
                settingRightItem(item, actualRight);
            } catch (IOException ex) {
                Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new NullPointerException("EscomERR: Object " + item.getName() + " dont have xml right!");
        } 
        return actualRight;
    }
    
    /* ПРАВА ДОСТУПА: дефолтные права доступа к объекту */
    public Rights getDefaultRights(){
        return appBean.getDefaultRights(getItemClass().getSimpleName());
    }                 
    
    public void saveAccess(T item, String xml){        
        try {
            byte[] compressXML = EscomUtils.compress(xml);
            item.setAccess(compressXML);
        } catch (IOException ex) {
            Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveAccessChild(T item, String xml){        
        try {
            byte[] compressXML = EscomUtils.compress(xml);
            item.setAccessChild(compressXML);
        } catch (IOException ex) {
            Logger.getLogger(BaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* *** *** *** */
    
    /* СПИСКИ ОБЪЕКТОВ: предварительная обработка списка объектов  */
    public List<BaseDict> prepareItems(List<T> sourceItems) {
        List<BaseDict> target = sourceItems.stream()
                .filter(item -> preloadCheckRightView(item))
                .collect(Collectors.toList());
        return target;
    }    
    
    /* ИЗБРАННОЕ: добавление объекта в избранное  */
    public void addInFavorites(BaseDict item){
        Object[] msg = new Object[]{item.getName()};
        if (favoriteService.addInFavorites(item, metadatesObj, currentUser)){
            EscomBeanUtils.SuccesFormatMessage("Successfully", "ObjectAddedToFavorites", msg);
        } else {
            EscomBeanUtils.WarnFormatMessage("NotExecuted", "ObjectAlreadyAddedFavorites", msg);
        }
    }    

    /* ВЛОЖЕНИЯ */
    
    /* Просмотр вложения  */
    public void onViewAttache(Attaches attache) {
        String path = conf.getUploadPath() + attache.getFullName();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(FilenameUtils.removeExtension(path) + ".pdf");
        paramMap.put("path", pathList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }    
    
    public void onViewReport(String reportName){
        String pdfFile = new StringBuilder()
                    .append(conf.getTempFolder())
                    .append(reportName)
                    .append("_")
                    .append(currentUser.getLogin())
                    .append(".pdf").toString();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(pdfFile);
        paramMap.put("path", pathList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }
    
    /* Скачивание вложения  */
    public void attacheDownLoad(Attaches attache){
        if (attache == null) return;
        String path = conf.getUploadPath() + attache.getFullName(); 
        EscomFileUtils.attacheDownLoad(path, attache.getName());
    }
        
    /* СЛУЖЕБНЫЕ МЕТОДЫ */    
    
    /* Установка признака изменения объекта  */
    public void onItemChange() {
        isItemChange = true;
    }    
    
    /* Признак изменения объекта  */
    public boolean isItemChange() {
        return isItemChange;
    }    
    
    /* GET & SET */
                
    public Boolean getIsItemChange() {
        return isItemChange;
    }
    public void setIsItemChange(Boolean isItemChange) {
        this.isItemChange = isItemChange;
    }  
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }  
    
    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {
        if (metadatesObj == null) {
            metadatesObj = getItemFacade().getMetadatesObj();
        }
        return metadatesObj;
    }
}