package com.maxfill.escom.beans;

import com.maxfill.Configuration;
import com.maxfill.dictionary.*;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.types.ProcessTypesBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.authlog.AuthLogFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.core.rights.Right;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.escom.beans.users.settings.UserSettings;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.beans.users.settings.DashBoardSettings;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.post.Post;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.model.WithDatesPlans;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.core.forms.FormsSettings;
import com.maxfill.model.core.forms.FormsSettingsFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.services.favorites.FavoriteService;
import com.maxfill.services.files.FileService;
import com.maxfill.services.print.PrintService;
import com.maxfill.services.worktime.DayType;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
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
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.MeterGaugeChartModel;
import org.primefaces.model.chart.PieChartModel;

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
    private final List<Attaches> attaches = new ArrayList<>();
    private BigDecimal totalfilesSize;
    private MeterGaugeChartModel meterFileSize;
    protected PieChartModel taskPieModel;
    
    private Date taskDateStart;
    private Date taskDateEnd;
    private String taskPeriod;
    private Integer taskTypeId;
    private Integer countTasks = 0;
    
    private final List<SelectItem> PERIODS = Collections.unmodifiableList(
        new ArrayList<SelectItem>() {
            private static final long serialVersionUID = 3109256773218160485L;
            {
                add(new SelectItem(null, MsgUtils.getBandleLabel("EmptySelData")));
                add(new SelectItem("today", MsgUtils.getBandleLabel("Today")));
                add(new SelectItem("curWeek", MsgUtils.getBandleLabel("CurrentWeek")));
                add(new SelectItem("сurMonth", MsgUtils.getBandleLabel("CurrentMonth")));
                add(new SelectItem("curQuarter", MsgUtils.getBandleLabel("CurrentQuarter")));
                add(new SelectItem("curYear", MsgUtils.getBandleLabel("CurrentYear")));
                add(new SelectItem("period", MsgUtils.getBandleLabel("Period")));
            }
        });
    
    private final List<DayType> dayTypes = Collections.unmodifiableList(
        new ArrayList<DayType>() {
            private static final long serialVersionUID = 3109256773218160485L;
            {
                add(new DayType(null, MsgUtils.getBandleLabel("EmptySelData"), ""));
                add(new DayType(DateUtils.WORKDAY, MsgUtils.getBandleLabel("Workday"), "portfolio"));
                add(new DayType(DateUtils.WEEKEND, MsgUtils.getBandleLabel("Weekend"), "weekend"));
                add(new DayType(DateUtils.HOLLYDAY, MsgUtils.getBandleLabel("Hollyday"), "holyday"));
                add(new DayType(DateUtils.HOSPITALDAY, MsgUtils.getBandleLabel("HospitalDay"), "hospital"));
                add(new DayType(DateUtils.MISSIONDAY, MsgUtils.getBandleLabel("MissionDay"), "mission"));                
            }
        });
    
    private final List<DashBoardSettings> dbsList = new ArrayList<>(); //список доступных пользователю виджетов
    
    private DashBoardSettings[] dbsChecked; //отображаемые виджеты
    
    //буфер бинов 
    private final ConcurrentHashMap<String, BaseView > openedBeans = new ConcurrentHashMap<>();
    
    private String openFormName;
    private boolean isChangeDashboard = true;
    
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
    @EJB
    protected DocFacade docFacade;
    @EJB
    protected ProcessFacade processFacade;
    @EJB
    protected AttacheService attacheService;
    @EJB
    private FormsSettingsFacade formsSettingsFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private AttacheFacade attacheFacade;
    
    @Inject
    private AttacheBean attacheBean;   
    @Inject
    private ApplicationBean appBean;
    @Inject
    private DocBean docBean;
    @Inject
    private ProcessBean processBean;
    @Inject
    private ProcessTypesBean processTypeBean;
    
    @PostConstruct
    public void init(){        
        temeInit();                
    }   
    
    public void initDashBoard(){         
        dbsList.add(new DashBoardSettings("userParams", MsgUtils.getBandleLabel("User"), 1 , 0));
        dbsList.add(new DashBoardSettings("tasks", MsgUtils.getBandleLabel("Tasks"), 2 , 0));
        dbsList.add(new DashBoardSettings("messages", MsgUtils.getBandleLabel("Messages"), 3 , 0));
        
        dbsList.add(new DashBoardSettings("docsExplorer", MsgUtils.getBandleLabel("Documents"), 1 , 1));
        dbsList.add(new DashBoardSettings("processes", MsgUtils.getBandleLabel("Processes"), 2 , 1));        
        
        dbsList.add(new DashBoardSettings("orgStructure", MsgUtils.getBandleLabel("OrgStructure"), 1 , 2));
        dbsList.add(new DashBoardSettings("dictsExplorer", MsgUtils.getBandleLabel("Partners"), 2 , 2));        
        
        if (isUserAdmin()){
            dbsList.add(new DashBoardSettings("admObjects", MsgUtils.getBandleLabel("Administation"), 1 , 3));
            dbsList.add(new DashBoardSettings("services", MsgUtils.getBandleLabel("Services"), 2 , 3));
            dbsList.add(new DashBoardSettings("loggers", MsgUtils.getBandleLabel("Logging"), 3 , 3));            
        }
         
        dbsList.add(new DashBoardSettings("eventFeed", MsgUtils.getBandleLabel("EventFeed"), 1 , 4));
        dbsList.add(new DashBoardSettings("tasks_pie_exe", MsgUtils.getBandleLabel("ExecutionInfo"), 2 , 4));   
        if (isUserAdmin()){
            dbsList.add(new DashBoardSettings("diskInfo", MsgUtils.getBandleLabel("DiskInfo"), 3, 4));
        }                               

        onLoadDashboard();
        
        dbsChecked = new DashBoardSettings[getUserSettings().getDashBoard().size()];
        dbsChecked = getUserSettings().getDashBoard().toArray(dbsChecked);
    }    
    
    public void onLoadDashboard(){
        dashboardModel = new DefaultDashboardModel(); 
        DefaultDashboardColumn col0 = new DefaultDashboardColumn();
        DefaultDashboardColumn col1 = new DefaultDashboardColumn();
        DefaultDashboardColumn col2 = new DefaultDashboardColumn();
        DefaultDashboardColumn col3 = new DefaultDashboardColumn();
        DefaultDashboardColumn col4 = new DefaultDashboardColumn();
        DefaultDashboardColumn col5 = new DefaultDashboardColumn();
        col0.setStyle("width:280px;");
        col1.setStyle("width:280px;");
        col2.setStyle("width:280px;");
        col3.setStyle("width:280px;");
        col4.setStyle("width:280px;");
        col5.setStyle("width:280px;");
        
        dashboardModel.addColumn(col0);
        dashboardModel.addColumn(col1);
        dashboardModel.addColumn(col2);
        dashboardModel.addColumn(col3);
        dashboardModel.addColumn(col4);
        dashboardModel.addColumn(col5);        
        
        if (getUserSettings().getDashBoard().isEmpty()){
            dbsList.stream()
                    .sorted(Comparator.comparing(DashBoardSettings::getItemIndex, nullsFirst(naturalOrder())))
                    .forEach(dbSettings->{
                        String widget = dbSettings.getWidget(); 
                        DashboardColumn col = dashboardModel.getColumn(dbSettings.getColIndex());
                        col.addWidget(dbSettings.getWidget());
                    });
            getUserSettings().setDashBoard(dbsList);
        } else {
            getUserSettings().getDashBoard().stream()
                    .sorted(Comparator.comparing(DashBoardSettings::getItemIndex, nullsFirst(naturalOrder())))
                    .forEach(dbSettings->{ 
                        String widget = dbSettings.getWidget();                       
                        DashboardColumn col = dashboardModel.getColumn(dbSettings.getColIndex());
                        col.addWidget(widget);
                    });
        }
    }
    
    public void onChangeDashboard(){
        isChangeDashboard = true;        
    }
    
    public void onUpdateDashboard(){
        if (isChangeDashboard == false) return; 
        
        List<DashBoardSettings> checked = new ArrayList<>(Arrays.asList(dbsChecked));
        List<String> checkedWidget = checked.stream().map(dbs->dbs.getWidget()).collect(Collectors.toList());
        
        dashboardModel.getColumns().forEach(col -> {
            List<String> forRemove = new ArrayList<>();
            col.getWidgets().forEach((widget) -> {                
                if (checkedWidget.contains(widget)){
                    checkedWidget.remove(widget);                    
                } else {
                    forRemove.add(widget);
                }
            });
            col.getWidgets().removeAll(forRemove);
        });
        
        checkedWidget.forEach(widget->dashboardModel.getColumn(0).addWidget(widget));
        isChangeDashboard = false;
    }
    
    public void onAutoUpdate(){
        dashboardModel.getColumns().forEach(col -> col.getWidgets()
            .forEach(widget->{
                switch (widget){
                    case "messages":{
                        PrimeFaces.current().executeScript("document.getElementById('mainFRM:refreshMsg').click();");
                        break;
                    }
                    case "tasks":{
                        PrimeFaces.current().executeScript("document.getElementById('mainFRM:refreshTasks').click();");
                        break;
                    }
                    case "eventFeed":{
                        PrimeFaces.current().executeScript("document.getElementById('mainFRM:refreshEventFeed').click();");
                        break;
                    }
                }
            }));
    }
    
    /* *** *** * /
        
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
    
    /**
     * Формирование статуса объекта в зависимости от его выполнения
     * @param item
     * @return 
     */
    public String getItemStatus(WithDatesPlans item){
        if (item == null) return "";       
        if (item.getBeginDate() == null) return MsgUtils.getBandleLabel("NotStarted");
        if (item.isDraft()) return MsgUtils.getBandleLabel("Draft");
        if (item.isCompleted()){            
            return MsgUtils.getBandleLabel("Сompleted") + ": " + EscomBeanUtils.makeDuration(item.getBeginDate(), item.getFactExecDate());
        }
        if (item.isCanceled()) return MsgUtils.getBandleLabel("Cancelled");
        return EscomBeanUtils.makeDateDiffStatus(new Date(), item.getPlanExecDate());
    }
         
    public String getItemStyle(BaseDict item){
        if (item == null) return "";
        StringBuilder sb = new StringBuilder();
        
        //добавляем стиль для текущего состояния
        sb.append(item.getState().getCurrentState().getName()).append(" ");
        
        if (item.getBeginDate() != null && item.getPlanExecDate() != null){
            WithDatesPlans wpd = (WithDatesPlans) item;
            
            //добавляем стиль для просроченных
            if (wpd.getFactExecDate() == null && item.getPlanExecDate().before(new Date())){
                sb.append("expired").append(" ");
            } 
        }
        
        //добавляем стиль для процессо и подпроцессов
        if (item instanceof Process){
            if (item.getParent() == null){
                sb.append("process");
            } else {
                sb.append("subProcess");
            }
        }
        return sb.toString();
    }
    
    /* ПРОЧИЕ МЕТОДЫ */   
    
    public void onUploadFile(FileUploadEvent event) throws IOException{       
        attaches.clear();
        UploadedFile uploadFile = EscomFileUtils.handleUploadFile(event);        
        attaches.add(attacheBean.uploadAtache(uploadFile));
    } 
     
    /**
     * Создание процесса из прикреплённого файла(ов)
     */
    public void onCreateProc(){        
        ProcessType procType = processTypeBean.getLazyFacade().find(ProcessTypesDict.CONCORDED_ID);
        if (procType == null){
            MsgUtils.errorFormatMsg("ObjectWithIDNotFound", new Object[]{ProcessType.class.getSimpleName(), ProcessTypesDict.CONCORDED_ID});
            return;
        }
        Set<String> errors = new HashSet<>();
        Process process = processFacade.createProcFromFile(procType, getCurrentUser(), attaches, errors);
        if (!errors.isEmpty()){
            attacheService.deleteAttaches(attaches);
            MsgUtils.showErrorsMsg(errors);            
            return;
        }
        processBean.openItemCard(process, DictEditMode.INSERT_MODE, processBean.getParamsMap(), errors);        
    }
    
    /**
     * Создание документа из прикреплённого файла
     */
    public void onCreateDoc(){
        User author = getCurrentUser();
        Folder folder = author.getInbox();
        if (folder == null){
            MsgUtils.errorMsg("NoDefaultUserFolderSpecified");
            return;
        }       
        Attaches attache = attaches.get(0); 
        Map<String, Object> params = new HashMap<>();
        params.put("attache", attache);
        params.put("name", attache.getName());
        Doc doc = docFacade.createItem(author, null, folder, params);        
        docFacade.makeRightItem(doc, author);        
        Set<String> errors = new HashSet<>();
        docBean.openItemCard(doc, DictEditMode.INSERT_MODE, docBean.getParamsMap(), errors);
        if (!errors.isEmpty()){
            attacheService.deleteAttaches(attaches);
            MsgUtils.showErrorsMsg(errors);
        }
    }
    
    /* Добавление объекта в избранное  */
    public void addInFavorites(BaseDict item, Metadates metadates){
        Object[] params = new Object[]{item.getName()};
        if (favoriteService.addInFavorites(item, metadates, getCurrentUser())){
            MsgUtils.succesFormatMsg("ObjectAddedToFavorites", params);
            PrimeFaces.current().ajax().update("mainFRM:tblDetail");
        } else {
            MsgUtils.warnFormatMsg("ObjectAlreadyAddedFavorites", params);
        }
    }

    /* Отображение системной панели напоминания о сроках тех. поддержки и т.п. */
    public void showNotification(){
        if (!canShowNotifBar) return;
        Boolean needUpadateSystem = appBean.getNeedUpadateSystem();
        notifMessages.clear();
        if (needUpadateSystem) {
            String urlCaption = MsgUtils.getMessageLabel("GetInfoAboutNewversion");
            String url = "https://escom-archive.ru/news";
            String msg = MsgUtils.getMessageLabel("NeedUpdateSystem");
            notifMessages.add(new NotifMsg(msg, url, urlCaption));
        }

        if (checkExpiredLicense()){
            String dateExpire = getLicenseExpireAsString();
            String licensee = appBean.getLicensee();
            String remainedDays = DateUtils.differenceDays(Instant.now(), appBean.getLicenseExpireDate().toInstant());
            String msg = MessageFormat.format(MsgUtils.getMessageLabel("LicenseExpired"), new Object[]{licensee, dateExpire, remainedDays});
            String url = "https://escom-archive.ru/faqs/";
            String urlCaption = MsgUtils.getMessageLabel("UpdateInformation");
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
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();        
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        if (currentUser != null) {
            doSaveUserSettings();
            appBean.clearUserLock(currentUser);
            authLogFacade.addAuthExit(currentUser.getLogin(), request);            
        } 
        ectx.invalidateSession();
    }    
    
    /**
     * Обработка события закрытия диалога лицензионного соглашения
     * @param event
     */
    public void onAfterCloseLicenseDlg(SelectEvent event){
        String result = (String) event.getObject();
        if (SysParams.EXIT.equals(result)) onSessionExit();
    }

    /* Переход на начальную страницу программы */
    public String goToIndex(){
        return "/view/index?faces-redirect=true";
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
        if (currentUser != null && !currentUser.isLdap() && currentUser.isNeedChangePwl()){
            openSettingsForm();
        }
    }

    /**
     * Вывод сообщения о количестве непрочитанных сообщений
     */
    public void checkUnReadMessages(){
        Integer countUnreadMessage = getCountUnreadMessage();
        if (countUnreadMessage > 0){
            MsgUtils.succesFormatMsg("YouHaveUnreadMessage", new Object[]{countUnreadMessage});
        }
    }
    
    /* Колво непрочитанных сообщений у текущего пользователя */
    public Integer getCountUnreadMessage(){
        return messagesFacade.getCountUnReadMessage(currentUser);        
    }

    /* Скачивание вложения  */
    public void attacheDownLoad(Attaches attache){
        if (attache == null) return;
        String path = configuration.getUploadPath() + attache.getFullName();
        EscomFileUtils.attacheDownLoad(path, attache.getName());
    }
    
    /**
     * Формирование URL формы обозревателя 
     * @param formName 
     * @return  
     */
    public String onGetExplorerURL(String formName){        
        StringBuilder sb = makePageURL(formName);
        return sb.toString();
    }
    
    /** 
     * Формирование URL диалоговой формы
     * @param formName
     * @return 
     */
    public String onGetFormURL(String formName){
        StringBuilder sb = makePageURL(formName);
        sb.append("?beanId=").append("&beanName=");        
        return sb.toString();
    }
    
    public StringBuilder makePageURL(String page){
        StringBuilder sb = new StringBuilder(configuration.getServerAppURL());
        sb.append(page).append(".xhtml");
        return sb;
    }    
    
    /* Формирует ссылку URL для объекта  */
    public String doGetItemURL(BaseDict item, String itemPagePath){        
        return ItemUtils.getItemURL(item, itemPagePath, configuration.getServerAppURL()); 
    } 
    
    /**
     * Открытие формы диалога
     */
    public void onOpenForm(){
        if (StringUtils.isNotEmpty(openFormName)){
            openDialogFrm(openFormName, getParamsMap());
        }
    }      
    
    /**
     * Используется при открытии селекторов объектов
     * @param frmName
     * @param paramsMap 
     */
    public void openCloseableDialog(String frmName, Map<String, List<String>> paramsMap){
        Tuple formSize = getFormSize(frmName);
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", formSize.a);
        options.put("height", formSize.b);
        options.put("minWidth", 600);
        options.put("minHeight", 400);
        options.put("maximizable", true);
        options.put("minimizable", false);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        PrimeFaces.current().dialog().openDynamic(frmName, options, paramsMap);        
    }       
    
    public void openDialogFrm(String frmName, Map<String, List<String>> paramsMap){
        Tuple formSize = getFormSize(frmName);
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", formSize.a);
        options.put("height", formSize.b);
        options.put("minWidth", 600);
        options.put("minHeight", 400);
        options.put("maximizable", false);
        options.put("minimizable", false);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        PrimeFaces.current().dialog().openDynamic(frmName, options, paramsMap);        
    }       
        
    public void openModalDialogFrm(String frmName, Map<String, List<String>> paramsMap){
        Tuple formSize = getFormSize(frmName);
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", formSize.a);
        options.put("height", formSize.b);
        options.put("minWidth", 900);
        options.put("minHeight", 600);
        options.put("maximizable", false);
        options.put("minimizable", false);
        options.put("closable", false);
        options.put("closeOnEscape", false);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        PrimeFaces.current().dialog().openDynamic(frmName, options, paramsMap);        
    }         

    /* Открытие окна просмотра лицензии */
    public void openLicenseForm(){        
        if (userSettings.isAgreeLicense()){
            openDialogFrm(DictFrmName.FRM_AGREE_LICENSE, getParamsMap());
        } else {
            openModalDialogFrm(DictFrmName.FRM_AGREE_LICENSE, getParamsMap());
        }
    }
    
    /* Открытие формы нового почтового сообщения  */
    public void openMailMsgForm(String mode, List<BaseDict> docs){      
        List<String> openModeList = new ArrayList<>();
        openModeList.add(mode); 
        List<Integer> idList = docs.stream().map(BaseDict::getId).collect(Collectors.toList());
        String docIds = StringUtils.join(idList, ",");
        List<String> docsList = new ArrayList<>();
        docsList.add(docIds); 
        Map<String, List<String>> paramMap = getParamsMap();
        paramMap.put("modeSendAttache", openModeList);
        paramMap.put("docIds", docsList);
        openDialogFrm(DictFrmName.FRM_MAIL_MESSAGE, paramMap);
    }
    
    /* Открытие формы добавления версии */
    public void openAttacheAddForm(Doc doc){        
        List<String> docsList = new ArrayList<>();
        docsList.add(doc.getId().toString()); 
        Map<String, List<String>> paramMap = getParamsMap();
        paramMap.put("docId", docsList);
        openDialogFrm(DictFrmName.FRM_ADD_ATTACHE, paramMap); 
    }    

    /* Открытие формы настроек пользователя */
    public void openSettingsForm(){
        openDialogFrm(DictFrmName.FRM_USER_SETTINGS, getParamsMap());
    }

    /* Просмотр файла PDF в диалоговом окне */
    public void onViewReport(String reportName){
        String pdfFile = new StringBuilder()
                .append(configuration.getTempFolder())
                .append(reportName)
                .append("_")
                .append(getCurrentUser().getLogin())
                .append(".pdf").toString();
        Map<String, List<String>> paramMap = getParamsMap();
        List<String> pathList = new ArrayList<>();
        pathList.add(pdfFile);
        paramMap.put("path", pathList);
        openDialogFrm(DictFrmName.FRM_DOC_VIEWER, paramMap);
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
    
    /* Открытие формы списка сообщений пользователя */
    public void openUserMessagesForm(String typeMsg){        
        Map<String, List<String>> paramMap = getParamsMap();
        paramMap.put("typeMsg", Collections.singletonList(typeMsg));
        openDialogFrm(DictFrmName.FRM_USER_MESSAGES, paramMap);
    }    

    /**
     * Формирует набор параметров для передачи его в открываемый бин
     * @return 
     */
    public Map<String, List<String>> getParamsMap(){
        Map<String, List<String>> paramsMap = new HashMap<>();        
        paramsMap.put(SysParams.PARAM_BEAN_ID, Collections.singletonList(this.toString()));
        paramsMap.put(SysParams.PARAM_BEAN_NAME, Collections.singletonList(""));
        return paramsMap;
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
    
    public void onFormSize(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();        
        if (params.containsKey("width") && params.containsKey("height")){
            Integer width = Integer.valueOf(params.get("width"));
            Integer height = Integer.valueOf(params.get("height"));
            String formName = params.get("form");
            if (width != 0 && height != 0 ){
                saveFormSize(formName, width, height);
            }
        }
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
            List<FormsSettings> fsList = formsSettingsFacade.findByName(formName);
            if (!fsList.isEmpty()){
                FormsSettings fs = fsList.get(0);
                rezult = new Tuple(fs.getWidth(), fs.getHeight());
            } else
                if (formName.contains("explorer")){
                    rezult = new Tuple(1150, 700);                
                } else {
                    rezult = new Tuple(800, 500);
                }
        }
        formsSize.put(formName, rezult);
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
        List<FormsSettings> fsList = formsSettingsFacade.findByName(formName);
        if (fsList.isEmpty()){
            FormsSettings fs = new FormsSettings(formName, width, heaght);
            formsSettingsFacade.create(fs);
        }
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
    
    /* *** ПОЛЬЗОВАТЕЛЬ *** */
    
    /* Сохранение настроек текущего пользователя в базу данных */
    private void doSaveUserSettings(){
        if (userSettings == null) return;
        try {
            currentUser = getRefreshCurrentUser();
            userSettings.setTheme(primefacesTheme);
                        
            userSettings.getDashBoard().clear();
            AtomicInteger colIndex = new AtomicInteger(0);
            dashboardModel.getColumns()
                    .forEach(col->{                        
                        AtomicInteger itemIndex = new AtomicInteger(0);
                        List<String> widgets = col.getWidgets();
                        widgets.forEach(widget-> userSettings.getDashBoard()
                                        .add(new DashBoardSettings(widget, itemIndex.getAndIncrement(), colIndex.get())));
                        colIndex.getAndIncrement();
                    });
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
        if (currentUser == null) return false;
        if (DictRights.USER_ADMIN_ID.equals(currentUser.getId())){
            return true;
        }
        UserGroups groupAdmin = currentUser.getUsersGroupsList().stream().filter(userGroup -> userGroup.getId() == 1).findFirst().orElse(null);
        return groupAdmin != null;
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
        openDialogFrm(DictFrmName.FRM_CHECK_RELEASE, getParamsMap());
    }
    
    /* Отображение справки */
    public void onViewHelp(){
       openDialogFrm(DictFrmName.FRM_HELP, getParamsMap()); 
    }                   
    
    /* ЗАДАЧИ диаграмма исполнения */
    public void initTaskExePie(){
        taskDateStart = DateUtils.firstDayYear(LocalDate.now());
        taskDateEnd = DateUtils.lastDayYear(LocalDate.now());
        taskPeriod = "curYear";
    }

    public void onTaskPieExerefresh(){
        taskPieModel = null;
    }
    public PieChartModel getTaskPieModel() {
        if (taskPieModel == null){            
            Tuple<Integer, Map<String, Integer>> results = taskFacade.countTaskStaffByStates(getCurrentUserStaff(), taskDateStart, taskDateEnd);
            countTasks = results.a;
            Map<String, Integer> mapResult = results.b; 
            
            taskPieModel = new PieChartModel();            
            
            taskPieModel.set(MsgUtils.getBandleLabel("ExeInTime"), mapResult.get("ExeInTime"));           //row 0
            taskPieModel.set(MsgUtils.getBandleLabel("ExeOverdue"), mapResult.get("ExeOverdue"));         //row 1
            taskPieModel.set(MsgUtils.getBandleLabel("FinishInTime"), mapResult.get("FinishInTime"));     //row 2
            taskPieModel.set(MsgUtils.getBandleLabel("FinishOverdue"), mapResult.get("FinishOverdue"));   //row 3
            taskPieModel.set(MsgUtils.getBandleLabel("ExePlanToday"), mapResult.get("ExePlanToday"));     //row 4
            
            // синий, красный, зелёный, бордовый, желтый
            taskPieModel.setSeriesColors("0000FF, FF0000, 008000, 800000, FFFF00");
            taskPieModel.setShowDataLabels(true); 
            taskPieModel.setShadow(true);
            taskPieModel.setShowDatatip(true);
            
        }
        return taskPieModel;
    }

    /**
     * Обработка события щелчка мышью по диаграмме исполнения задач
     * @param event 
     */
    public void onPieTasksExeSelect(ItemSelectEvent event){         
        taskTypeId = event.getItemIndex();
        setOpenFormName(DictFrmName.FRM_MY_TASKS);     
    }
    
    public void onOpenTasks(){
       if (StringUtils.isNotEmpty(openFormName)){
            Map<String, List<String>> params = getParamsMap();
            params.put("tasksStatus", Collections.singletonList(taskTypeId.toString()));
            openDialogFrm(openFormName, params);
        } 
    }
    
    public void onPeriodChange(ValueChangeEvent event){
        taskPeriod = (String) event.getNewValue();  
        if (taskPeriod != null){
            taskDateStart = DateUtils.periodStartDate(taskPeriod, taskDateStart);
            taskDateEnd = DateUtils.periodEndDate(taskPeriod, taskDateEnd);
            taskPieModel = null;
        }
    }
    
    public Comparator<BaseDict> getBaseDictComporator(){
        Collator collator = Collator.getInstance(getLocale());
        
        return (BaseDict item1, BaseDict item2) -> {
            int rezult = collator.compare(item1.getCompanyName(), item2.getCompanyName());
            if (rezult == 0){                
                rezult = collator.compare(item1.getTypeName(), item2.getTypeName());
            }
            if (rezult == 0){                
                rezult = collator.compare(item1.getRegNumber(), item2.getRegNumber());
            }
            return rezult;
        };        
    }
    
    /* GETS & SETS */

    public Integer getCountTasks() {
        return countTasks;
    }
    
    public BigDecimal getTotalfilesSize() {
        return totalfilesSize;
    }
    
    public MeterGaugeChartModel getMeterFileSize() {
        if (meterFileSize == null){
            Long filesSize = attacheFacade.sumFilesSize();
            if (filesSize == null){
                filesSize = 0l;
            }
            totalfilesSize = new BigDecimal(filesSize);            
            if (totalfilesSize.intValue() > 0){
                totalfilesSize = totalfilesSize.divide(new BigDecimal(1048576000), 4, BigDecimal.ROUND_HALF_UP);
            }
            Integer filesQuote = configuration.getDiskQuote();        
            List<Number> intervals = new ArrayList<>();
            intervals.add(filesQuote * 30 / 100);
            intervals.add(filesQuote * 60 / 100);
            intervals.add(filesQuote * 80 / 100);
            intervals.add(filesQuote * 90 / 100);
            intervals.add(filesQuote);
            meterFileSize = new MeterGaugeChartModel(totalfilesSize, intervals);;
            meterFileSize.setTitle(MsgUtils.getBandleLabel("ArchiveSize"));
            meterFileSize.setGaugeLabel("Gb");
            meterFileSize.setGaugeLabelPosition("bottom");
            meterFileSize.setSeriesColors("66cc66,93b75f,E7E658,cc6666,C33730");
        }
        return meterFileSize;
    }

    public void setMeterFileSize(MeterGaugeChartModel meterFileSize) {
        this.meterFileSize = meterFileSize;
    }
        
    public void setOpenFormName(String openFormName) {
        this.openFormName = openFormName;
    }
                
    public String getLicenseLocalName(){
        return MsgUtils.getBandleLabel(appBean.getLicenseBundleName());
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

    /**
     * Формирует сообщение о количестве новых сообщений у текущего пользователя
     * @return
     */
    public String getMessagesInfo(){
        Integer countMsg = getCountUnreadMessage();
        if (countMsg > 0){
            return countMsg.toString();
        } else {
            return MsgUtils.getBandleLabel("No");
        }
    }

    /**
     * Формирует сообщение о количестве новых задач у текущего пользователя
     * @return
     */
    public String getTasksInfo(){
        Long count = taskFacade.getCountExecTasksByUser(getCurrentUser().getStaff());
        if (count > 0){
            return count.toString();
        } else {
            return MsgUtils.getBandleLabel("No");
        }
    }
    
    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public List<String> getThemes() {
        return themes;
    }

    public List <NotifMsg> getNotifMessages() {
        return notifMessages;
    }

    public List<DayType> getDayTypes() {
        return dayTypes;
    }   
        
    /**
     * Возвращает папку текущего пользователя
     * @return 
     */
    public Folder getUserFolder(){
        if (getCurrentUser() == null) return null;
        return getCurrentUser().getInbox();
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

    public ConcurrentHashMap<String, BaseView> getOpenedBeans() {
        return openedBeans;
    }
    
    /* Возвращает максимальный размер загружаемого файла */    
    public Integer getMaxFileSize(){
        return configuration.getMaxFileSize();
    }

    public List<SelectItem> getPERIODS() {
        return PERIODS;
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

    public List<DashBoardSettings> getDbsList() {
        return dbsList;
    }

    public DashBoardSettings[] getDbsChecked() {
        return dbsChecked;
    }
    public void setDbsChecked(DashBoardSettings[] dbsChecked) {
        this.dbsChecked = dbsChecked;
    }

    public Date getTaskDateStart() {
        return taskDateStart;
    }
    public void setTaskDateStart(Date taskDateStart) {
        this.taskDateStart = taskDateStart;
    }

    public Date getTaskDateEnd() {
        return taskDateEnd;
    }
    public void setTaskDateEnd(Date taskDateEnd) {
        this.taskDateEnd = taskDateEnd;
    }

    public String getTaskPeriod() {
        return taskPeriod;
    }
    public void setTaskPeriod(String taskPeriod) {
        this.taskPeriod = taskPeriod;     
    }
}