package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.beans.processes.templates.ProcTemplBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.process.conditions.ConditionFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.process.templates.ProcessTemplFacade;
import com.maxfill.model.process.types.ProcessTypesFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.templates.ProcTempl;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.process.timers.ProcTimerFacade;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.task.Task;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.users.User;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.Tuple;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.diagram.ConnectEvent;
import org.primefaces.event.diagram.ConnectionChangeEvent;
import org.primefaces.event.diagram.DisconnectEvent;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.endpoint.RectangleEndPoint;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.overlay.Overlay;

/**
 * Контролер формы "Карточка процесса"
 */
@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process> {
    private static final long serialVersionUID = -5558740260204665618L;    
    
    @Inject
    private TaskBean taskBean;
    @Inject
    private ProcTemplBean procTemplBean;
    @Inject
    private AttacheBean attacheBean;
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private ProcessTemplFacade processTemplFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private ConditionFacade conditionFacade;
    @EJB
    private StatusesDocFacade statusesDocFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private ProcTimerFacade procTimerFacade;
        
    private Element selectedElement = null;

    private int defX = 8;
    private int defY = 8;
    private WFConnectedElem baseElement;
    private WFConnectedElem copiedElement;

    private final Set<Task> editedTasks = new HashSet<>();
    private Task currentTask;
    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;
    private final DefaultDiagramModel model = new DefaultDiagramModel();
    
    private String nameTemplate;
    private ProcTempl selectedTempl;
    private List<ProcTempl> templates;
    
    private final List<Attaches> attaches = new ArrayList<>();
    private Doc selectedDoc;
    
    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }

    @Override
    protected void doPrepareOpen(Process item) {
        model.setMaxConnections(-1);
        model.getDefaultConnectionOverlays().add(new ArrowOverlay(20, 20, 1, 1));
        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:2}");
        connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
        connector.setCornerRadius(10);
        model.setDefaultConnector(connector);
        loadModel(getScheme());
    }

    @Override
    public void onAfterFormLoad() {
        if (getEditedItem() == null) return;
        if (getEditedItem().getScheme() == null){
            Scheme scheme = new Scheme(getEditedItem());
            getEditedItem().setScheme(scheme);            
            if (getEditedItem().getOwner() != null && !getTemplates().isEmpty()){
                PrimeFaces.current().executeScript("PF('LoadFromTemplDLG').show();");
            }
        } 
    }

    /**
     * Перед сохранением процесса
     * @param item
     */
    @Override
    protected void onBeforeSaveItem(Process item){       
        //сохраняем только оставшиеся на схеме задачи, а старые удаляем        
        List<Task> liveTasks = getTasksFromModel();        
        List<Task> forRemoveTasks = new ArrayList<>(getScheme().getTasks());        
        forRemoveTasks.removeAll(liveTasks); //в списке остались только те элементы, которые нужно удалить
        if (!forRemoveTasks.isEmpty()){
            getScheme().getTasks().removeAll(forRemoveTasks);
            editedTasks.removeAll(forRemoveTasks);
        }
        //сохраняем только оставшиеся на схеме таймеры, а старые удаляем
        List<ProcTimer> liveTimers = getProcTimersFromModel();
        List<ProcTimer> forRemoveTimers = new ArrayList<>(getScheme().getTimers());
        forRemoveTimers.removeAll(liveTimers);
        if (!forRemoveTimers.isEmpty()){
            getScheme().getTimers().removeAll(forRemoveTimers);
        }
        workflow.packScheme(getScheme());
        super.onBeforeSaveItem(item);
    }

    @Override
    protected void checkItemBeforeSave(Process process, Set<String> errors){                
        super.checkItemBeforeSave(process, errors);
    }    
    
    @Override
    public boolean doSaveItem(){
        Boolean result = super.doSaveItem();
        modelRefresh();
        return result;
    }   
    
    /**
     * Переопределение метода закрытия формы. Передаём параметр закрытия, 
     * который устанавливается в зависимости от того, был ли запущен/остановлен процесс
     * @return 
     */
    @Override
    public String doFinalCancelSave() {  
        return closeItemForm(exitParam);  //закрыть форму объекта
    }
    
    @Override
    protected String makeHeader(StringBuilder sb){
        if (getEditedItem() != null && StringUtils.isNotEmpty(getEditedItem().getRegNumber())){
            sb.append(" ").append(MsgUtils.getBandleLabel("NumberShort")).append(getEditedItem().getRegNumber()).append(" ");
        }
        return super.makeHeader(sb);
    }
     
    /* МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ */
    
    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        Set<String> errors = new HashSet<>();
        validatePlanDate(getEditedItem().getPlanExecDate(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){            
            workflow.start(getEditedItem(), getCurrentUser(), errors);
            if (!errors.isEmpty()){
                MsgUtils.showErrorsMsg(errors);
            } else {
                getEditedItem().getState().setCurrentState(stateFacade.getRunningState());
                //processFacade.addLogEvent(getEditedItem(), DictLogEvents., getCurrentUser());
                setItemCurrentState(getEditedItem().getState().getCurrentState());
                MsgUtils.succesMsg("ProcessSuccessfullyLaunched");
            }
            loadModel(getScheme());
            exitParam = SysParams.EXIT_EXECUTE;
            PrimeFaces.current().ajax().update("mainFRM");
        }
    }
    
    /**
     * Обработка события прерывания процесса
     */
    public void onStop(){
        Set<String> errors = new HashSet<>();
        workflow.stop(getEditedItem(), getCurrentUser(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
        } else {
            loadModel(getScheme());
            modelRefresh();
            exitParam = SysParams.EXIT_EXECUTE;
            MsgUtils.warnMsg("ProcessExecutionInterrupted");
        }
    }
    
    /* ШАБЛОНЫ ПРОЦЕССА */    

    /**
     * Загрузка визуальной схемы процесса из шаблона
     */
    public void onLoadModelFromTempl(){
        Integer termHours = selectedTempl.getTermHours();
        if (termHours != null){
            Date planExecDate = DateUtils.addHour(new Date(), termHours);
            getEditedItem().setPlanExecDate(planExecDate);
        }
        
        Scheme scheme = new Scheme(getEditedItem());        
        scheme.setPackElements(selectedTempl.getElements());               
        getEditedItem().setScheme(scheme);        
        loadModel(scheme);
        onItemChange(); 
        setTabActiveIndex(1);
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:diagramm");
        addElementContextMenu();
    }      
    
    /**
     * Сохранение модели процесса в шаблон
     */
    public void onSaveModelAsTempl(){
        Set<String> errors = new HashSet<>();
        Scheme scheme = getScheme();
        workflow.validateScheme(scheme, false, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        workflow.packScheme(scheme);

        ProcessType processType = processTypesFacade.find(getEditedItem().getOwner().getId());
        List<ProcTempl> templs = processType.getTemplates();        
        
        if (selectedTempl == null) {        //то создаём новый шаблон       
            Map<String, Object> params = new HashMap<>();
            params.put("name", nameTemplate);        
            selectedTempl = processTemplFacade.createItem(getCurrentUser(), null, processType, params);                        
            Tuple result = processTemplFacade.findDublicateExcludeItem(selectedTempl);
            if ((Boolean)result.a){
                MsgUtils.errorFormatMsg("ObjectIsExsist", new Object[]{nameTemplate, result.b});
                return;
            }
            processTemplFacade.addLogEvent(selectedTempl, "ObjectCreate", getCurrentUser());
            selectedTempl.setElements(scheme.getPackElements());            
            if (templs.isEmpty()){
                selectedTempl.setIsDefault(Boolean.TRUE);
            }
            templs.add(selectedTempl);
            processTypesFacade.edit(processType);
        } else {    //перезаписываем схему в существующий шаблон            
            selectedTempl.setElements(scheme.getPackElements());
            processTemplFacade.addLogEvent(selectedTempl, "ObjectModified", getCurrentUser());
            processTemplFacade.edit(selectedTempl);            
        }
        templates = null; 
        getEditedItem().setOwner(processType);
        PrimeFaces.current().executeScript("PF('SaveAsTemplDLG').hide();");
        MsgUtils.succesMsg("TemplateSaved");
    }
    
    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */
    
    /**
     * Загрузка визуальной схемы процесса
     * @param scheme
     */
    public void loadModel(Scheme scheme){       
        if (scheme == null) return;
        workflow.unpackScheme(scheme);
        model.clear();
        restoreModel();
    }

    /**
     * Перерисовка модели на странице формы
     */
    public void modelRefresh(){        
        model.clear();
        restoreModel();
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:diagramm");
        if (!isReadOnly()){
            addElementContextMenu();
        }
    }
    
    /**
     * Формирует графическую схему из данных модели процесса
     */
    private void restoreModel(){                
        Map<String, Element> elementMap = new HashMap <>();        
        getScheme().getElements().getTasks().forEach((k, taskEl)->{             
            if (taskEl.getTask() == null){
                Staff staff = null;
                if (taskEl.getStaffId() != null){
                    staff = staffFacade.find(taskEl.getStaffId());
                }
                Task task = taskFacade.createTask(MsgUtils.getBandleLabel("Task"), staff, getCurrentUser(), getEditedItem().getPlanExecDate(), getScheme(), taskEl.getUid());
                taskEl.setTask(task);
                getScheme().getTasks().add(task);
            }
            elementMap.put(k, createElement(taskEl));
        });
        getScheme().getElements().getTimers().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getMessages().forEach((k, v)->elementMap.put(k, createElement(v))); 
        getScheme().getElements().getProcedures().forEach((k, v)->elementMap.put(k, createElement(v))); 
        getScheme().getElements().getExits().forEach((k, v)->elementMap.put(k, createElement(v)));        
        getScheme().getElements().getLogics().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getEnters().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getStates().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getConditions().forEach((k, v)->elementMap.put(k, createElement(v)));
        StartElem startElem = getScheme().getElements().getStartElem();
        if (startElem != null){
            elementMap.put(startElem.getUid(), createElement(startElem));
        }
        List<Connection> connections = new ArrayList <>();
        getScheme().getElements().getConnectors().stream().forEach(connectorElem->{
            AnchorElem anchorFrom = connectorElem.getFrom();            
            Element fromEl = elementMap.get(anchorFrom.getOwnerUID());
            EndPoint endPointFrom = getEndPointById(fromEl, anchorFrom.getUid());
            AnchorElem anchorTo = connectorElem.getTo();            
            Element toEl = elementMap.get(anchorTo.getOwnerUID());
            EndPoint endPointTo = getEndPointById(toEl, anchorTo.getUid());
            Connection connection = createConnection(endPointFrom, endPointTo, connectorElem);
            connections.add(connection);
        });
        elementMap.forEach((s, element) -> model.addElement(element));
        connections.stream().forEach(c -> model.connect(c));        
    }

    /**
     * Очистка визуальной схемы процесса
     */
    public void onClearModel(){        
        Scheme scheme = new Scheme(getEditedItem());
        getEditedItem().setScheme(scheme);
        model.clear();
        modelRefresh();
    }

    /**
     * Обоработка события удаления текущего визуального компонента со схемы
     */
    public void onElementDelete(){
        Set<String> errors = new HashSet <>();
        workflow.removeElement(baseElement, getScheme(), errors);
        if (errors.isEmpty()) {
            model.removeElement(selectedElement);
            modelRefresh();
            onItemChange();
        } else {
            MsgUtils.showErrors(errors);
        }
    }

    /**
     * Обработка события выделения мышью визуального компонента на схеме процесса
     * используется для получения и сохранения координат элементов
     */
    public void onElementClicked() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String paramId = params.get("elementId");
        if (StringUtils.isNotBlank(paramId)) {
            String x = params.get("posX");
            String y = params.get("posY");
            String id = paramId.substring(paramId.indexOf("-") + 1);
            selectedElement = model.findElement(id);
            if (selectedElement != null){
                selectedElement.setX(x + "em");
                selectedElement.setY(y + "em");
                defX = Integer.valueOf(x);
                defY = Integer.valueOf(y);
                baseElement = (WFConnectedElem) selectedElement.getData();
                baseElement.setPosX(Integer.valueOf(x));
                baseElement.setPosY(Integer.valueOf(y));
            }
        }
    }   
    
    /**
     * Обоработка события контекстного меню для открытия карточки свойств визуального компонента 
     */
    public void onElementOpenClick(){
        onElementClicked();        
        PrimeFaces.current().executeScript("document.getElementById('mainFRM:btnOpenElement').click();");
    }

    /**
     * Обоработка события открытия карточки свойств визуального компонента 
     */
    public void onElementOpen(){
        if (baseElement instanceof TaskElem){
            TaskElem taskElem = (TaskElem) baseElement;           
            currentTask = (Task) taskElem.getTask();           
            onOpenTask();
            return;
        } 
        if (baseElement instanceof ConditionElem){
            openElementCard(DictFrmName.FRM_CONDITION);
            return;
        } 
        if (baseElement instanceof StatusElem){
            openElementCard(DictFrmName.FRM_DOC_STATUS);
            return;
        }
        if (baseElement instanceof ExitElem){
            openElementCard(DictFrmName.FRM_EXIT);
            return;
        }
        if (baseElement instanceof TimerElem){
            openElementCard(DictFrmName.FRM_TIMER);
            return;
        }
        if (baseElement instanceof MessageElem){
            openElementCard(DictFrmName.FRM_MESSAGE);
            return;
        }
        if (baseElement instanceof ProcedureElem){
            openElementCard(DictFrmName.FRM_PROCEDURE); 
            return;
        }
        modelRefresh();
    }
    
    /**
     * Служебный метод открытия карточки формы элемента схемы маршрута
     * @param formName 
     */
    private void openElementCard(String formName){
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> itemIds = Collections.singletonList(beanId);
        List<String> beanNameList = Collections.singletonList(getBeanName());
        paramMap.put(SysParams.PARAM_BEAN_ID, itemIds);
        paramMap.put(SysParams.PARAM_BEAN_NAME, beanNameList);
        sessionBean.openDialogFrm(formName, getParamsMap());
    }
    
    /**
     * Обработка события копирования эемента
     */
    public void onElementCopy(){ 
        if (baseElement instanceof TaskElem){
            doCopyElement(new TaskElem());
        } else 
            if (baseElement instanceof ConditionElem){
                doCopyElement(new ConditionElem());
            } else 
                if (baseElement instanceof StatusElem){           
                    doCopyElement(new StatusElem());
                } else {
                    MsgUtils.warnMsg("CopyingObjectsTypeNotProvided");
                }                                 
    }
    
    private void doCopyElement(WFConnectedElem elem){
        try { 
            copiedElement = elem;
            BeanUtils.copyProperties(copiedElement, baseElement);
            PrimeFaces.current().executeScript("refreshContextMenu('mainFRM:mainTabView:diagramm');");
            MsgUtils.succesFormatMsg("ObjectIsCopied", new Object[]{copiedElement.getCaption()}); 
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);        
        }  
    }
    
    /**
     * Обработка события вставки скопированного элемента
     */
    public void onElementPaste(){
        try {
            if (baseElement instanceof TaskElem){           
                TaskElem newTaskElem = createTask(null, "", defX, defY, new HashSet<>());                
                Task newTask = newTaskElem.getTask();
                TaskElem sourceTaskElem = (TaskElem) baseElement;
                Task sourceTask = sourceTaskElem.getTask();
                BeanUtils.copyProperties(newTask, sourceTask); 
                newTask.setTaskLinkUID(newTaskElem.getUid()); 
                StringBuilder sb = new StringBuilder();
                sb.append(MsgUtils.getBandleLabel("Copy")).append(" ").append(sourceTask.getName());                
                newTask.setName(sb.toString());
                finalAddElement();
            } else 
                if (baseElement instanceof ConditionElem){
                    ConditionElem sourceElem = (ConditionElem) baseElement;
                    Condition condition = conditionFacade.find(sourceElem.getConditonId());
                    createCondition(condition, defX, defY, new HashSet<>());
                    finalAddElement();
                } else  
                    if (baseElement instanceof StatusElem){
                        StatusElem sourceElem = (StatusElem) baseElement;                        
                        StatusesDoc docStatus = statusesDocFacade.find(sourceElem.getDocStatusId());
                        createState(docStatus, sourceElem.getStyleType(), defX, defY, new HashSet<>());
                        finalAddElement();
                    }
        } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Открытие карточки задачи
     */  
    public void onOpenTask(){ 
        taskBean.prepEditChildItem(currentTask, getParamsMap());
    }
    
    /**
     * Обработка события закрытия карточки свойств визуального компонента
     * @param event
     */
    public void onElementClose(SelectEvent event){
        if (event.getObject() == null) return;
        if (baseElement instanceof TaskElem){
            TaskElem taskElem = (TaskElem)baseElement;
            currentTask = taskElem.getTask();
            onAfterTaskClose(event);
            return;
        }
        if (baseElement instanceof ExitElem){
            Element exit = model.findElement(baseElement.getUid());
            exit.setStyleClass(baseElement.getStyle());
            return;
        }
        if (baseElement instanceof StatusElem){
            Element status = model.findElement(baseElement.getUid());
            status.setStyleClass(baseElement.getStyle());
        }
        onItemChange();
        modelRefresh();
    }
    
    /**
     * Обработка события закрытия карточки задачи
     * @param event
     */
    public void onAfterTaskClose(SelectEvent event){
        if (event.getObject() == null) return;        
        String result = (String) event.getObject();
        switch (result){
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{
                editedTasks.add(currentTask);           
                onItemChange();
                Element task = model.findElement(baseElement.getUid());
                task.setStyleClass(baseElement.getStyle());
                PrimeFaces.current().ajax().update("mainFRM:mainTabView:concorderList");      
                modelRefresh();
                break;
            }
            case SysParams.EXIT_EXECUTE:{
                setItemCurrentState(getEditedItem().getState().getCurrentState());
                loadModel(getScheme());
                PrimeFaces.current().ajax().update("process");
                break;
            }
        }
    }
        
    /* ДОБАВЛЕНИЕ ЭЛЕМЕНТОВ НА СХЕМУ ПРОЦЕССА С ПАНЕЛИ КОМПОНЕНТ */
    
    /**
     * Обработка события добавления в компонентов "Задача" из выбранных в селекторе штатных единиц
     * @param event
     */
    public void onStaffsSelected(SelectEvent event){
        List<Staff> executors = (List<Staff>) event.getObject();
        if (executors.isEmpty()) return;
        Set<String> errors = new HashSet<>();
        for (Staff executor : executors) {
            Set<String> metodErr = new HashSet<>();
            baseElement = createTask(executor, MsgUtils.getMessageLabel("AgreeDocument"), defX, defY, metodErr); 
            defX = defX + 5;
            defY = defY + 5;
            if (!metodErr.isEmpty()){
                errors.addAll(metodErr);
            }
        }
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
        } else { 
            onItemChange();
            modelRefresh();
            //finalAddElement();
        }
    }   
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Логическоe ветвление" 
     * @param bundleName
     */
    public void onAddLogicElement(String bundleName){              
        baseElement = createLogic(bundleName, defX, defY, new HashSet <>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Состояния" 
     * @param name
     */
    public void onAddStateElement(String name){
        baseElement = createState(null, "success", defX, defY, new HashSet<>());        
        finalAddElement();        
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Условие" 
     */
    public void onAddConditionElement(){
        baseElement = createCondition(null, defX, defY, new HashSet<>());
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Таймер"
     */
    public void onAddTimerElement() {
        baseElement = createTimer(null, defX, defY, new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Процедура"
     */
    public void onAddProcElement() {
        baseElement = createProcedure(null, defX, defY, new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Сообщение"
     */
    public void onAddMessageElement() {
        baseElement = createMessage(null, defX, defY, new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddEnterElement(){       
        baseElement = createEnter(defX, defY, new HashSet<>());
        finalAddElement();
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddStartElement(){       
        if (getScheme().getElements().getStartElem() == null){
            baseElement = createStart(3, 3, new HashSet<>());        
            finalAddElement();            
        } else {
            MsgUtils.errorMsg("SchemeCanBeOnlyOneStartElement");
        }
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Выход" 
     */
    public void onAddExitElement(){               
        baseElement = createExit(Boolean.TRUE, defX, defY, new HashSet<>());        
        finalAddElement();
    }
    
    /**
     * Завершает добавление элемента к визуальной схеме 
     * @param elementId 
     */
    private void finalAddElement(){
        defX = defX + 5;
        defY = defY + 5;
        onItemChange();
        onElementOpen();
    }
    
    /* СОЗДАНИЕ КОМПОНЕНТОВ СХЕМЫ ПРОЦЕССА */

    /**
     * Создание элемента "Условие" в модель процесса
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private ConditionElem createCondition(Condition condition, int x, int y, Set<String> errors){
        ConditionElem conditionElem;
        if (condition != null){            
            conditionElem = new ConditionElem(condition.getName(), condition.getId(), x, y);
        } else {
            conditionElem = new ConditionElem("???", null, x, y);
        }
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT, DictWorkflowElem.STYLE_YES);
        createSourceEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_NO);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP); 
        conditionElem.setAnchors(makeAnchorElems(conditionElem, endPoints));
        workflow.addCondition(conditionElem, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(conditionElem);
            return conditionElem;
        }
        return null;
    }

    private TimerElem createTimer(String name, int x, int y, Set<String> errors){        
        TimerElem timer = new TimerElem(null, x, y);
        ProcTimer procTimer = procTimerFacade.createTimer(getEditedItem(), getScheme(), timer.getUid());
        timer.setProcTimer(procTimer);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        timer.setAnchors(makeAnchorElems(timer, endPoints));
        workflow.addTimer(timer, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(timer);
            return timer;
        }
        return null;
    }
    
    private MessageElem createMessage(String name, int x, int y, Set<String> errors){        
        MessageElem element = new MessageElem(null, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        element.setAnchors(makeAnchorElems(element, endPoints));
        workflow.addMessage(element, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(element);
            return element;
        }
        return null;
    }
     
    private ProcedureElem createProcedure(String name, int x, int y, Set<String> errors){        
        ProcedureElem element = new ProcedureElem(null, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        element.setAnchors(makeAnchorElems(element, endPoints));
        workflow.addProcedure(element, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(element);
            return element;
        }
        return null;
    }        
    
    /**
     * Создание элемента "Логическое ветвление"
     * @param x
     * @param y
     * @param errors
     */
    private LogicElem createLogic(String name, int x, int y, Set<String> errors){
        LogicElem logic = new LogicElem(name, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT); 
        logic.setAnchors(makeAnchorElems(logic, endPoints));
        workflow.addLogic(logic, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(logic);
            return logic;
        }
        return null;
    }

    /**
     * Создание элемента "Вход в процесс"
     * @param x
     * @param y
     * @param errors
     */
    private EnterElem createEnter(int x, int y, Set<String> errors){
        EnterElem enter = new EnterElem("", x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        enter.setAnchors(makeAnchorElems(enter, endPoints));
        workflow.addEnter(enter, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(enter);
            return enter;
        }
        return null;
    }

    /**
     * Создание элемента "Старт процесса"
     * @param x
     * @param y
     * @param errors
     */
    private StartElem createStart(int x, int y, Set<String> errors){
        StartElem start = new StartElem("", x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        start.setAnchors(makeAnchorElems(start, endPoints));
        workflow.addStart(start, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(start);
            return start;
        }
        return null;
    }
    
    /**
     * Создание элемента "Вход в процесс"
     * @param x
     * @param y
     * @param errors
     */
    private ExitElem createExit(Boolean finalize, int x, int y, Set<String> errors){
        ExitElem exit = new ExitElem("", finalize, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_MAIN);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP, DictWorkflowElem.STYLE_MAIN);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM, DictWorkflowElem.STYLE_MAIN);
        exit.setAnchors(makeAnchorElems(exit, endPoints));
        workflow.addExit(exit, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(exit);
            return exit;
        }
        return null;
    }

    /**
     * Создание элемента "Поручение"
     * @param executor
     * @param x
     * @param y
     */
    private TaskElem createTask(Staff executor, String taskName, int x, int y, Set<String> errors){
        TaskElem taskElem = new TaskElem(taskName, x, y);
        Task task = taskFacade.createTask(taskName, executor, getCurrentUser(), getEditedItem().getPlanExecDate(), getScheme(), taskElem.getUid());
        taskElem.setTask(task);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        taskElem.setAnchors(makeAnchorElems(taskElem, endPoints));
        workflow.addTask(taskElem, getScheme(), errors);
        if (errors.isEmpty()){
            modelAddElement(taskElem);
            return taskElem;
        } 
        return null;
    }

    /**
     * Создание элемента "Состояние"
     * @param styleType
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private StatusElem createState(StatusesDoc docStatus, String styleType, int x, int y, Set<String> errors){        
        StatusElem stateEl;
        if (docStatus != null){
            stateEl = new StatusElem(docStatus.getBundleName(), docStatus.getId(), x, y);
        } else {
            stateEl = new StatusElem("???", null, x, y);
        }
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        stateEl.setAnchors(makeAnchorElems(stateEl, endPoints));
        stateEl.setStyleType(styleType);
        workflow.addState(stateEl, getScheme(), errors);
        if (errors.isEmpty()) {
            modelAddElement(stateEl);
            return stateEl;
        }
        return null;
    }

    /**
     * Добавление workflow элементов в модель процесса
     * @param wfElement
     * @return 
     */    
    private Element modelAddElement(WFConnectedElem wfElement){
        Element element = createElement(wfElement);
        model.addElement(element);
        return element;
    }

    private Element createElement(WFConnectedElem wfElement){
        Element element = new Element(wfElement, wfElement.getPosX() + "em", wfElement.getPosY() + "em");        
        List<EndPoint> endPoints = restoreEndPoints(wfElement.getAnchors());
        endPoints.forEach(endPoint -> element.addEndPoint(endPoint));
        element.setId(wfElement.getUid());
        element.setStyleClass(wfElement.getStyle());
        return element;
    }

    /* *** СОЗДАНИЕ КОННЕКТОРОВ и ЯКОРЕЙ *** */

    /**
     * Создание точки приёмника для элемента
     * @param anchor
     * @return 
     */
    private EndPoint createTargetEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor) {
        return createTargetEndPoint(endPoints, anchor, DictWorkflowElem.STYLE_MAIN);
    }
    private EndPoint createTargetEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor, String style) {
        EndPoint endPoint;
        if (isReadOnly()){
            endPoint = new BlankEndPoint(anchor);
        } else {
            endPoint = new DotEndPoint(anchor);
        }
        endPoint.setScope("network");
        endPoint.setTarget(true);
        endPoint.setStyle(style);
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        endPoints.add(endPoint);
        return endPoint;
    }

    /**
     * Создание точки источника для элемента
     * @param anchor
     * @return 
     */
    private EndPoint createSourceEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor) {
        return createSourceEndPoint(endPoints, anchor, DictWorkflowElem.STYLE_MAIN);
    }
    private EndPoint createSourceEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor, String style) {
        EndPoint endPoint;
        if (isReadOnly()){
            endPoint = new BlankEndPoint(anchor);
        } else {
            endPoint = new RectangleEndPoint(anchor);
        }
        endPoint.setScope("network");
        endPoint.setSource(true);
        endPoint.setStyle(style);
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        endPoints.add(endPoint); 
        return endPoint;
    }

    /**
     * Создание визуального соединения компонентов схемы
     * @param from
     * @param to
     * @param label
     * @return
     */
    private Connection createConnection(EndPoint from, EndPoint to, ConnectorElem connectorElem) {
        if (from == null || to == null) return null;
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));
        String label = "";
        String keyLabel = connectorElem.getCaption();
        if(StringUtils.isNotBlank(keyLabel)) {                         
            label = MsgUtils.getBandleLabel(keyLabel);
        }        
        conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));       
        if (connectorElem.isDone()){
            FlowChartConnector connector = new FlowChartConnector();
            connector.setPaintStyle("{strokeStyle:'#020202', lineWidth:3}");
            connector.setCornerRadius(10); 
            conn.setConnector(connector);
        }
        return conn;
    }

    /**
     * Формирует список точек для компонента из списка якорей workflow элемента
     * @param anchorElems
     * @return
     */
    private List<EndPoint> restoreEndPoints(Set<AnchorElem> anchorElems){
        List<EndPoint> endPoints = new ArrayList <>();
        for (AnchorElem anchorElem : anchorElems){
            EndPoint endPoint;
            String position = anchorElem.getPosition().toUpperCase();
            EndPointAnchor anchor = EndPointAnchor.valueOf(position);
            if (anchorElem.isSource()) {
                endPoint = createSourceEndPoint(endPoints, anchor, anchorElem.getStyle());
            } else {
                endPoint = createTargetEndPoint(endPoints, anchor, anchorElem.getStyle());
            }
            endPoint.setId(anchorElem.getUid());
        }
        return endPoints;
    }

    /**
     * Формирует список якорей для workflow элементов из списка точек визуальных компонент
     * @param endPoints
     * @return
     */
    private Set<AnchorElem> makeAnchorElems(WFConnectedElem element, List<EndPoint> endPoints){
        Set<AnchorElem> anchorElems =  new HashSet <>();
        for(EndPoint endPoint : endPoints){
            String position = endPoint.getAnchor().toString();
            AnchorElem anchorElem = new AnchorElem("", position, endPoint.isSource(), element.getUid());
            anchorElem.setStyle(endPoint.getStyle());
            anchorElems.add(anchorElem);
        }
        return anchorElems;
    }

    /**
     * Обработка события соединения объектов визуальной модели
     * @param event 
     */
    public void onConnect(ConnectEvent event){
        WFConnectedElem wfSource = (WFConnectedElem) event.getSourceElement().getData();
        WFConnectedElem wfTarget = (WFConnectedElem) event.getTargetElement().getData();
        EndPoint sourcePoint = event.getSourceEndPoint();
        EndPoint targetPoint = event.getTargetEndPoint();
        AnchorElem sourceAnchor = wfSource.getAnchorsById(sourcePoint.getId());
        AnchorElem targetAnchor = wfTarget.getAnchorsById(targetPoint.getId());
        Set<String> errors = new HashSet <>();
        String label = "";
        switch(sourcePoint.getStyle()){
            case DictWorkflowElem.STYLE_NO:{
                label = "No";
                break;
            }
            case DictWorkflowElem.STYLE_YES:{
                label = "Yes";
                break;
            }
        }
        
        if (workflow.createConnector(sourceAnchor, targetAnchor, getScheme(), label, errors) != null){  //если коннектор создался
            onItemChange();
            if (StringUtils.isNotBlank(label)){
                Connection connection = findConnection(sourcePoint, targetPoint);
                connection.getOverlays().clear();
                Overlay overlay = new LabelOverlay(MsgUtils.getBandleLabel(label), "flow-label", 0.5);
                connection.getOverlays().add(overlay);
                modelRefresh();
            }
        }

        if (!errors.isEmpty()){
            Connection connection = findConnection(sourcePoint, targetPoint);
            model.disconnect(connection);
            modelRefresh();
            MsgUtils.showErrors(errors);
        }
    }

    /**
     * Обработка события разрыва соединения на схеме процесса
     * @param event
     */
    public void onDisconnect(DisconnectEvent event) {
        WFConnectedElem wfSource = (WFConnectedElem) event.getSourceElement().getData();
        WFConnectedElem wfTarget = (WFConnectedElem) event.getTargetElement().getData();
        EndPoint sourcePoint = event.getSourceEndPoint();
        EndPoint targetPoint = event.getTargetEndPoint();
        Set<String> errors = new HashSet <>();
        AnchorElem sourceAnchor = wfSource.getAnchorsById(sourcePoint.getId());
        AnchorElem targetAnchor = wfTarget.getAnchorsById(targetPoint.getId());
        workflow.removeConnector(sourceAnchor, targetAnchor, getScheme(), errors);
        onItemChange();
    }

    public void onConnectionChange(ConnectionChangeEvent event) {
        //OriginalSource = event.getOriginalSourceElement().getData();
        //NewSource = event.getNewSourceElement().getData();
        //OriginalTarget = event.getOriginalTargetElement().getData();
        //NewTarget = event.getNewTargetElement().getData();
        onItemChange();
    }

    /**
     * Добавление контекстного меню к элементам схемы процесса
     */
    private void addElementContextMenu(){
        PrimeFaces.current().executeScript("addContextMenu('mainFRM:mainTabView:diagramm')");
        StringBuilder sb = new StringBuilder("addElementMenu([");
        model.getElements().stream()
                .filter(element-> !element.getStyleClass().equals(DictWorkflowElem.STYLE_START))
                .forEach(element-> {            
            sb.append("'mainFRM:mainTabView:diagramm-").append(element.getId()).append("', "); 
        });
        sb.append("])");      
        PrimeFaces.current().executeScript(sb.toString());
    }
    
    @Override
    public void onTabChange(TabChangeEvent event){
        if (event.getTab().getId().equals("tbScheme")){
            addElementContextMenu();
        }
    }
    
    /* ПРОЧИЕ МЕТОДЫ */
    
    /**
     * Проверка срока исполнения
     * @param planDate 
     * @param errors 
     */
    public void validatePlanDate(Date planDate, Set<String> errors){
        Date today = new Date();
        if (today.after(planDate)){
            String errMsg = MsgUtils.getMessageLabel("DeadlineSpecifiedInPastTime");
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:dtPlanExecDate");
            input.setValid(false);
            errors.add(errMsg);            
            context.validationFailed();
        }
    }
    
    /**
     * Возвращает якорь компонента по ID якоря
     * @param element
     * @param endPointId
     * @return
     */
    public EndPoint getEndPointById(Element element, String endPointId){
        for (EndPoint endPoint : element.getEndPoints()){
            if (endPoint.getId().equals(endPointId.toLowerCase())){
                return endPoint;
            }
        }
        return null;
    }

    /**
     * Возвращает Connection по двум точкам
     * @param source
     * @param target
     * @return
     */
    private Connection findConnection(EndPoint source, EndPoint target){
        for(Connection connection : model.getConnections()){
            if (connection.getSource().equals(source) && connection.getTarget().equals(target)){
                return connection;
            }
        }
        return null;
    }

    /**
     * Обработка события после закрытия карточки поручения
     */
    public void onAfterTaskEdit(){
        modelRefresh();
    }

    public boolean isDisableBtnStop(){
        if (getEditedItem() == null) return false;
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || !getEditedItem().isRunning();
    }
    
    @Override
    public boolean isReadOnly(){
        if (getEditedItem() == null) return false;
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || getEditedItem().isRunning() || getEditedItem().isCompleted() ;
    }
    
    /**
     * Формирует заголовок элемента модели
     * @param wfElement
     * @return 
     */
    public String getElementCaption(WFConnectedElem wfElement){
        String bundleName = wfElement.getCaption();
        if (StringUtils.isEmpty(bundleName)) return "";
        
        if (wfElement instanceof TaskElem){
            TaskElem taskElem = (TaskElem) wfElement;
            return taskElem.getCaption();
        }
        return getLabelFromBundle(bundleName);
    }          
    
    /**
     * Формирует ссылку на изображение для элемента модели
     * @param wfElement
     * @return 
     */
    public String getElementImage(WFConnectedElem wfElement){
        String image = wfElement.getImage();
        if (StringUtils.isEmpty(image)) return "";
        return "/resources/icon/" + image + ".png";
    }
    
    public void onOpenExeReport(ProcReport report){
        currentReport = report;
    }
     
    /**
     * Обработка события выбора шаблона из выпадающего списка
     * @param event 
     */
    public void onTemplSelector(ValueChangeEvent event){
        selectedTempl = (ProcTempl) event.getNewValue();
        if (selectedTempl != null){
            nameTemplate = selectedTempl.getName();
        } 
    }
    
    /* РАБОТА СО СПИСКОМ ДОКУМЕНТОВ */
    
    /**
     * Обработка события удаления документа из списка документов процесса
     * @param doc
     */
    public void onDeleteDocFromChilds(Doc doc){
        getEditedItem().getDocs().remove(doc);
        onItemChange();
    }
    
    /**
     * Обработка события добавления файла в процесс с созданием документа
     */
    public void onAddFile(){
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
        Doc doc = docFacade.createDocInUserFolder(attache.getName(), author, folder, attache);
        getEditedItem().getDocs().add(doc);
        onItemChange();
    }
    
    /* Загрузка файла через контрол на форме карточки процеса */
    public void onUploadFile(FileUploadEvent event) throws IOException{       
        attaches.clear();
        UploadedFile uploadFile = EscomFileUtils.handleUploadFile(event);        
        attaches.add(attacheBean.uploadAtache(uploadFile));
    }  
    
    /**
     * Обработка события выбора документа(ов) из селектора
     * @param event
     */
    public void onDocsSelected(SelectEvent event){
        List<Doc> docs = (List<Doc>) event.getObject();
        if (docs.isEmpty()) return;        
        getEditedItem().getDocs().addAll(docs);        
        onItemChange();
    }     
    
    /**
     * Обработка события закрытия карточки документа
     * @param event 
     */
    public void onUpdateAfterCloseDocForm(SelectEvent event){
        String exitResult = (String) event.getObject();
        if (!SysParams.EXIT_NOTHING_TODO.equals(exitResult)) {
           PrimeFaces.current().ajax().update("mainFRM:mainTabView:tblDocs");
        }
    }
    
    /* GETS & SETS */

    public Doc getSelectedDoc() {
        return selectedDoc;
    }
    public void setSelectedDoc(Doc selectedDoc) {
        this.selectedDoc = selectedDoc;
    }
    
    public List<ProcTempl> getTemplates() {
        if (templates == null && getEditedItem() != null){            
            templates = procTemplBean.findDetailItems(getEditedItem().getOwner());            
        }
        return templates;
    }
    public void setTemplates(List<ProcTempl> templates) {
        this.templates = templates;
    }
        
    public ProcTempl getSelectedTempl() {
        return selectedTempl;
    }
    public void setSelectedTempl(ProcTempl selectedTempl) {
        this.selectedTempl = selectedTempl;
    }
    
    public String getNameTemplate() {
        return nameTemplate;
    }
    public void setNameTemplate(String nameTemplate) {
        this.nameTemplate = nameTemplate;
    }
        
    public ProcReport getCurrentReport() {
        return currentReport;
    }
    public void setCurrentReport(ProcReport currentReport) {
        this.currentReport = currentReport;
    }
    
    /**
     * Получение из модели списка задач процесса
     * @return 
     */
    public List<Task> getTasksFromModel(){
        Scheme scheme = getScheme(); 
        List<Task> result = new ArrayList<>();
        if (scheme != null){
            result = getScheme().getElements().getTasks().entrySet().stream()
                .filter(tsk->tsk.getValue().getTask() != null)
                .map(tsk->tsk.getValue().getTask())
                .collect(Collectors.toList());
        }
        return result;
    }
    
    /**
     * Получение из модели списка та
     * @return 
     */
    public List<ProcTimer> getProcTimersFromModel(){
        Scheme scheme = getScheme(); 
        List<ProcTimer> result = new ArrayList<>();
        if (scheme != null){
            result = getScheme().getElements().getTimers().entrySet().stream()                
                .map(tsk->tsk.getValue().getProcTimer())
                .collect(Collectors.toList());
        }
        return result;
    }
        
    /**
     * Формирует имя элемента для вывода в заголовке формы
     * @return 
     */
    public String getNameBasedElement(){
        if (getBaseElement() == null) return "";
        String key = baseElement.getBundleKey();
        return getLabelFromBundle(key);
    }
    
    public DiagramModel getModel() {
        return model;
    }

    public WFConnectedElem getBaseElement() {
        return baseElement;
    }        
        
    @Override
    public Task getSourceItem(){        
        return currentTask;
    }
        
    public Task getCurrentTask() {
        return currentTask;
    }
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }    

    public WFConnectedElem getCopiedElement() {
        return copiedElement;
    }        
    
    public Scheme getScheme(){
        if (getEditedItem() == null) return null;
        return getEditedItem().getScheme();
    }
   
}