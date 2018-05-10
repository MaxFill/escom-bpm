package com.maxfill.escom.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.Configuration;
import com.maxfill.dictionary.*;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AuthLogFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import com.maxfill.model.users.User;
import com.maxfill.facade.UserFacade;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.escom.beans.users.settings.UserSettings;
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
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.services.files.FileService;
import com.maxfill.services.print.PrintService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.component.themeswitcher.ThemeSwitcher;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.primefaces.extensions.model.layout.LayoutOptions;

/* Cессионный бин приложения */
@SessionScoped
@Named
public class SessionBean implements Serializable{  
    private static final long serialVersionUID = -5356932297321623340L;
    protected static final Logger LOGGER = Logger.getLogger(SessionBean.class.getName());

    //служебное поле для передачи ссылки на право доступа
    private final HashMap<String, Right> sourceRightMap = new HashMap<>(); 
    
    private User currentUser;

    private DashboardModel dashboardModel;
    private Boolean canShowNotifBar = true;
    private String primefacesTheme;   
    private List<String> themes;
    private Locale locale;
    private UserSettings userSettings = new UserSettings();
    private final List<NotifMsg> notifMessages = new ArrayList <>();

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
    @EJB
    protected PrintService printService;
    @EJB
    private AuthLogFacade authLogFacade;
    @EJB
    protected FavoriteService favoriteService;

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
    @Inject
    private ProcessBean processBean;
    @Inject
    private ProcessTypesBean processTypeBean;

    @PostConstruct
    public void init() {                  
        dashboardModel = new DefaultDashboardModel();
        DashboardColumn column1 = new DefaultDashboardColumn();
        DashboardColumn column2 = new DefaultDashboardColumn();
        DashboardColumn column3 = new DefaultDashboardColumn();
        DashboardColumn column4 = new DefaultDashboardColumn();
         
        column4.addWidget("admObjects"); 
        column4.addWidget("services");
        column4.addWidget("loggers");
        
        column3.addWidget("orgStructure");
         
        column2.addWidget("docsExplorer");
        column2.addWidget("dictsExplorer");
        column2.addWidget("processes");
         
        column1.addWidget("userParams");
        column1.addWidget("messages");
 
        dashboardModel.addColumn(column1);
        dashboardModel.addColumn(column2);
        dashboardModel.addColumn(column3); 
        dashboardModel.addColumn(column4);

        temeInit();                
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
        BaseTableBean bean = getItemBean(item);
        return (BaseDict)bean.getFacade().find(item.getId());
    }
    
    public BaseDict prepEditItem(BaseDict item){
        BaseTableBean bean = getItemBean(item);
        return bean.prepEditItem(item);
    }
    
    public BaseDict prepViewItem(BaseDict item){
        BaseTableBean bean = getItemBean(item);
        return bean.prepViewItem(item, new HashSet<>());
    }    
    
    public BaseDict prepPasteItem(BaseDict sourceItem, BaseDict recipient, Set<String> errors){
        BaseTableBean bean = getItemBean(sourceItem);
        BaseDict pasteItem = bean.doPasteItem(sourceItem, recipient, errors);

        if (!errors.isEmpty()) return null;

        if (bean.isNeedCopyOnPaste(sourceItem, recipient)){
            List<List<?>> dependency = bean.doGetDependency(sourceItem);
            if (CollectionUtils.isNotEmpty(dependency)){
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
        return getItemBean(copyItem).doCopy(copyItem);
    }
    
    /* копирование дочерних и подчинённых объектов */
    private void copyPasteDependency(List<List<?>> dependency, BaseDict pasteItem, Set<String> errors){
        for (List<?> depend : dependency){
            depend.stream().forEach(detailItem -> prepPasteItem((BaseDict)detailItem, pasteItem, errors));
        }        
    }                       
    
    public boolean prepAddItemToGroup(BaseDict item, BaseDict targetGroup){ 
        return ((BaseDetailsBean)getItemBean(item)).addItemToGroup(item, targetGroup);
    }                  
    
    /* ПРОЧИЕ МЕТОДЫ */

    /* Добавление объекта в избранное  */
    public void addInFavorites(BaseDict item, Metadates metadates){
        Object[] params = new Object[]{item.getName()};
        if (favoriteService.addInFavorites(item, metadates, getCurrentUser())){
            EscomMsgUtils.succesFormatMsg("ObjectAddedToFavorites", params);
        } else {
            EscomMsgUtils.warnFormatMsg("ObjectAlreadyAddedFavorites", params);
        }
    }

    /* Отображение системной панели напоминания о сроках тех. поддержки и т.п. */
    public void showNotification(){
        if (!canShowNotifBar) return;
        Boolean needUpadateSystem = appBean.getNeedUpadateSystem();
        notifMessages.clear();
        if (needUpadateSystem) {
            String urlCaption = EscomMsgUtils.getMessageLabel("GetInfoAboutNewversion");
            String url = "https://escom-archive.ru/news";
            String msg = EscomMsgUtils.getMessageLabel("NeedUpdateSystem");
            notifMessages.add(new NotifMsg(msg, url, urlCaption));
        }

        if (checkExpiredLicense()){
            String dateExpire = getLicenseExpireAsString();
            String licensee = appBean.getLicensee();
            String remainedDays = DateUtils.differenceDays(Instant.now(), appBean.getLicenseExpireDate().toInstant());
            String msg = MessageFormat.format(EscomMsgUtils.getMessageLabel("LicenseExpired"), new Object[]{licensee, dateExpire, remainedDays});
            String url = "https://escom-archive.ru/faqs/";
            String urlCaption = EscomMsgUtils.getMessageLabel("UpdateInformation");
            notifMessages.add(new NotifMsg(msg, url, urlCaption));
        }
        if (!notifMessages.isEmpty()){
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('notifBar').show();");
        }
        canShowNotifBar = false;
        //ToDo вызов проверки сообщений нужно отсюда убрать!
        checkUnReadMessages();
    }          
    
    /* Обработка события выхода из программы, завершения сессии */
    public void onSessionExit() {
        doSessionExit(SysParams.LOGOUT_PAGE);
    }
    
    /* Завершение сессии пользователя  */           
    private void doSessionExit(String page) {
        doSaveUserSettings();
        appBean.clearUserLock(currentUser);
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        authLogFacade.addAuthExit(currentUser.getLogin(), request);
        redirectToPage(page, Boolean.TRUE);
    }

    /**
     * Обработка события закрытия диалога лицензионного соглашения
     * @param event
     */
    public void onAfterCloseLicenseDlg(SelectEvent event){
        if (event.getObject() == null) return;
        onSessionExit();
    }

    /* Закрытие диалога */
    public String closeDialog(Object param){
        PrimeFaces.current().dialog().closeDynamic(param);
        return goToIndex();
    }

    /* Переход на начальную страницу программы */
    public String goToIndex(){
        return "/view/index?faces-redirect=true";
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /* Возвращает обновлённую версию текущего пользователя  */
    public User getRefreshCurrentUser(){
        currentUser = userFacade.find(currentUser.getId());
        return currentUser;
    }
    
    /* Является ли текущий пользователь администратором  */
    public boolean isUserAdmin(){
        if (DictRights.USER_ADMIN_ID.equals(currentUser.getId())){
            return true;
        }
        UserGroups groupAdmin = currentUser.getUsersGroupsList().stream().filter(userGroup -> userGroup.getId() == 1).findFirst().orElse(null);
        return groupAdmin != null;
    }

    /**
     * Обработка события после открытия экранной формы рабочего места
     */
    public void onAfterFormLoad(){
        checkUserMastChangePwl();
        if (!userSettings.isAgreeLicense()){
            PrimeFaces.current().executeScript("document.getElementById('mainFRM:btnLicense').click()");
        }
    }

    /**
     * Открытие окна смены пароля в случае если у пользователя установлен признак необходимости его смены
     */
    public void checkUserMastChangePwl(){
        if (currentUser.isNeedChangePwl()){
            openSettingsForm();
        }
    }

    /**
     * Вывод сообщения о количестве непрочитанных сообщений
     */
    public void checkUnReadMessages(){
        Integer countUnreadMessage = getCountUnreadMessage();
        if (countUnreadMessage > 0){
            EscomMsgUtils.succesFormatMsg("YouHaveUnreadMessage", new Object[]{countUnreadMessage});
        }
    }
    
    /* Колво непрочитанных сообщений у текущего пользователя */
    public Integer getCountUnreadMessage(){
        return messagesFacade.getCountUnReadMessage(currentUser);        
    }

    /* Просмотр вложения  */
    public void onViewAttache(Attaches attache) {
        String path = configuration.getUploadPath() + attache.getFullName();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(FilenameUtils.removeExtension(path) + ".pdf");
        paramMap.put("path", pathList);
        openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }

    /* Скачивание вложения  */
    public void attacheDownLoad(Attaches attache){
        if (attache == null) return;
        String path = configuration.getUploadPath() + attache.getFullName();
        EscomFileUtils.attacheDownLoad(path, attache.getName());
    }

    /**
     * Открытие формы диалога
     * @param frmName
     * @param paramMap
     */
    public void openDialogFrm(String frmName, Map<String, List<String>> paramMap){
        EscomBeanUtils.openDlgFrm(frmName, paramMap, getFormSize(frmName));
    }

    /**
     * Открытие формы задачи 
     * @param beanId 
     * @param beanName 
     */
    public void openTask(String beanId, String beanName) {
        String formName = DictDlgFrmName.FRM_TASK + "-card";
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> itemIds = new ArrayList<>();
        itemIds.add(beanId);
        paramMap.put(SysParams.PARAM_BEAN_ID, itemIds);
        List<String> beanNameList = new ArrayList<>();
        beanNameList.add(beanName);
        paramMap.put(SysParams.PARAM_BEAN_NAME, beanNameList);
        openDialogFrm(formName.toLowerCase(), paramMap);        
    }

    /**
     * Открытие обозревателя процессов
     * @return
     */
    public String openProcessExpl(){
        String url = "";
        if (appBean.getLicence().isCanUses(DictModules.MODULE_PROCESSES)){
            url = "/view/processes/" + DictDlgFrmName.FRM_PROCESS_EXPL + "?faces-redirect=true";
        }
        return url;
    }

    /**
     * Открытие обозревателя документов
     * @param filterId
     * @return 
     */
    public String openDocExplorer(String filterId){
        return "/view/docs/" + DictDlgFrmName.FRM_DOC_EXPLORER + "?faces-redirect=true&filterId=" + filterId;
    }

    /* Открытие карточки администрирования объекта */
    public void openAdmCardForm(BaseDict item){
        String beanName = item.getClass().getSimpleName();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> beanNameList = new ArrayList<>();
        List<String> itemIds = new ArrayList<>();
        beanNameList.add(beanName);
        itemIds.add(String.valueOf(item.getId()));
        paramMap.put("beanName", beanNameList);
        paramMap.put("itemId", itemIds);
        openDialogFrm(DictDlgFrmName.FRM_OBJECT_ADMIN, paramMap);
    }
    
    /* Открытие окна сканирования */
    public void openScaningForm(){
        Map<String, List<String>> paramMap = new HashMap<>();
        openDialogFrm(DictDlgFrmName.FRM_SCANING, paramMap);
    }

    /* Открытие окна пользовательских сессий */
    public void openSessionsForm(){
        Map<String, List<String>> paramMap = new HashMap<>();
        openDialogFrm(DictDlgFrmName.FRM_USER_SESSIONS, paramMap);
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

    /**
     * Открытие диалога журнала аутентификации
     */
    public void openAuthLog(){
        openDialogFrm(DictDlgFrmName.FRM_AUTH_LOG, new HashMap<>());
    }

    /* Просмотр файла PDF в диалоговом окне */
    public void onViewReport(String reportName){
        String pdfFile = new StringBuilder()
                .append(configuration.getTempFolder())
                .append(reportName)
                .append("_")
                .append(getCurrentUser().getLogin())
                .append(".pdf").toString();
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        pathList.add(pdfFile);
        paramMap.put("path", pathList);
        openDialogFrm(DictDlgFrmName.FRM_DOC_VIEWER, paramMap);
    }

    /**
     * Подготовка отчёта для вывода на просмотр
     * @param dataReport
     * @param parameters
     * @param reportName
     */
    public void preViewReport(List<Object> dataReport, Map<String, Object> parameters, String reportName){
        printService.doPrint(dataReport, parameters, reportName);
        onViewReport(reportName);
    }

    /* Открытие формы почтовой службы отправки e-mail сообщений */
    public void openMailSenderService(){
        openDialogFrm(DictDlgFrmName.FRM_MAIL_SENDER_SERVICE, new HashMap<>());
    }

    /* Открытие формы почтовой службы получения e-mail сообщений */    
    public void openMailReaderService(){        
        openDialogFrm(DictDlgFrmName.FRM_MAIL_READER_SERVICE, new HashMap<>());
    }

    /* Открытие формы службы интеграции с LDAP */
    public void openLdapService(){
        openDialogFrm(DictDlgFrmName.FRM_LDAP, new HashMap<>());
    }

    /* Открытие формы службы формирования системных уведомлений */
    public void openNotificationService(){
        openDialogFrm(DictDlgFrmName.FRM_NOTIFICATION, new HashMap<>());
    }
    
    /* Открытие формы списка сообщений пользователя */
    public void openUserMessagesForm(String typeMsg){
        List<String> msgList = new ArrayList<>();
        msgList.add(typeMsg);
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put("typeMsg", msgList);
        openDialogFrm(DictDlgFrmName.FRM_USER_MESSAGES, paramMap);
    }
    
    /* Открытие окна счётчиков нумераторов */
    public void openContersExpl(){        
        openDialogFrm(DictDlgFrmName.FRM_COUNTERS, new HashMap<>());
    }

    /* Открытие окна планировщика */
    public void openScheduler(){
        openDialogFrm(DictDlgFrmName.FRM_SCHEDULER, new HashMap<>());
    }
    
    /* Инициализация спиcка тем */
    private void temeInit(){
        themes = new ArrayList<>();
        themes.add("afterdark");
        themes.add("afternoon");
        themes.add("afterwork");
        themes.add("aristo");
        themes.add("black-tie");
        themes.add("blitzer");
        themes.add("bluesky");
        themes.add("bootstrap");
        themes.add("casablanca");
        themes.add("cupertino");
        themes.add("cruze");
        themes.add("dark-hive");
        themes.add("delta");
        themes.add("dot-luv");
        themes.add("eggplant");
        themes.add("flick");
        themes.add("glass-x");
        themes.add("home");
        themes.add("hot-sneaks");
        themes.add("humanity");
        themes.add("le-frog");
        themes.add("midnight");
        themes.add("mint-choc");
        themes.add("overcast");
        themes.add("pepper-grinder");
        themes.add("redmond");
        themes.add("rocket");
        themes.add("sam");
        themes.add("smoothness");
        themes.add("south-street");
        themes.add("start");
        themes.add("sunny");
        themes.add("ui-darkness");
        themes.add("ui-lightness");
        themes.add("vader");
    }

    /**
     * Возврашает размеры формы диалога, сохранённые в настройках пользователя
     * @param formName
     * @return
     */
    public Tuple<Integer, Integer> getFormSize(String formName){
        Map<String, Tuple<Integer, Integer>> formsSize = userSettings.getFormsSize();
        Tuple<Integer, Integer> rezult;
        if (formsSize.containsKey(formName)){
            rezult = formsSize.get(formName);            
        } else {
            rezult = new Tuple(800, 420);
            formsSize.put(formName, rezult);
        }
        return rezult;
    }

    /**
     * Сохранение размера формы диалога в настройках пользователя
     * @param formName
     * @param width
     * @param heaght
     */
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
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
    
    /* Отображение справки */
    public void onViewHelp(){
       openDialogFrm(DictDlgFrmName.FRM_HELP, null); 
    }
            
    /* GETS & SETS */
            
    public String getLicenseLocalName(){
        return EscomMsgUtils.getBandleLabel(appBean.getLicenseBundleName());
    }

    /* Проверяет, истекает ли срок лицензии в течении месяца */
    public Boolean checkExpiredLicense(){
        Date currentClearDate = DateUtils.clearDate(new Date());
        Date licenceClearDate = DateUtils.addMounth(DateUtils.clearDate(appBean.getLicenseExpireDate()), -1);
        return !licenceClearDate.after(currentClearDate);
    }

    /* Возвращает дату окончания лицензии в виде строки */
    public String getLicenseExpireAsString(){
        Date termLicense = appBean.getLicenseExpireDate();
        return DateUtils.dateToString(termLicense, DateFormat.SHORT, null, getLocale());
    }

    private BaseTableBean getItemBean(BaseDict item){
        return getItemBeanByClassName(item.getClass().getSimpleName());
    }   
    public BaseTableBean getItemBeanByClassName(String className){
        BaseTableBean bean = null;
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
            case DictObjectName.PROCESS:{
                bean = processBean;
                break;
            }
            case DictObjectName.PROCESS_TYPE:{
                bean = processTypeBean;
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

    public Boolean getCanShowNotifBar() {
        return canShowNotifBar;
    }
    public void setCanShowNotifBar(Boolean canShowNotifBar) {
        this.canShowNotifBar = canShowNotifBar;
    }

    public String getPrimefacesTheme() {
        if (StringUtils.isBlank(primefacesTheme)){
            primefacesTheme = "aristo";
        }
        return primefacesTheme;
    }
    public void setPrimefacesTheme(String primefacesTheme) {
        this.primefacesTheme = primefacesTheme;
    }
    
    public void saveTheme(AjaxBehaviorEvent ajax) {
        primefacesTheme = ((String) ((ThemeSwitcher)ajax.getSource()).getValue());
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

    /**
     * Формирует сообщение о количестве новых сообщений у текущего пользователя
     * @return
     */
    public String getMessagesInfo(){
        Integer countMsg = getCountUnreadMessage();
        if (countMsg > 0){
            return countMsg.toString();
        } else {
            return EscomMsgUtils.getBandleLabel("No");
        }
    }

    /* Gets & Sets */

    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public List<String> getThemes() {
        return themes;
    }

    public List <NotifMsg> getNotifMessages() {
        return notifMessages;
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }
    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public class NotifMsg{
        private final String message;
        private final String url;
        private final String urlCaption;

        public NotifMsg(String message, String url, String urlCaption) {
            this.message = message;
            this.url = url;
            this.urlCaption = urlCaption;
        }

        public String getMessage() {
            return message;
        }

        public String getUrl() {
            return url;
        }

        public String getUrlCaption() {
            return urlCaption;
        }

    }
}