package com.maxfill.escom.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.Configuration;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Right;
import com.maxfill.model.users.User;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.beans.users.settings.Theme;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.escom.beans.posts.PostBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.docs.docsTypes.DocTypeBean;
import com.maxfill.escom.beans.docs.docsTypes.docTypeGroups.DocTypeGroupsBean;
import com.maxfill.escom.beans.folders.FoldersBean;
import com.maxfill.escom.beans.partners.PartnersBean;
import com.maxfill.escom.beans.partners.groups.PartnersGroupsBean;
import com.maxfill.escom.beans.partners.types.PartnerTypesBean;
import com.maxfill.escom.beans.system.numPuttern.NumeratorPatternBean;
import com.maxfill.escom.beans.system.statuses.StatusesDocBean;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.dictionary.SysParams;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.files.FileService;
import com.maxfill.utils.EscomUtils;
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
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.inject.Named;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.primefaces.extensions.model.layout.LayoutOptions;

/* Cессионный бин приложения */
@SessionScoped
@Named
public class SessionBean implements Serializable{  
    private static final long serialVersionUID = -5356932297321623340L;
    protected static final Logger LOG = Logger.getLogger(SessionBean.class.getName());
    
    //служебное поле для передачи ссылки на вызвавший бин
    private final HashMap<String, BaseBean> sourceBeansMap = new HashMap<>(); 
    //служебное поле для передачи ссылки на право доступа 
    private final HashMap<String, Right> sourceRightMap = new HashMap<>(); 
    
    private User currentUser;

    private DashboardModel dashboardModel;
    private Boolean canShowNotifBar = true;
    private String primefacesTheme;   
    private List<Theme> themes;
    private Locale locale;
    private UserSettings userSettings = new UserSettings();
    
    @EJB
    protected Configuration configuration;    
    @EJB
    private UserFacade userFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB 
    private UserMessagesFacade messagesFacade;
    @EJB
    protected FileService fileService;
    
    @Inject
    private ApplicationBean appBean;    
    @Inject
    private UserBean usersBean;
    @Inject
    private CompanyBean companyBean;
    @Inject
    private StaffBean staffBean;
    @Inject
    private DocTypeBean docTypeBean;
    @Inject
    private PartnersBean partnerBean;
    @Inject
    private DocBean docBean;
    @Inject
    private StatusesDocBean statusesDocBean;
    @Inject
    private FoldersBean folderBean;
    @Inject
    private PostBean postBean;
    @Inject
    private DepartmentBean departmentBean;
    @Inject
    private UserGroupsBean userGroupBean;
    @Inject
    private PartnersGroupsBean partnerGroupBean;
    @Inject
    private PartnerTypesBean partnerTypeBean;
    @Inject
    private DocTypeGroupsBean docTypeGroupBean;
    @Inject
    private NumeratorPatternBean numeratorPatternBean;
    
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
        
    /* БУФЕР ИСТОЧНИКОВ */
    
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
    
    /* ITEM HELPER */    
        
    public BaseDict reloadItem(BaseDict item){
        BaseExplBean bean = getItemBean(item);
        return (BaseDict)bean.getItemFacade().find(item.getId());
    }
    
    public BaseDict prepEditItem(BaseDict item){
        BaseExplBean bean = getItemBean(item);
        return bean.prepEditItem(item);
    }
    
    public BaseDict prepViewItem(BaseDict item){
        BaseExplBean bean = getItemBean(item);
        return bean.prepViewItem(item);
    }    
    
    public BaseDict prepPasteItem(BaseDict sourceItem, BaseDict recipient, Set<String> errors){
        BaseExplBean bean = getItemBean(sourceItem);       
        BaseDict pasteItem = bean.doPasteItem(sourceItem, recipient, errors);
        
        if (bean.isNeedCopyOnPaste(sourceItem, recipient)){
            List<List<?>> dependency = bean.doGetDependency(sourceItem);
            if (dependency != null){
                copyPasteDependency(dependency, pasteItem, errors);
                pasteItem = bean.findItem(pasteItem.getId());
            }
            return pasteItem;
        } else {
            bean.preparePasteItem(pasteItem, sourceItem, recipient);
            return sourceItem;
        }
    }
    
    public BaseDict prepCopyItem(BaseDict copyItem){
        return getItemBean(copyItem).doCopy(copyItem, currentUser);
    }
    
    /* копирование дочерних и подчинённых объектов */
    private void copyPasteDependency(List<List<?>> dependency, BaseDict pasteItem, Set<String> errors){
        for (List<?> depend : dependency){
            depend.stream().forEach(detailItem -> prepPasteItem((BaseDict)detailItem, pasteItem, errors));
        }        
    }                       
    
    public boolean prepAddItemToGroup(BaseDict item, BaseDict targetGroup){ 
        return getItemBean(item).addItemToGroup(item, targetGroup);
    }                  
    
    /* ПРОЧИЕ МЕТОДЫ */
    
    /* Отображение системной панели напоминания о сроках тех. поддержки и т.п. */
    public void showNotification(){
        Boolean needUpadateSystem = appBean.getNeedUpadateSystem();
        if (canShowNotifBar && needUpadateSystem){
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('bar').show();");
        }
        canShowNotifBar = false;
        checkUnReadMessages();
    }          
    
    /* Метод завершения сессии вызывается со страницы рабочего места пользователя */
    public void onSessionExit() {
        doSessionExit(SysParams.LOGOUT_PAGE);
    }
    
    /* Завершение сессии пользователя  */           
    private void doSessionExit(String page) {
        doSaveUserSettings();
        appBean.clearUserLock(currentUser);
        redirectToPage(page, Boolean.TRUE);
    }

    /* Сохранение настроек текущего пользователя в базу данных */
    private void doSaveUserSettings(){
        if (userSettings == null) return;
        try {
            currentUser = getRefreshCurrentUser();
            userSettings.setTheme(primefacesTheme);
            String settings = userSettings.toString();
            byte[] compressXML = EscomUtils.compress(settings);
            currentUser.setUserSettings(compressXML);
            userFacade.edit(currentUser);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    /* Возвращает обновлённую версию текущего пользователя  */
    public User getRefreshCurrentUser(){
        currentUser = userFacade.find(currentUser.getId());
        return currentUser;
    }
    
    /* Является ли текущий пользователь администратором  */
    public boolean isUserAdmin(){
        if (SysParams.ADMIN_USER_ID.equals(currentUser.getId())){
            return true;
        }
        UserGroups groupAdmin = currentUser.getUsersGroupsList().stream().filter(userGroup -> userGroup.getId() == 1).findFirst().orElse(null);
        return groupAdmin != null;
    }
    
    public void checkUnReadMessages(){
        Integer countUnreadMessage = getCountUnreadMessage();
        if (countUnreadMessage > 0){
            EscomBeanUtils.SuccesFormatMessage("Info", "YouHaveUnreadMessage", new Object[]{countUnreadMessage});            
        }
    }
    
    /* Колво непрочитанных сообщений у текущего пользователя */
    private Integer getCountUnreadMessage(){
        return messagesFacade.getCountUnReadMessage(currentUser);        
    }
    
    public void openDialogFrm(String frmName, Map<String, List<String>> paramMap){
        EscomBeanUtils.openDlgFrm(frmName, paramMap, getFormSize(frmName));
    }
    
    /* Открытие карточки администрирования объекта */
    public void openAdmCardForm(BaseDict item){ 
        BaseExplBean bean = getItemBean(item);
        addSourceBean(bean);
        String beanName = bean.toString();        
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> beanNameList = new ArrayList<>();
        List<String> itemIds = new ArrayList<>();
        beanNameList.add(beanName);
        itemIds.add(String.valueOf(item.getId()));
        paramMap.put("beanName", beanNameList);
        paramMap.put("itemId", itemIds);
        openDialogFrm(DictDlgFrmName.FRM_OBJECT_ADMIN, paramMap);      
    }
        
    /* Открытие карточки записи права  */ 
    public void openRightCard(Integer editMode, State state, String keyRight){
        Integer stateId = state.getId();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> keyRightList = new ArrayList<>();
        List<String> editModeList = new ArrayList<>();
        List<String> stateIdList = new ArrayList<>();
        editModeList.add(editMode.toString());        
        stateIdList.add(stateId.toString());
        keyRightList.add(keyRight);
        paramMap.put("editMode", editModeList);
        paramMap.put("stateId", stateIdList);
        paramMap.put("keyRight", keyRightList);
        openDialogFrm(DictDlgFrmName.FRM_RIGHT_CARD, paramMap);
    }
    
    /* Открытие окна сканирования */
    public void openScaningForm(){
        Map<String, List<String>> paramMap = new HashMap<>();
        openDialogFrm(DictDlgFrmName.FRM_SCANING, paramMap);
    }
    
    /* Открытие окна просмотра лицензии */
    public void openLicenseForm(){
        Map<String, List<String>> paramMap = new HashMap<>();
        openDialogFrm(DictDlgFrmName.FRM_AGREE_LICENSE, paramMap);
    }
    
    /* Открытие формы нового почтового сообщения  */
    public void openMailMsgForm(String mode, List<BaseDict> docs){      
        List<String> openModeList = new ArrayList<>();
        openModeList.add(mode); 
        List<Integer> idList = docs.stream().map(BaseDict::getId).collect(Collectors.toList());
        String docIds = org.apache.commons.lang3.StringUtils.join(idList, ",");
        List<String> docsList = new ArrayList<>();
        docsList.add(docIds); 
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put("modeSendAttache", openModeList);
        paramMap.put("docIds", docsList);
        openDialogFrm(DictDlgFrmName.FRM_MAIL_MESSAGE, paramMap);
    }
    
    /* Открытие формы добавления версии */
    public void openAttacheAddForm(Doc doc){        
        List<String> docsList = new ArrayList<>();
        docsList.add(doc.getId().toString()); 
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put("docId", docsList);
        openDialogFrm(DictDlgFrmName.FRM_ADD_ATTACHE, paramMap); 
    }
    
    /* Открытие формы настроек пользователя */
    public void openSettingsForm(){
        openDialogFrm(DictDlgFrmName.FRM_USER_SETTINGS, new HashMap<>());
    }
    
    /* Открытие формы почтовой службы */
    public void openMailService(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 1300);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog(DictDlgFrmName.FRM_MAIL_SERVICE, options, null);  
    }
    
    public void openLdapService(){
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 1300);
        options.put("height", 600);
        options.put("maximizable", true);
        options.put("closable", false);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        RequestContext.getCurrentInstance().openDialog("/view/services/ldap-users.xhtml", options, null);  
    }    

    public void openUserMessagesForm(){        
        openDialogFrm(DictDlgFrmName.FRM_USER_MESSAGES, new HashMap<>());
    }
    
    /* Переход на начальную страницу программы */
    public String goToIndex(){
        return "/view/index?faces-redirect=true";
    } 

    /* Инициализация спиcка тем */
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
    
    /* Возвращает url по которому автоматически происходит переход на новую страницу  */
    public String onShowDocInFolder(BaseDict doc){
        return EscomBeanUtils.doGetItemURL(doc, "docs/doc-explorer");
    } 
    
    /* Возврашает размеры формы карточки */
    public Tuple<Integer, Integer> getFormSize(String formName){
        Map<String, Tuple<Integer, Integer>> formsSize = userSettings.getFormsSize();
        Tuple<Integer, Integer> rezult;
        if (formsSize.containsKey(formName)){
            rezult = formsSize.get(formName);            
        } else {
            rezult = new Tuple(780, 420);
            formsSize.put(formName, rezult);
        }
        return rezult;
    }
    public void saveFormSize(String formName, Integer width, Integer heaght){
        Tuple<Integer, Integer> size = new Tuple(width, heaght);
        userSettings.getFormsSize().put(formName, size);
    }       
    
    /* редирект на страницу */
    public void redirectToPage(String page, Boolean invalidate){
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        StringBuilder sb = new StringBuilder();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        try {
            URL reconstructedURL = new URL(request.getScheme(),
                    request.getServerName(),
                    request.getServerPort(),
                    "");
            String serverURL = reconstructedURL.toString();            
            sb.append(serverURL).append(ectx.getRequestContextPath()).append(page);
            if (invalidate){
                ectx.invalidateSession();
            }
            ectx.redirect(sb.toString()); 
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } 
    }
    
    /* УСТАНОВКА И ИЗМЕНЕНИЕ ЛОКАЛИ */
    
    public void changeLocale(String lang){
        locale = new Locale(lang);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }   
    public Locale getLocale() {
        if (locale == null){
            locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }   
    
    /* Проверка наличия обновления программы */
    public void onCheckReleaseApp(){       
        openDialogFrm(DictDlgFrmName.FRM_CHECK_RELEASE, null);
    }
    
    /* GETS & SETS */
    
    public String getLicenseLocalName(){
        return EscomBeanUtils.getBandleLabel(appBean.getLicence().getLicenceName());
    }
    
    private BaseExplBean getItemBean(BaseDict item){
        return getItemBeanByClassName(item.getClass().getSimpleName());
    }   
    public BaseExplBean getItemBeanByClassName(String className){
        BaseExplBean bean = null;
        switch(className){
            case DictObjectName.STAFF:{
                bean = staffBean;
                break;
            }
            case DictObjectName.POST:{
                bean = postBean;
                break;
            }
            case DictObjectName.PARTNER_TYPES:{
                bean = partnerTypeBean;
                break;
            }
            case DictObjectName.PARTNER_GROUP:{
                bean = partnerGroupBean;
                break;
            }
            case DictObjectName.PARTNER:{
                bean = partnerBean;
                break;
            }
            case DictObjectName.FOLDER:{
                bean = folderBean;
                break;
            }            
            case DictObjectName.DOC_TYPE:{
                bean = docTypeBean;
                break;
            }
            case DictObjectName.DOC_TYPE_GROUPS:{
                bean = docTypeGroupBean;
                break;
            }
            case DictObjectName.DOC:{
                bean = docBean;
                break;
            }
            case DictObjectName.USER_GROUP:{
                bean = userGroupBean;
                break;
            }
            case DictObjectName.USER:{
                bean = usersBean;
                break;
            }
            case DictObjectName.STATUS_DOCS:{
                bean = statusesDocBean;
                break;
            }
            case DictObjectName.DEPARTAMENT:{
                bean = departmentBean;
                break;
            }
            case DictObjectName.COMPANY:{
                bean = companyBean;
                break;
            }
            case DictObjectName.NUMERATOR_PATTERN:{
                bean = numeratorPatternBean;
                break;
            }
        }
        return bean;
    }   
    
    public Configuration getConfiguration(){        
        return configuration;
    }

    public Staff getCurrentUserStaff() {
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
    public void setCanShowNotifBar(Boolean canShowNotifBar) {
        this.canShowNotifBar = canShowNotifBar;
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

    public LayoutOptions getExplLayoutOptions(String formName) {
        LayoutOptions layoutOptions = new LayoutOptions();
        Map<String, String> explFormMap = userSettings.getExplFormParam();
        if (explFormMap.containsKey(formName)){
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonStr = explFormMap.get(formName);                                    
                layoutOptions = mapper.readValue(jsonStr, LayoutOptions.class);
            } catch (IOException ex) {
                Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            EscomBeanUtils.initLayoutOptions(layoutOptions);
            EscomBeanUtils.initAddLayoutOptions(layoutOptions);
        }
        return layoutOptions;
    }

    public void saveLayoutOptions(LayoutOptions layoutOptions, String frmName) {
        if (layoutOptions.getCenterOptions() == null) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String paneOptions = mapper.writeValueAsString(layoutOptions);
            userSettings.getExplFormParam().put(frmName, paneOptions);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }
    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
         
    public String getMessagesInfo(){
        Integer countMsg = getCountUnreadMessage();
        
        StringBuilder info = new StringBuilder();
        info.append(EscomBeanUtils.getBandleLabel("Messages")).append(" (");
        if (countMsg > 0){
            info.append(EscomBeanUtils.getBandleLabel("CountNewMessages")).append(" ").append(countMsg);
        } else {
            info.append(EscomBeanUtils.getBandleLabel("NoNewMessages"));
        }
        info.append(")");
        return info.toString();
    }
}