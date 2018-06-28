package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ConditionFacade;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.StateFacade;
import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.task.Task;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.services.workflow.Workflow;
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
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.beanutils.BeanUtils;
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
    private ProcessBean processBean;
    @Inject
    private TaskBean taskBean;
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private ConditionFacade conditionFacade;
    @EJB
    private StatusesDocFacade statusesDocFacade;
    @EJB
    private StateFacade stateFacade;
        
    private Element selectedElement = null;

    private int defX = 8;
    private int defY = 8;
    private WFConnectedElem baseElement;
    private WFConnectedElem copiedElement;

    private final Set<Task> editedTasks = new HashSet<>();
    private Task currentTask;
    
    private final DefaultDiagramModel model = new DefaultDiagramModel();
    
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
        onReloadModel();
    }    

    /**
     * Перед сохранением процесса
     * @param item
     */
    @Override
    protected void onBeforeSaveItem(Process item){       
        List<Task> liveTask = getTasksFromModel();        
        List<Task> forRemove = new ArrayList<>();
        List<Task> schemeTasks = getScheme().getTasks();
        forRemove.addAll(schemeTasks);
        forRemove.removeAll(liveTask); //в списке остались только те элементы, которые нужно удалить
        if (!forRemove.isEmpty()){
            getScheme().getTasks().removeAll(forRemove);
            editedTasks.removeAll(forRemove);
        }       
        workflow.packScheme(getScheme());
        super.onBeforeSaveItem(item);
    }

    @Override
    protected void checkItemBeforeSave(Process process, Set<String> errors){        
        if (process.getPlanExecDate() == null ){
                errors.add("DeadlineIncorrect");
        } else 
            if (process.getPlanExecDate().before(new Date())){
                errors.add("DeadlineSpecifiedInPastTime");
            }
        super.checkItemBeforeSave(process, errors);
    }
    
    @Override
    public void onAfterFormLoad(String beanId) {
        super.onAfterFormLoad(beanId);
        if (!isReadOnly()){
            addElementContextMenu();
        }
    }
    
    @Override
    public boolean doSaveItem(){
        Boolean result = super.doSaveItem();
        modelRefresh();
        return result;
    }   
    
    /* МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ */
    
    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        onItemChange();
        if (doSaveItem()){
            Set<String> errors = new HashSet<>();
            workflow.start(getEditedItem(), getCurrentUser(), errors);
            if (!errors.isEmpty()){
                EscomMsgUtils.showErrorsMsg(errors);
            } else {
                getEditedItem().getState().setCurrentState(stateFacade.getRunningState());
                //processFacade.addLogEvent(getEditedItem(), DictLogEvents., getCurrentUser());
                setItemCurrentState(getEditedItem().getState().getCurrentState());
                EscomMsgUtils.succesMsg("ProcessSuccessfullyLaunched");
            }
            loadModel(getScheme());
            PrimeFaces.current().ajax().update(getFormName());
        }
    }
    
    /**
     * Обработка события прерывания процесса
     */
    public void onStop(){
        Set<String> errors = new HashSet<>();
        workflow.stop(getEditedItem(), getCurrentUser(), errors);
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
        } else {
            onReloadModel();
            EscomMsgUtils.warnMsg("ProcessExecutionInterrupted");
        }
    }
    
    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Обработка события перезагрузки визуальной схемы процесса
     */
    public void onReloadModel(){
        loadModel(getScheme());
        modelRefresh();
    }
    
    /**
     * Загрузка визуальной схемы процесса
     * @param scheme
     */
    public void loadModel(Scheme scheme){
        if (scheme == null){
            createScheme();            
        } else {
            workflow.unpackScheme(scheme);
            restoreModel();            
        }
    }

    /**
     * Перерисовка модели на странице формы
     */
    private void modelRefresh(){
        PrimeFaces.current().ajax().update("process:mainTabView:diagramm");               
        if (!isReadOnly()){
            addElementContextMenu();
        }
    }
    
    /**
     * Формирует графическую схему из данных модели процесса
     */
    private void restoreModel(){        
        model.clear();
        Map<String, Element> elementMap = new HashMap <>();        
        getScheme().getElements().getTasks().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getExits().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getLogics().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getEnters().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getStates().forEach((k, v)->elementMap.put(k, createElement(v)));
        getScheme().getElements().getConditions().forEach((k, v)->elementMap.put(k, createElement(v)));
        StartElem startElem = getScheme().getElements().getStartElem();
        elementMap.put(startElem.getUid(), createElement(startElem));
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
        workflow.removeElement((WFConnectedElem)selectedElement.getData(), getScheme(), errors);
        if (errors.isEmpty()) {
            model.removeElement(selectedElement);
            modelRefresh();
            onItemChange();
        } else {
            EscomMsgUtils.showErrors(errors);
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
            selectedElement.setX(x + "em");
            selectedElement.setY(y + "em");
            defX = Integer.valueOf(x) + 5;
            defY = Integer.valueOf(y) + 5;
            baseElement = (WFConnectedElem) selectedElement.getData();
            baseElement.setPosX(Integer.valueOf(x));
            baseElement.setPosY(Integer.valueOf(y));            
        }
    }
   
    /**
     * Обоработка события контекстного меню для открытия карточки свойств визуального компонента 
     */
    public void onElementOpenClick(){
        onElementClicked();        
        PrimeFaces.current().executeScript("document.getElementById('process:btnOpenElement').click();");
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
           openElementCard(DictDlgFrmName.FRM_CONDITION);
           return;
       } 
       if (baseElement instanceof StatusElem){
           openElementCard(DictDlgFrmName.FRM_DOC_STATUS);
           return;
       }
       if (baseElement instanceof ExitElem){
           openElementCard(DictDlgFrmName.FRM_EXIT);           
       }
    }
    
    /**
     * Служебный метод открытия карточки формы элемента схемы маршрута
     * @param formName 
     */
    private void openElementCard(String formName){
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> itemIds = new ArrayList<>();
        itemIds.add(beanId);
        paramMap.put(SysParams.PARAM_BEAN_ID, itemIds);
        List<String> beanNameList = new ArrayList<>();
        beanNameList.add(getBeanName());
        paramMap.put(SysParams.PARAM_BEAN_NAME, beanNameList);
        sessionBean.openDialogFrm(formName, paramMap);
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
                    EscomMsgUtils.warnMsg("CopyingObjectsTypeNotProvided");
                }                                 
    }
    
    private void doCopyElement(WFConnectedElem elem){
        try { 
            copiedElement = elem;
            BeanUtils.copyProperties(copiedElement, baseElement);
            PrimeFaces.current().executeScript("refreshContextMenu('process:mainTabView:diagramm');");
            EscomMsgUtils.succesFormatMsg("ObjectIsCopied", new Object[]{copiedElement.getCaption()}); 
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
                Element newElement = createTask(null, "", defX, defY, new HashSet<>());
                TaskElem newTaskElem = (TaskElem)newElement.getData();
                Task newTask = newTaskElem.getTask();
                TaskElem sourceTaskElem = (TaskElem) baseElement;
                Task sourceTask = sourceTaskElem.getTask();
                BeanUtils.copyProperties(newTask, sourceTask); 
                newTask.setTaskLinkUID(newTaskElem.getUid()); 
                StringBuilder sb = new StringBuilder();
                sb.append(EscomMsgUtils.getBandleLabel("Copy")).append(" ").append(sourceTask.getName());                
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
     * @param beanId 
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
        Element exit = model.findElement(baseElement.getUid());
        exit.setStyleClass(baseElement.getStyle());
        onItemChange();
        modelRefresh();        
    }
    
    /**
     * Обработка события закрытия карточки задачи
     * @param event
     */
    public void onAfterTaskClose(SelectEvent event){
        if (event.getObject() == null) return;
        Map<String, Object> exits = (Map)event.getObject();
        String result = (String) exits.get(SysParams.PARAM_EXIT_RESULT);
        switch (result){
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{
                editedTasks.add(currentTask);           
                onItemChange();
                modelRefresh();
                PrimeFaces.current().ajax().update("process:mainTabView:concorderList");      
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
            createTask(executor, "Согласовать документ!", defX, defY, metodErr); 
            defX = defX + 5;
            defY = defY + 5;
            if (!metodErr.isEmpty()){
                errors.addAll(metodErr);
            }
        }
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrors(errors);
        } else { 
            finalAddElement();
        }
    }   
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Логическоe ветвление" 
     * @param bundleName
     */
    public void onAddLogicElement(String bundleName){              
        createLogic(bundleName, defX, defY, new HashSet <>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Состояния" 
     * @param name
     */
    public void onAddStateElement(String name){
        createState(null, "success", defX, defY, new HashSet<>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Условие" 
     */
    public void onAddConditionElement(){
        createCondition(null, defX, defY, new HashSet<>());
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddEnterElement(){       
        createEnter(defX, defY, new HashSet<>());        
        finalAddElement();
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Выход" 
     */
    public void onAddExitElement(){               
        createExit(Boolean.TRUE, defX, defY, new HashSet<>());        
        finalAddElement();
    }
    
    /**
     * Завершает добавление элемента к визуальной схеме 
     * @param elementId 
     */
    private void finalAddElement(){        
        onItemChange();        
        modelRefresh();
    }
    
    /* СОЗДАНИЕ КОМПОНЕНТОВ СХЕМЫ ПРОЦЕССА */

    /**
     * Создание элемента "Условие" в модель процесса
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private Element createCondition(Condition condition, int x, int y, Set<String> errors){
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
            return modelAddElement(conditionElem);
        }
        return null;
    }

    /**
     * Создание элемента "Логическое ветвление"
     * @param x
     * @param y
     * @param errors
     */
    private Element createLogic(String name, int x, int y, Set<String> errors){
        LogicElem logic = new LogicElem(name, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT); 
        logic.setAnchors(makeAnchorElems(logic, endPoints));
        workflow.addLogic(logic, getScheme(), errors);
        if (errors.isEmpty()) {
            return modelAddElement(logic);
        }
        return null;
    }

    /**
     * Создание элемента "Вход в процесс"
     * @param x
     * @param y
     * @param errors
     */
    private Element createEnter(int x, int y, Set<String> errors){
        EnterElem enter = new EnterElem("", x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        enter.setAnchors(makeAnchorElems(enter, endPoints));
        workflow.addEnter(enter, getScheme(), errors);
        if (errors.isEmpty()) {
            return modelAddElement(enter);            
        }
        return null;
    }

    /**
     * Создание элемента "Старт процесса"
     * @param x
     * @param y
     * @param errors
     */
    private Element createStart(int x, int y, Set<String> errors){
        StartElem start = new StartElem("", x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        start.setAnchors(makeAnchorElems(start, endPoints));
        workflow.addStart(start, getScheme(), errors);
        if (errors.isEmpty()) {
            return modelAddElement(start);            
        }
        return null;
    }
    
    /**
     * Создание элемента "Вход в процесс"
     * @param x
     * @param y
     * @param errors
     */
    private Element createExit(Boolean finalize, int x, int y, Set<String> errors){
        ExitElem exit = new ExitElem("", finalize, x, y);
        List<EndPoint> endPoints = new ArrayList<>();
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_MAIN);
        exit.setAnchors(makeAnchorElems(exit, endPoints));
        workflow.addExit(exit, getScheme(), errors);
        if (errors.isEmpty()) {
            return modelAddElement(exit);
        }
        return null;
    }

    /**
     * Создание элемента "Поручение"
     * @param executor
     * @param x
     * @param y
     */
    private Element createTask(Staff executor, String taskName, int x, int y, Set<String> errors){
        TaskElem taskElem = new TaskElem(taskName, x, y);
        Task task = taskFacade.createTask(taskName, executor, getCurrentUser(), getScheme(), taskElem.getUid());
        taskElem.setTask(task);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        taskElem.setAnchors(makeAnchorElems(taskElem, endPoints));
        workflow.addTask(taskElem, getScheme(), errors);
        if (errors.isEmpty()){
            return modelAddElement(taskElem);         
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
    private Element createState(StatusesDoc docStatus, String styleType, int x, int y, Set<String> errors){        
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
            return modelAddElement(stateEl);
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
            label = EscomMsgUtils.getBandleLabel(keyLabel);
        }        
        conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));       
        if (connectorElem.isDone()){
            FlowChartConnector connector = new FlowChartConnector();
            connector.setPaintStyle("{strokeStyle:'#020202', lineWidth:3}");
            connector.setCornerRadius(10); 
            conn.setConnector(connector);
        }
        int h = conn.hashCode();
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
     * Создание новой схемы процесса
     * @return
     */
    private void createScheme(){
        Scheme scheme = new Scheme(getEditedItem());
        getEditedItem().setScheme(scheme);
        model.clear();
        Staff staff = staffFacade.findStaffByUser(getCurrentUser());
        Set<String> errors = new HashSet <>();
        
        createStart(2, 4, errors);                     

        createTask(staff, "Согласовать документ!", 10, 2, errors);
      
        createCondition(conditionFacade.find(1), 28, 18, errors);

        createState(statusesDocFacade.find(4), "success", 36, 26,  errors);
        
        createState(statusesDocFacade.find(6), "fail", 18, 26, errors);
        
        createExit(Boolean.TRUE, 50, 35, errors);
    }

    /**
     * Загрузка визуальной схемы процесса из шаблона
     */
    public void onLoadModelFromTempl(){
        //ToDo !
        modelRefresh();
    }
    
    /**
     * Сохранение модели процесса в шаблон
     */
    public void onSaveModelToTempl(){
        //ToDo !
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
                Overlay overlay = new LabelOverlay(EscomMsgUtils.getBandleLabel(label), "flow-label", 0.5);
                connection.getOverlays().add(overlay);
                modelRefresh();
            }
        }

        if (!errors.isEmpty()){
            Connection connection = findConnection(sourcePoint, targetPoint);
            model.disconnect(connection);
            modelRefresh();
            EscomMsgUtils.showErrors(errors);
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
         PrimeFaces.current().executeScript("addContextMenu('process:mainTabView:diagramm')");
        StringBuilder sb = new StringBuilder("addElementMenu([");
        model.getElements().stream()
                .filter(element-> !element.getStyleClass().equals(DictWorkflowElem.STYLE_START))
                .forEach(element-> {            
            sb.append("'process:mainTabView:diagramm-").append(element.getId()).append("', "); 
        });
        sb.append("])");      
        PrimeFaces.current().executeScript(sb.toString());
       
    }    
    
    /* ПРОЧИЕ МЕТОДЫ */    
    
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

    /**
     * Формирование статуса задачи в зависимости от её выполнения
     * @param task
     * @return 
     */
    public String getTaskStatus(Task task){             
        return processBean.getTaskStatus(task);
    }
              
    public boolean isDisableBtnStop(){
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || !getEditedItem().isRunning();
    }
    
    @Override
    public boolean isReadOnly(){
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
     * Обработка события выбора документа из селектора
     * @param event
     */
    public void onDocSelected(SelectEvent event){
        List<Doc> items = (List<Doc>) event.getObject();
        if (items.isEmpty()) return;
        Doc doc = items.get(0);
        onItemChange();
        getEditedItem().setDoc(doc);
        StringBuilder sb = new StringBuilder(getEditedItem().getOwner().getName());
        sb.append(" документа <").append(doc.getFullName()).append(">");
        getEditedItem().setName(sb.toString());
    }    
    
    /* GETS & SETS */
    
    public List<Task> getTasksFromModel(){
        return getScheme().getElements().getTasks().entrySet().stream()
                .map(tsk->tsk.getValue().getTask())
                .collect(Collectors.toList());       
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
        return getEditedItem().getScheme();
    }
    
   
}