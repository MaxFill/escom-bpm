package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.facade.CompanyFacade;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.PostFacade;
import com.maxfill.facade.DocFacade;
import com.maxfill.facade.DocStatusFacade;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.facade.DocTypeGroupsFacade;
import com.maxfill.facade.FiltersFacade;
import com.maxfill.facade.FoldersFacade;
import com.maxfill.facade.PartnersFacade;
import com.maxfill.facade.PartnersGroupsFacade;
import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.rights.Right;
import com.maxfill.facade.StateFacade;
import com.maxfill.model.users.User;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.facade.UserGroupsFacade;
import com.maxfill.escom.beans.users.settings.Theme;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.themeswitcher.ThemeSwitcher;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.xml.bind.JAXB;
import org.primefaces.event.SelectEvent;

/**
 * Бин формы рабочего стола пользователя, а так же сессионный бин приложения
 * @author Maxim
 */
@SessionScoped
@Named
public class SessionBean implements Serializable{  
    private static final long serialVersionUID = -5356932297321623340L;
    protected static final Logger LOG = Logger.getLogger(SessionBean.class.getName());
    private static final String PAGE_LOGOUT = "/faces/logout.xhtml";
    
    //служебное поле для передачи ссылки на вызвавший бин
    private final HashMap<String, BaseBean> sourceBeansMap = new HashMap<>(); 
    //служебное поле для передачи ссылки на право доступа 
    private final HashMap<String, Right> sourceRightMap = new HashMap<>(); 
    
    private User currentUser;

    private DashboardModel dashboardModel;
    private Boolean canShowNotifBar = true;
    private String primefacesTheme;   
    private List<Theme> themes;
    
    private UserSettings userSettings;
    
    @Inject
    private ApplicationBean appBean;
    
    @EJB
    private RightFacade rightFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private CompanyFacade companyFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private DocTypeFacade docTypeFacade;
    @EJB
    private PartnersFacade partnersFacade;
    @EJB
    private DocFacade docsFacade;
    @EJB
    private DocStatusFacade docStatusFacade;
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private PostFacade postFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private UserGroupsFacade usGroupFacade;
    @EJB
    private PartnersGroupsFacade partnersGroupsFacade;
    @EJB
    private PartnerTypesFacade partnerTypesFacade;
    @EJB
    private DocFacade docFacade;
    @EJB
    private DocTypeGroupsFacade docTypeGroupsFacade;
    @EJB
    private FiltersFacade filtersFacade;
    @EJB
    private StateFacade stateFacade;   
    
    @PostConstruct
    public void init() {
        dashboardModel = new DefaultDashboardModel();
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        DashboardColumn column4 = new DefaultDashboardColumn();
         
        column4.addWidget("admObjects"); 
        column4.addWidget("services");
        
        column3.addWidget("orgStructure");
         
        column2.addWidget("docsExplorer");
        column2.addWidget("dictsExplorer");
         
        column1.addWidget("userParams");
 
        dashboardModel.addColumn(column1);
        dashboardModel.addColumn(column2);
        dashboardModel.addColumn(column3); 
        dashboardModel.addColumn(column4);

        temeInit();
    }    
    
    /* *** БУФЕР ИСТОЧНИКОВ *** */
    
    /* Добавление бина источника в буфер */
    public void addSourceBean(BaseBean bean){
        String key = bean.toString();
        sourceBeansMap.put(key, bean);                
    }
    
    /* Получение из буфера бина по его ключу  */
    public BaseBean getSourceBean(String key){
       return sourceBeansMap.get(key);
    }
    
    /* Удаление из буфера бина по его ключу  */
    public void removeSourceBean(String key){
        sourceBeansMap.remove(key);
    }
    
    /* Добавление права объекта источника в буфер  */
    public void addSourceRight(String key, Right right){
        sourceRightMap.put(key, right);
    }
    
    /* Получение из буфера права объекта по его ключу  */
    public Right getSourceRight(String key){
       return sourceRightMap.get(key);
    }
    
    /* Удаление из буфера права объекта по его ключу   */
    public void removeSourceRight(String key){
        sourceRightMap.remove(key);
    }
    
    /* *** ----  *** */
    
    /* Отображение системной панели напоминания о сроках тех. поддержки и т.п. */
    public void showNotifBar(){
        Boolean needUpadateSystem = appBean.getNeedUpadateSystem();
        if (canShowNotifBar && needUpadateSystem){
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('bar').show();");
        }
        canShowNotifBar = false;
    }          
    
    /* Метод завершения сессии вызывается со страницы рабочего места пользователя */
    public void onSessionExit() throws IOException{
        doSessionExit(PAGE_LOGOUT);
    }
    
    /* Завершение сессии пользователя  */           
    private void doSessionExit(String page) throws IOException{
        doSaveUserSettings();
        appBean.clearUserLock(currentUser);
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        URL reconstructedURL = new URL(request.getScheme(),
                               request.getServerName(),
                               request.getServerPort(),
                               "");
        String serverURL = reconstructedURL.toString();
        ectx.invalidateSession();
        ectx.redirect(serverURL +ectx.getRequestContextPath() + page);   
    }

    /* Сохранение настроек текущего пользователя в базу данных */
    private void doSaveUserSettings(){
        currentUser = getRefreshCurrentUser();
        userSettings.setTheme(primefacesTheme);
        currentUser.setUserSettings(userSettings.toString());
        userFacade.edit(currentUser);
    }
    
    /* Возвращает обновлённую версию текущего пользователя  */
    public User getRefreshCurrentUser(){
        currentUser = userFacade.find(currentUser.getId());
        return currentUser;
    }
    
    /* Является ли текущий пользователь администратором  */
    public boolean isUserAdmin(){
        if (currentUser.getId() == 1){
            return true;
        }
        UserGroups groupAdmin =  currentUser.getUsersGroupsList().stream().filter(userGroup -> userGroup.getId() == 1).findFirst().orElse(null);
        return groupAdmin != null;
    }
    
    /* Открытие формы настроек пользователя */
    public void openSettingsForm(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("modal", true);
        options.put("width", 600);
        options.put("height", 400);
        options.put("maximizable", false);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/admin/users/settings", options, null); 
    }
    
    /* Открытие формы почтовой службы */
    public void openMailService(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 1300);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/services/mail-service.xhtml", options, null);  
    }
    
    /* Открытие формы службы интеграции с LDAP */
    public void openLdapService(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 1300);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/services/ldap-users.xhtml", options, null);  
    }

    /* НАВИГАЦИЯ: переход на начальную страницу */
    public String goToIndex(){
        return "/view/index?faces-redirect=true";
    } 

    private void temeInit(){
        themes = new ArrayList<>();
        themes.add(new Theme(0, "Afterdark", "afterdark"));
        themes.add(new Theme(1, "Afternoon", "afternoon"));
        themes.add(new Theme(2, "Afterwork", "afterwork"));
        themes.add(new Theme(3, "Aristo", "aristo"));
        themes.add(new Theme(4, "Black-Tie", "black-tie"));
        themes.add(new Theme(5, "Blitzer", "blitzer"));
        themes.add(new Theme(6, "Bluesky", "bluesky"));
        themes.add(new Theme(7, "Bootstrap", "bootstrap"));
        themes.add(new Theme(8, "Casablanca", "casablanca"));
        themes.add(new Theme(9, "Cupertino", "cupertino"));
        themes.add(new Theme(10, "Cruze", "cruze"));
        themes.add(new Theme(11, "Dark-Hive", "dark-hive"));
        themes.add(new Theme(12, "Delta", "delta"));
        themes.add(new Theme(13, "Dot-Luv", "dot-luv"));
        themes.add(new Theme(14, "Eggplant", "eggplant"));
        themes.add(new Theme(16, "Flick", "flick"));
        themes.add(new Theme(17, "Glass-X", "glass-x"));
        themes.add(new Theme(18, "Home", "home"));
        themes.add(new Theme(19, "Hot-Sneaks", "hot-sneaks"));
        themes.add(new Theme(20, "Humanity", "humanity"));
        themes.add(new Theme(21, "Le-Frog", "le-frog"));
        themes.add(new Theme(22, "Midnight", "midnight"));
        themes.add(new Theme(23, "Mint-Choc", "mint-choc"));
        themes.add(new Theme(24, "Overcast", "overcast"));
        themes.add(new Theme(25, "Pepper-Grinder", "pepper-grinder"));
        themes.add(new Theme(26, "Redmond", "redmond"));
        themes.add(new Theme(27, "Rocket", "rocket"));
        themes.add(new Theme(28, "Sam", "sam"));
        themes.add(new Theme(29, "Smoothness", "smoothness"));
        themes.add(new Theme(30, "South-Street", "south-street"));
        themes.add(new Theme(31, "Start", "start"));
        themes.add(new Theme(32, "Sunny", "sunny"));
        themes.add(new Theme(35, "UI-Darkness", "ui-darkness"));
        themes.add(new Theme(36, "UI-Lightness", "ui-lightness"));
        themes.add(new Theme(37, "Vader", "vader"));
    }
        
    /* *** ПРАВА ДОСТУПА *** */
          
    /* ПРАВА ДОСТУПА: получение дефолтных прав объекта */
    public Rights getDefaultRights(BaseDict item){
        return appBean.getDefaultRights(item.getClass().getSimpleName());
    }
            
    /* ПРАВА ДОСТУПА: актуализация прав доступа к объекту */    
    public boolean checkMaskEdit(BaseDict item){
        return isHaveRightEdit(item);
    }
    
    /* ПРАВА ДОСТУПА: */
    public boolean checkMaskDelete(BaseDict item){
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_DELETE);
    }
    
    /* ПРАВА ДОСТУПА: */
    public boolean checkMaskRightChangeRight(BaseDict item){
        Boolean rezult = checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CHANGE_RIGHT);
        return rezult;
    }
    
    /* ПРАВА ДОСТУПА: акутализация прав доступа объекта */
    public void actualizeRightItem(BaseDict item){
        BaseDict freshItem = (BaseDict)getItemFacade(item).find(item.getId()); //актуализируем объект, т.к. он может быть удалён!
        if (freshItem == null){
            item.setRightItem(null); //обнулим права у объекта так как он скорее всего удалён ...
            item.setRightMask(null);
            return;
        }
        Rights freshRights = getRightItem(freshItem);
        settingRightItem(item, freshRights);
    }
    
    /* ПРАВА ДОСТУПА: формирование прав для объекта  */
    public Rights makeRightItem(BaseDict item) {
        Rights rights = getRightItem(item);
        settingRightItem(item, rights); 
        return rights;
    }
    
    /* ПРАВА ДОСТУПА: формирование прав дочерних объектов */
    public void makeRightForChilds(BaseDict item, BaseDict parent){
        Rights childRights = getRightForChild(parent);
        settingRightForChild(item, childRights); 
    }
    
    /* ПРАВА ДОСТУПА: Установка и проверка прав при загрузке объекта */
    public Boolean preloadCheckRightView(BaseDict item) {
        Rights rights = getRightItem(item);
        settingRightItem(item, rights);
        return checkMaskAccess(item.getRightMask(), DictRights.RIGHT_VIEW);
    }
    
    /* ПРАВА ДОСТУПА: проверка маски доступа к объекту */
    public boolean checkMaskAccess(Integer mask, Integer right) {
        if (mask == null) {
            return false;
        }
        Integer m = mask & right;
        return m.equals(right);
    }
    
    /* ПРАВА ДОСТУПА: Получение прав объекта  */
    public Rights getRightItem(BaseDict item) {
        Rights rights;
        if (item == null) {
            return null;
        }                
        if (!item.isInherits()) {
            rights = getActualRightItem(item);
        } else 
            if (item.getOwner() != null) {
                rights = getRightItem(item.getOwner()); //получаем права от владельца
            } else 
                if (item.getParent() != null) {
                    rights = getRightItem(item.getParent()); //получаем права от родителя
                } else {
                    rights = getDefaultRights(item);
                }
        return rights;
    }       
    
    /* ПРАВА ДОСТУПА: Получение прав для дочерних объектов */
    public Rights getRightForChild(BaseDict item){
        Rights rights;
        if (item == null) {
            return null;
        }                
        if (!item.isInherits()) {
            rights = getActualRightChildItem(item);
        } else 
            if (item.getOwner() != null) {
                rights = getRightForChild(item.getOwner()); //получаем права от владельца
            } else 
                if (item.getParent() != null) {
                    rights = getRightForChild(item.getParent()); //получаем права от родителя
                } else {
                    rights = getDefaultRights(item);
                }
        return rights;
    }
    
    /* ПРАВА ДОСТУПА: Установка прав объекту для текущего пользователя в текущем состоянии объекта с актуализацией маски доступа  */
    public void settingRightItem(BaseDict item, Rights newRight) {
        if (item != null) {
            item.setRightItem(newRight);
            Integer mask = getAccessMask(item.getState(), newRight, currentUser);
            item.setRightMask(mask);
            item.setAccess(newRight.toString()); //сохраняем права в виде XML
        }
    } 
        
    /* ПРАВА ДОСТУПА: Установка прав дочерних объектов их владельцу  */
    public void settingRightForChild(BaseDict ownerItem, Rights newRight) {
        if (ownerItem != null && newRight != null) {
            ownerItem.setRightForChild(newRight);
            ownerItem.setXmlAccessChild(newRight.toString());
        }
    }   
    
    /* ПРАВА ДОСТУПА: получение актуальных прав объекта  */
    private Rights getActualRightItem(BaseDict item) {        
        if (item.getRightItem() != null){
            return item.getRightItem();
        }
        if (StringUtils.isNotBlank(item.getAccess())){
            StringReader access = new StringReader(item.getAccess());         
            Rights actualRight = (Rights) JAXB.unmarshal(access, Rights.class); //Демаршаллинг прав из строки! 
            settingRightItem(item, actualRight);
            return actualRight;
        } else {
            throw new NullPointerException("EscomERR: Object " + item.getName() + " dont have xml right!");
        }            
    }
    
    /* ПРАВА ДОСТУПА: получение актуальных прав объекта от его владельца  */
    private Rights getActualRightChildItem(BaseDict item) {
        //TODO Тут вероятно нужно через вызов абстрактного метода актуализировать данные по правам т.к. в XmlAccessChild ни хрена нет!
        String childStrRight = item.getXmlAccessChild();
        if (StringUtils.isNotBlank(childStrRight)){
            Rights actualRight = (Rights) JAXB.unmarshal(new StringReader(childStrRight), Rights.class);
            return actualRight;
        } 
        return null;
    }
    
    /* ПРАВА ДОСТУПА: возвращает маску доступа пользователя  */
    public Integer getAccessMask(State state, Rights sourcesRight, User user) {
        Integer userId = user.getId();
        Integer accessMask = 0;
        for (Right right : sourcesRight.getRights()) {  //распарсиваем права 
            if (right.getState().equals(state)) {
                switch (right.getObjType()) {
                    case 1: {    //права указаны для пользователя
                        if (right.getObjId().equals(userId)) { //это текущий пользователь, добавляем ему права
                            accessMask = accessMask | makeAccessMask(right, accessMask);
                        }
                        break;
                    }
                    case 0: {    //права указаны для группы
                        if (checkUserInGroup(right.getObjId(), user)) {
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
    
    /* ПРАВА ДОСТУПА: проверяет вхождение текущего пользователя в группу */
    private boolean checkUserInGroup(Integer groupId, User user) {
        for (UserGroups group : user.getUsersGroupsList()) {
            if (group.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
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
    
    /* *** --- ITEM HELPER --- *** */                    
    
    /* Вставка объекта. Возвращает вставленный объект и список ошибок */
    public BaseDict doPasteItem(BaseDict sourceItem, BaseDict recipient, Set<String> errors){
        BaseDictFacade facade = getItemFacade(sourceItem);
        if (facade.isNeedCopyOnPaste(sourceItem, recipient)){
            BaseDict pasteItem = (BaseDict)facade.doCopy(sourceItem, currentUser);            
        //    pasteItem.setDetailItems(null);
            pasteItem.setChildItems(null);
            pasteItem.setId(null);                    //нужно сбросить скопированный id!
            pasteItem.setItemLogs(new ArrayList<>()); //нужно сбросить скопированный log !
            facade.preparePasteItem(pasteItem, recipient);
            prepCreate(pasteItem, pasteItem.getParent(), pasteItem.getOwner(), errors, null);            
            if (!errors.isEmpty()){
                EscomBeanUtils.showErrorsMsg(errors);
                return null;
            }
            changeNamePasteItem(sourceItem, pasteItem);
            facade.create(pasteItem);
            List<List<?>> dependency = facade.doGetDependency(sourceItem);
            if (dependency != null){
                copyPasteDependency(dependency, pasteItem, errors);
                pasteItem = (BaseDict)facade.find(pasteItem.getId());
            }
            return pasteItem;
        } else {
            facade.preparePasteItem(sourceItem, recipient);
            return sourceItem;
        }
    }
    
    /* Копирование зависимых объектов */
    private void copyPasteDependency(List<List<?>> dependency, BaseDict pasteItem, Set<String> errors){
        for (List<?> depend : dependency){
            depend.stream().forEach(detailItem -> doPasteItem((BaseDict)detailItem, pasteItem, errors));
        }        
    }
    
    /* Изменение имени вставляемого объекта */
    private void changeNamePasteItem(BaseDict sourceItem, BaseDict pasteItem){
        if (Objects.equals(sourceItem.getParent(), pasteItem.getParent()) 
            && Objects.equals(sourceItem.getOwner(), pasteItem.getOwner()) ){            
            String name = getBandleLabel("CopyItem") + " " + pasteItem.getName();
            pasteItem.setName(name);
        }
    }
    
    /* Создание объекта с открытием карточки */
    public BaseDict createItemAndOpenCard(BaseDict parent, BaseDict owner, String itemClassName, Map<String, Object> params){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacadeByClassName(itemClassName); 
        BaseDict newItem = facade.createItem(owner, currentUser);
        prepCreate(newItem, parent, owner, errors, params); 
        Tuple<Integer, Integer> formSize = facade.getFormSize();
        openItemCard(newItem, DictEditMode.INSERT_MODE, errors, formSize);        
        return newItem;
    }

    /* Действия перед созданием объекта */
    private void prepCreate(BaseDict newItem, BaseDict parent, BaseDict owner, Set<String> errors, Map<String, Object> params){
        boolean isAllowedEditOwner = true;
        BaseDictFacade facade = getItemFacade(newItem);
        if (owner != null) {
            actualizeRightItem(owner);
            isAllowedEditOwner = isHaveRightEdit(owner); //можно ли редактировать owner?
        }
        if (isAllowedEditOwner) {
            newItem.setParent(parent);
            makeRightItem(newItem);
            if (isHaveRightCreate(newItem)) {                
                if (parent != null){
                    makeRightForChilds(newItem, parent);
                }    
                facade.setSpecAtrForNewItem(newItem, params);
                facade.addLogEvent(newItem, DictLogEvents.CREATE_EVENT, currentUser);
            } else {
                String objName = ItemUtils.getBandleLabel(facade.getMetadatesObj().getBundleName());
                String error = MessageFormat.format(ItemUtils.getMessageLabel("RightCreateNo"), new Object[]{objName});
                errors.add(error);
            }
        } else {
            String objName = ItemUtils.getBandleLabel(facade.getMetadatesObj().getBundleName());
            String error = MessageFormat.format(ItemUtils.getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
    }           
    
    /* Подготовка к редактированию объекта на карточке  */      
    public BaseDict prepEditItem(BaseDict item){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacade(item);
        BaseDict editItem = (BaseDict)facade.find(item.getId());   //получаем копию объекта для редактирования 
        makeRightItem(editItem);
        if (!isHaveRightEdit(editItem)){            
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
        Tuple<Integer, Integer> formSize = facade.getFormSize();
        openItemCard(editItem, DictEditMode.EDIT_MODE, errors, formSize);
        return editItem;
    }
    
    /* Подготовка к просмотру объекта на карточке */
    public BaseDict prepViewItem(BaseDict item){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacade(item);        
        BaseDict editItem = (BaseDict) facade.find(item.getId());   //получаем копию объекта для просмотра 
        makeRightItem(editItem);        
        if (!isHaveRightView(editItem)){ 
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightViewNo"), new Object[]{objName});
            errors.add(error);
        }
        Tuple<Integer, Integer> formSize = facade.getFormSize();
        openItemCard(editItem, DictEditMode.VIEW_MODE, errors, formSize);
        return editItem;
    }
    
    /* Открытие карточки объекта*/
    public void openItemCard(BaseDict item, Integer editMode, Set<String> errors, Tuple<Integer, Integer> formSize){
        if (!errors.isEmpty()){
            EscomBeanUtils.showErrorsMsg(errors);
            return;
        }
        
        String itemKey = item.getItemKey();
        
        if (editMode.equals(DictEditMode.EDIT_MODE)){
            Integer userLockId = appBean.whoLockedItem(itemKey); //узнаём, заблокирован ли уже объект        
            if (userLockId != null){
                User user = rightFacade.findUserById(userLockId);
                String objName = user.getName();
                EscomBeanUtils.ErrorFormatMessage("AccessDenied", "ObjectAlreadyOpened", new Object[]{objName});
                return;
            }
        }

        String itemOpenKey = appBean.addLockedItem(itemKey, editMode, item, getCurrentUser());
        String cardName = item.getClass().getSimpleName().toLowerCase() + "-card";
        EscomBeanUtils.openItemForm(cardName, itemOpenKey, formSize);
    }              
    
    public BaseDict doCopy(BaseDict copyItem){
        return getItemFacade(copyItem).doCopy(copyItem, currentUser);
    }        
    
    public boolean addItemToGroup(BaseDict item, BaseDict targetGroup){ 
        return getItemFacade(item).addItemToGroup(item, targetGroup);
    }    
    
    private BaseDictFacade getItemFacade(BaseDict item){
        return getItemFacadeByClassName(item.getClass().getSimpleName());
    }
    
    public BaseDictFacade getItemFacadeByClassName(String className){
        BaseDictFacade itemFacade = null;
        switch(className){
            case DictObjectName.STATE:{
                itemFacade = stateFacade;
                break;
            }
            case DictObjectName.STAFF:{
                itemFacade = staffFacade;
                break;
            }
            case DictObjectName.POST:{
                itemFacade = postFacade;
                break;
            }
            case DictObjectName.PARTNER_TYPES:{
                itemFacade = partnerTypesFacade;
                break;
            }
            case DictObjectName.PARTNER_GROUP:{
                itemFacade = partnersGroupsFacade;
                break;
            }
            case DictObjectName.PARTNER:{
                itemFacade = partnersFacade;
                break;
            }
            case DictObjectName.FOLDER:{
                itemFacade = foldersFacade;
                break;
            }
            case DictObjectName.FILTERS:{
                itemFacade = filtersFacade;
                break;
            }
            case DictObjectName.DOC_TYPE:{
                itemFacade = docTypeFacade;
                break;
            }
            case DictObjectName.DOC_TYPE_GROUPS:{
                itemFacade = docTypeGroupsFacade;
                break;
            }
            case DictObjectName.DOC:{
                itemFacade = docFacade;
                break;
            }
            case DictObjectName.USER_GROUP:{
                itemFacade = usGroupFacade;
                break;
            }
            case DictObjectName.USER:{
                itemFacade = userFacade;
                break;
            }
            case DictObjectName.DEPARTAMENT:{
                itemFacade = departmentFacade;
                break;
            }
            case DictObjectName.COMPANY:{
                itemFacade = companyFacade;
                break;
            }

        }
        return itemFacade;
    }
    
    public void openLevel1() {
        Map<String,Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        RequestContext.getCurrentInstance().openDialog("test1", options, null);
    }
     
    public void onReturnFromLevel1(SelectEvent event) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Data Returned", ""));
    }
    
    /* *** GET & SET *** */
    
    public Staff getCurrentUserStaff(){        
        List<Staff> staffs = staffFacade.findStaffsByUser(currentUser);
        if (staffs.isEmpty()){
            return null;
        }
        return staffs.get(0);
    }
    public Post getCurrentUserPost(){
        if (getCurrentUserStaff() == null){
            return null;
        }
        return getCurrentUserStaff().getPost();
    }
            
    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }  
    
    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public Boolean getCanShowNotifBar() {
        return canShowNotifBar;
    }

    public String getPrimefacesTheme() {
        if (StringUtils.isBlank(primefacesTheme)){
            primefacesTheme = "flick";
        }
        return primefacesTheme;
    }
    public void setPrimefacesTheme(String primefacesTheme) {
        this.primefacesTheme = primefacesTheme;
    }
    
    public void saveTheme(AjaxBehaviorEvent ajax) {
        primefacesTheme = ((String) ((ThemeSwitcher)ajax.getSource()).getValue());
    }
    public List<Theme> getThemes() {
        return themes;
    } 

    public UserSettings getUserSettings() {
        return userSettings;
    }
    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

}
