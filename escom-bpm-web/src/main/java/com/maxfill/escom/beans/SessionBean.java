package com.maxfill.escom.beans;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.escom.beans.BaseBean;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.companies.Company;
import com.maxfill.facade.CompanyFacade;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.model.staffs.Staff;
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
import com.maxfill.utils.ItemUtils;
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
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
    private final HashMap<String, BaseBean> sourceBeans = new HashMap<>(); 
    //служебное поле для передачи ссылки на право доступа 
    private final HashMap<String, Right> sourceRight = new HashMap<>(); 
    
    private User currentUser;
    private Company selectedCompany;

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
    
    public SessionBean(){         
    };
    
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
    
    /**
     * Добавление бина источника в буфер
     * @param bean
     */
    public void addSourceBean(BaseBean bean){
        String key = bean.toString();
        sourceBeans.put(key, bean);                
    }
    
    /**
     * Получение из буфера бина по его ключу
     * @param key
     * @return 
     */
    public BaseBean getSourceBean(String key){
       return sourceBeans.get(key);
    }
    
    /**
     * Удаление из буфера бина по его ключу
     * @param key 
     */
    public void removeSourceBean(String key){
        sourceBeans.remove(key);
    }
    
    /**
     * Добавление права объекта источника в буфер
     * @param key
     * @param right
     */
    public void addSourceRight(String key, Right right){
        sourceRight.put(key, right);
    }
    
    /**
     * Получение из буфера права объекта по его ключу
     * @param key
     * @return 
     */
    public Right getSourceRight(String key){
       return sourceRight.get(key);
    }
    
    /**
     * Удаление из буфера права объекта по его ключу
     * @param key 
     */
    public void removeSourceRight(String key){
        sourceRight.remove(key);
    }
    
    /* *** ----  *** */
    
    public void showNotifBar(){
        Boolean needUpadateSystem = appBean.getNeedUpadateSystem();
        if (canShowNotifBar && needUpadateSystem){
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('bar').show();");
        }
        canShowNotifBar = false;
    }          
    
    /**
     * Метод завершения сессии вызывается со страницы рабочего места пользователя
     * @throws IOException 
     */
    public void onSessionExit() throws IOException{
        doSessionExit(PAGE_LOGOUT);
    }
    
    /**
     * Завершение сессии пользователя
     * @param page
     */           
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

    /**
     * Сохранение настроек текущего пользователя в базу данных
     */
    private void doSaveUserSettings(){
        currentUser = getRefreshCurrentUser();
        userSettings.setTheme(primefacesTheme);
        currentUser.setUserSettings(userSettings.toString());
        userFacade.edit(currentUser);
    }
    
    /**
     * Возвращает обновлённую версию текущего пользователя
     * @return 
     */
    public User getRefreshCurrentUser(){
        currentUser = userFacade.find(currentUser.getId());
        return currentUser;
    }
    
    /**
     * Является ли текущий пользователь администратором
     * @return 
     */
    public boolean isUserAdmin(){
        if (currentUser.getId() == 1){
            return true;
        }
        UserGroups groupAdmin =  currentUser.getUsersGroupsList().stream().filter(userGroup -> userGroup.getId() == 1).findFirst().orElse(null);
        return groupAdmin != null;
    }
    
    /**
     * Открытие формы настроек пользователя
     */
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
    
    /**
     * Открытие формы почтовой службы 
     */
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
    
    /**
     *  Открытие формы службы интеграции с LDAP
     */
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
    
    /**
     * Закрытие диалога сохранения изменений
     * @param isCancelSave 
     * @return  
     */
    /*
    public void onCloseDialog(Boolean isCancelSave){
        RequestContext.getCurrentInstance().closeDialog(isCancelSave);        
    }
*/
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
    
    /* ПРАВА ДОСТУПА: актуализация прав доступа к объекту */
    public void actualizeRightItem(BaseDict item){
        getItemFacade(item).actualizeRightItem(item, currentUser);
    }
    
    public boolean checkMaskEdit(BaseDict item){
        return getItemFacade(item).isHaveRightEdit(item);
    }
    
    public boolean checkMaskDelete(BaseDict item){
        return getItemFacade(item).checkMaskAccess(item.getRightMask(), DictRights.RIGHT_DELETE);
    }
    
    public boolean checkMaskRightChangeRight(BaseDict item){
        return getItemFacade(item).checkMaskAccess(item.getRightMask(), DictRights.RIGHT_CHANGE_RIGHT);
    }
    
    public boolean preloadCheckRightView(BaseDict item){
        return getItemFacade(item).preloadCheckRightView(item, currentUser);
    }    
    
    /* *** --- ITEM HELPER --- *** */                
    
    public void onPasteItem(BaseDict sourcreItem, BaseDict recipient, Set<String> errors){
        BaseDictFacade facade = getItemFacade(sourcreItem);
        if (facade.isNeedCopyOnPaste()){
            BaseDict pasteItem = facade.doCopy(sourcreItem, currentUser);
            String name = getBandleLabel("CopyItem") + " " + pasteItem.getName();
            pasteItem.setName(name);
            pasteItem.setId(null);                    //нужно сбросить скопированный id!
            pasteItem.setItemLogs(new ArrayList<>()); //нужно сбросить скопированный log !
            facade.pasteItem(pasteItem, recipient, errors);
        } else {
            facade.pasteItem(sourcreItem, recipient, errors);
        }
    }
    
    public boolean addItemToGroup(BaseDict item, BaseDict targetGroup){ 
        return getItemFacade(item).addItemToGroup(item, targetGroup);
    }    
    
    private BaseDictFacade getItemFacade(BaseDict item){
        return getItemFacadeByClassName(item.getClass().getSimpleName());
    }
    
    private BaseDictFacade getItemFacadeByClassName(String className){
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
    
    public BaseDict prepEditItem(BaseDict item){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacade(item);
        BaseDict editItem = facade.prepEditItem(item, currentUser);
        if (editItem == null){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
        doOpenItem(editItem, DictEditMode.EDIT_MODE, errors);
        return editItem;
    }
    
    public BaseDict prepViewItem(BaseDict item){
        Set<String> errors = new HashSet<>();
        BaseDictFacade facade = getItemFacade(item);
        BaseDict editItem = facade.prepViewItem(item, currentUser);
        if (editItem == null){
            String objName = getBandleLabel(facade.getMetadatesObj().getBundleName()) + ": " + item.getName();
            String error = MessageFormat.format(getMessageLabel("RightViewNo"), new Object[]{objName});
            errors.add(error);
        }
        doOpenItem(editItem, DictEditMode.VIEW_MODE, errors);
        return editItem;
    }
    
    /* КАРТОЧКА: открытие карточки объекта*/
    public void doOpenItem(BaseDict item, Integer editMode, Set<String> errors){
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
        EscomBeanUtils.openItemForm(getItemCardName(item), itemOpenKey);
    } 
    
    public String getItemCardName(BaseDict item) {
        return item.getClass().getSimpleName().toLowerCase() + "-card";
    }
         
    /* Создание объекта и открытие карточки */
    public BaseDict createItemAndOpenCard(BaseDict parent, BaseDict owner, String itemClassName, Map<String, Object> params){
        Set<String> errors = new HashSet<>();
        params.put("user", currentUser);
        BaseDict newItem = getItemFacadeByClassName(itemClassName).createItemAndOpenCard(parent, owner, params, errors);        
        doOpenItem(newItem, DictEditMode.INSERT_MODE, errors);        
        return newItem;
    }
    
    public BaseDict doCopy(BaseDict copyItem){
        return getItemFacade(copyItem).doCopy(copyItem, currentUser);
    }
            
    /* *** GET & SET *** */
    
    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }  

    public Company getSelectedCompany() {
        if (selectedCompany == null){
            Staff staff = getCurrentUser().getStaff();
            if (staff != null){
                selectedCompany = staff.getOwner().getOwner();
            }
        }
        return selectedCompany;
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

    public UserFacade getUserFacade() {
        return userFacade;
    }
    public CompanyFacade getCompanyFacade() {
        return companyFacade;
    }
    public StaffFacade getStaffFacade() {
        return staffFacade;
    }
    public DocTypeFacade getDocTypeFacade() {
        return docTypeFacade;
    }
    public PartnersFacade getPartnersFacade() {
        return partnersFacade;
    }
    public DocFacade getDocsFacade() {
        return docsFacade;
    }
    public DocStatusFacade getDocStatusFacade() {
        return docStatusFacade;
    }
    public FoldersFacade getFoldersFacade() {
        return foldersFacade;
    }
    public PostFacade getPostFacade() {
        return postFacade;
    }
    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }
}
