package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.docStatuses.StatusesDocFacade;
import com.maxfill.model.basedict.process.conditions.Condition;
import com.maxfill.model.basedict.process.conditions.ConditionFacade;
import com.maxfill.model.basedict.process.procedures.Procedure;
import com.maxfill.model.basedict.process.procedures.ProcedureFacade;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.schemes.elements.AnchorElem;
import com.maxfill.model.basedict.process.schemes.elements.ConditionElem;
import com.maxfill.model.basedict.process.schemes.elements.ConnectorElem;
import com.maxfill.model.basedict.process.schemes.elements.EnterElem;
import com.maxfill.model.basedict.process.schemes.elements.ExitElem;
import com.maxfill.model.basedict.process.schemes.elements.LogicElem;
import com.maxfill.model.basedict.process.schemes.elements.MessageElem;
import com.maxfill.model.basedict.process.schemes.elements.ProcedureElem;
import com.maxfill.model.basedict.process.schemes.elements.StartElem;
import com.maxfill.model.basedict.process.schemes.elements.StatusElem;
import com.maxfill.model.basedict.process.schemes.elements.TaskElem;
import com.maxfill.model.basedict.process.schemes.elements.TimerElem;
import com.maxfill.model.basedict.process.schemes.elements.WFConnectedElem;
import com.maxfill.model.basedict.process.schemes.elements.WorkflowElements;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import com.maxfill.model.basedict.process.schemes.elements.SubProcessElem;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.process.timers.ProcTimerFacade;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.endpoint.RectangleEndPoint;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;
import org.primefaces.model.diagram.overlay.Overlay;

/**
 * Бин для работы с моделью процесса
 */
@Named
@Dependent
public class DiagramBean extends BaseViewBean<ProcessCardBean>{
    private static final long serialVersionUID = -4403976059082444626L;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.00"); 
             
    @Inject
    private TaskBean taskBean;
    
    @EJB
    private Workflow workflow;
    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private ProcessTemplFacade processTemplFacade;
    @EJB
    private ProcTimerFacade procTimerFacade;
    @EJB
    private ProcedureFacade procedureFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private ConditionFacade conditionFacade;
    @EJB
    private StatusesDocFacade statusesDocFacade;
            
    private Element selectedElement = null;

    private double defX = 8;
    private double defY = 8;
    
    private WFConnectedElem baseElement;
    private WFConnectedElem copiedElement;
    
    private Task currentTask;
        
    private ProcTempl selectedTempl;
      
    private Scheme scheme;
    private boolean readOnly;
    
    private final DefaultDiagramModel model = new DefaultDiagramModel();  
    
    private Process process;    
    
    @Override
    public String onCloseCard(Object param){
        workflow.packScheme(scheme);
        if (isItemChange){
            param = SysParams.EXIT_NEED_UPDATE;
        } else {
            param = SysParams.EXIT_NOTHING_TODO;
        }
        return super.onCloseCard(param);
    }

    @Override
    public void doBeforeOpenCard(Map params) {
        if (!isReadOnly()){
            addElementContextMenu();
        }
    }
    
    public void prepareModel(Process process){
        this.process = process;
        model.setMaxConnections(-1);
        model.getDefaultConnectionOverlays().add(new ArrowOverlay(20, 20, 1, 1));
        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:2}");
        connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
        connector.setCornerRadius(10);
        model.setDefaultConnector(connector);
        scheme = process.getScheme();
        loadModel(scheme);
    }

    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */
    
    /**
     * Перерисовка модели на странице формы
     */
    public void modelRefresh(){        
        restoreModel();
        onItemChange();
        PrimeFaces.current().ajax().update("southFRM:diagramm");
        if (!isReadOnly()){
            addElementContextMenu();
        }
    }        

    /**
     * Очистка визуальной схемы процесса
     */
    public void onClearModel(){        
        scheme.setElements(new WorkflowElements());        
        modelRefresh();
    }
    
    /**
     * Загрузка визуальной схемы процесса
     * @param scheme
     */
    public void loadModel(Scheme scheme){       
        workflow.unpackScheme(scheme);        
        restoreModel();
    } 
    
    /**
     * Формирует графическую схему из данных модели процесса
     */
    private void restoreModel(){
        model.clear();
        model.getConnections().clear();
        Map<String, Element> elementMap = new HashMap <>();
        scheme.getElements().getTasks().forEach((k, taskEl)->{             
            if (taskEl.getTask() == null){
                Staff staff = null;
                if (taskEl.getStaffId() != null){
                    staff = staffFacade.find(taskEl.getStaffId());
                }
                Task task = taskFacade.createTaskInProc(staff, getCurrentUser(), process, taskEl.getUid());
                task.setConsidInProcReport(taskEl.getConsidInProc());
                taskEl.setTask(task);
                scheme.getTasks().add(task);
            }
            elementMap.put(k, createElement(taskEl));
        });
        scheme.getElements().getTimers().forEach((k, timerEl)->{
                if (timerEl.getProcTimer() == null){
                    ProcTimer procTimer = procTimerFacade.createTimer(process, scheme, timerEl.getUid());
                    timerEl.setProcTimer(procTimer);
                    procTimer.setRepeatType(timerEl.getRepeatType());
                    procTimer.setStartType(timerEl.getStartType());
                    scheme.getTimers().add(procTimer);
                }
                elementMap.put(k, createElement(timerEl));
            });
        scheme.getElements().getSubprocesses().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getMessages().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getProcedures().forEach((k, v)->elementMap.put(k, createElement(v))); 
        scheme.getElements().getExits().forEach((k, v)->elementMap.put(k, createElement(v)));        
        scheme.getElements().getLogics().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getEnters().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getStates().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getConditions().forEach((k, v)->elementMap.put(k, createElement(v)));
        StartElem startElem = scheme.getElements().getStartElem();
        if (startElem != null){
            elementMap.put(startElem.getUid(), createElement(startElem));
        }
        List<Connection> connections = new ArrayList <>();
        scheme.getElements().getConnectors().stream().forEach(connectorElem->{
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
            connector.setPaintStyle("{strokeStyle:'blue', lineWidth:2}");
            connector.setCornerRadius(10); 
            conn.setConnector(connector);
        }
        return conn;
    }
    
    private Element createElement(WFConnectedElem wfElement){
        String x = wfElement.getPosX() + "px";
        String y = wfElement.getPosY() + "px";
        Element element = new Element(wfElement, x, y);        
        List<EndPoint> endPoints = restoreEndPoints(wfElement.getAnchors());
        endPoints.forEach(endPoint -> element.addEndPoint(endPoint));
        element.setId(wfElement.getUid());
        element.setStyleClass(wfElement.getStyle());
        return element;
    }         
    
    /**
     * Обоработка события удаления текущего визуального компонента со схемы
     */
    public void onElementDelete(){
        Set<String> errors = new HashSet <>();
        workflow.removeElement(baseElement, scheme, errors);
        if (errors.isEmpty()) {
            model.removeElement(selectedElement);            
            modelRefresh();            
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
            defX = Float.valueOf(params.get("posX"));
            defY = Float.valueOf(params.get("posY"));            
            String x = getX();
            String y = getY();
            String id = paramId.substring(paramId.indexOf("-") + 1);
            selectedElement = model.findElement(id);
            if (selectedElement != null){
                baseElement = (WFConnectedElem) selectedElement.getData();
                baseElement.setPosX(x);
                baseElement.setPosY(y);                
            }
            if (!isReadOnly()){
                onItemChange();
            }
        }
    }

    /**
     * Обоработка события контекстного меню для открытия карточки свойств визуального компонента 
     */
    public void onElementOpenClick(){
        onElementClicked();        
        PrimeFaces.current().executeScript("document.getElementById('southFRM:btnOpenElement').click();");
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
        if (baseElement instanceof SubProcessElem){
            openElementCard(DictFrmName.FRM_SUB_PROCESS); 
            return;
        }
        modelRefresh();
    }
    
    /**
     * Служебный метод открытия карточки формы элемента схемы маршрута
     * @param formName 
     */
    private void openElementCard(String formName){
        sessionBean.openDialogFrm(formName, getParamsMap());
    }            
    
    /**
     * Открытие карточки задачи
     */  
    public void onOpenTask(){ 
        setSourceItem(currentTask);
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
            taskElem.setConsidInProc(currentTask.getConsidInProcReport());
            onAfterTaskClose(event);
            return;
        } else 
            if (baseElement instanceof ExitElem){
                Element exit = model.findElement(baseElement.getUid());
                exit.setStyleClass(baseElement.getStyle());                     
            } else 
                if (baseElement instanceof StatusElem){
                    Element status = model.findElement(baseElement.getUid());
                    status.setStyleClass(baseElement.getStyle());
                }                     
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
                Element task = model.findElement(baseElement.getUid());
                task.setStyleClass(baseElement.getStyle());
                modelRefresh();
                break;
            }
            case SysParams.EXIT_EXECUTE:{
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
            beforeAddElement();
            baseElement = createTask(executor, metodErr); 
            if (!metodErr.isEmpty()){
                errors.addAll(metodErr);
            }
        }
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
        } else { 
            modelRefresh();
        }
    }   
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Логическоe ветвление" 
     * @param bundleName
     */
    public void onAddLogicElement(String bundleName){              
        beforeAddElement();
        baseElement = createLogic(bundleName, new HashSet <>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Состояния" 
     * @param name
     */
    public void onAddStateElement(){
        beforeAddElement();
        baseElement = createState(null, "success", new HashSet<>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Условие" 
     */
    public void onAddConditionElement(){
        beforeAddElement();
        baseElement = createCondition(null, new HashSet<>());
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Таймер"
     */
    public void onAddTimerElement() {
        beforeAddElement();
        baseElement = createTimer(new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Процедура"
     */
    public void onAddProcElement() {
        beforeAddElement();
        baseElement = createProcedure(null, new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Сообщение"
     */
    public void onAddMessageElement() {
        beforeAddElement();
        baseElement = createMessage(new HashSet<>());
        finalAddElement(); 
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddEnterElement(){
        beforeAddElement();
        baseElement = createEnter(new HashSet<>());
        finalAddElement();
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddStartElement(){       
        if (scheme.getElements().getStartElem() == null){
            defX = 3;
            defY = 3;
            baseElement = createStart(new HashSet<>());        
            finalAddElement();            
        } else {
            MsgUtils.errorMsg("SchemeCanBeOnlyOneStartElement");
        }
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Выход" 
     */
    public void onAddExitElement(){
        beforeAddElement();
        baseElement = createExit(Boolean.TRUE, new HashSet<>());        
        finalAddElement();
    }
    
    /**
     * Обработка события добавления в схему процесс визуального компонента "Подпроцесс"
     */
    public void onAddSubProcessElement(){
        beforeAddElement();
        baseElement = createSubProcess(null, new HashSet<>());
        finalAddElement();
    }
    
    private void beforeAddElement(){
        defX = defX + 45;
        defY = defY + 45;    
    }
    
    /**
     * Завершает добавление элемента к визуальной схеме 
     * @param elementId 
     */
    private void finalAddElement(){
        onItemChange();
        onElementOpen();
    }
    
    /* СОЗДАНИЕ КОМПОНЕНТОВ СХЕМЫ ПРОЦЕССА */

    /**
     * Создание элемента "Условие" в модели процесса
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private ConditionElem createCondition(Condition condition, Set<String> errors){
        ConditionElem conditionElem;
        String x = getX();
        String y = getY();
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
        workflow.addCondition(conditionElem, scheme, errors);
        if (errors.isEmpty()) {
            modelAddElement(conditionElem);
            return conditionElem;
        }
        return null;
    }

    /**
     * Создание элемента "Таймер" в модели процесса
     * @param errors
     * @return 
     */
    private TimerElem createTimer(Set<String> errors){        
        TimerElem timer = new TimerElem(null, getX(), getY());
        ProcTimer procTimer  = procTimerFacade.createTimer(process, scheme, timer.getUid());        
        timer.setProcTimer(procTimer);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        timer.setAnchors(makeAnchorElems(timer, endPoints));
        workflow.addTimer(timer, scheme, errors);
        if (errors.isEmpty()) {
            modelAddElement(timer);
            return timer;
        }
        return null;
    }
    
    private MessageElem createMessage(Set<String> errors){        
        MessageElem element = new MessageElem(null, getX(), getY());
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        element.setAnchors(makeAnchorElems(element, endPoints));
        workflow.addMessage(element, scheme, errors);
        if (errors.isEmpty()) {
            modelAddElement(element);
            return element;
        }
        return null;
    }
    
     /**
     * Создание элемента "Подпроцесс"
     * @param procedure
     * @param errors
     * @return 
     */
    private SubProcessElem createSubProcess(Process process, Set<String> errors){
        SubProcessElem subProcElem;
        String x = getX();
        String y = getY();
        if (process != null){
            subProcElem = new SubProcessElem(process.getName(), process.getId(), x, y);
        } else {
            subProcElem = new SubProcessElem("???", null, x, y);
        }
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        subProcElem.setAnchors(makeAnchorElems(subProcElem, endPoints));
        workflow.addSubProcess(subProcElem, scheme, errors);
        if (errors.isEmpty()) {
            modelAddElement(subProcElem);
            return subProcElem;
        }
        return null;
    }
    
    /**
     * Создание элемента "Процедура"
     * @param procedure
     * @param errors
     * @return 
     */
    private ProcedureElem createProcedure(Procedure procedure, Set<String> errors){
        ProcedureElem procedureElem;
        String x = getX();
        String y = getY();
        if (procedure != null){
            procedureElem = new ProcedureElem(procedure.getName(), procedure.getId(), x, y);
        } else {
            procedureElem = new ProcedureElem("???", null, x, y);
        }
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        procedureElem.setAnchors(makeAnchorElems(procedureElem, endPoints));
        workflow.addProcedure(procedureElem, scheme, errors);
        if (errors.isEmpty()) {
            modelAddElement(procedureElem);
            return procedureElem;
        }
        return null;
    }        
    
    /**
     * Создание элемента "Логическое ветвление"
     * @param x
     * @param y
     * @param errors
     */
    private LogicElem createLogic(String name, Set<String> errors){
        LogicElem logic = new LogicElem(name,  getX(), getY());
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);         
        logic.setAnchors(makeAnchorElems(logic, endPoints));
        workflow.addLogic(logic, scheme, errors);
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
    private EnterElem createEnter(Set<String> errors){
        EnterElem enter = new EnterElem("",  getX(), getY());
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        enter.setAnchors(makeAnchorElems(enter, endPoints));
        workflow.addEnter(enter, scheme, errors);
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
    private StartElem createStart(Set<String> errors){
        StartElem start = new StartElem("",  getX(), getY());
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        start.setAnchors(makeAnchorElems(start, endPoints));
        workflow.addStart(start, scheme, errors);
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
    private ExitElem createExit(Boolean finalize, Set<String> errors){        
        ExitElem exit = new ExitElem("", finalize, getX(), getY());
        List<EndPoint> endPoints = new ArrayList<>();
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_MAIN);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP, DictWorkflowElem.STYLE_MAIN);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM, DictWorkflowElem.STYLE_MAIN);
        exit.setAnchors(makeAnchorElems(exit, endPoints));
        workflow.addExit(exit, scheme, errors);
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
    private TaskElem createTask(Staff executor, Set<String> errors){        
        TaskElem taskElem = new TaskElem("", getX(), getY());
        Task task = taskFacade.createTaskInProc(executor, getCurrentUser(), process, taskElem.getUid());
        taskElem.setTask(task);
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        taskElem.setAnchors(makeAnchorElems(taskElem, endPoints));
        workflow.addTask(taskElem, scheme, errors);
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
    private StatusElem createState(StatusesDoc docStatus, String styleType, Set<String> errors){        
        StatusElem stateEl;
        String x = getX();
        String y = getY();
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
        workflow.addState(stateEl, scheme, errors);
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

    /* *** СОЗДАНИЕ КОННЕКТОРОВ и ЯКОРЕЙ *** */

    /**
     * Создание точки приёмника для элемента
     * @param anchor
     * @return 
     */
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
     * Создание точки приёмника для элемента
     * @param anchor
     * @return 
     */
    private EndPoint createTargetEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor) {
        return createTargetEndPoint(endPoints, anchor, DictWorkflowElem.STYLE_MAIN);
    }
    
    /**
     * Создание точки источника для элемента
     * @param anchor
     * @return 
     */
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
     * Создание точки источника для элемента
     * @param anchor
     * @return 
     */
    private EndPoint createSourceEndPoint(List<EndPoint> endPoints, EndPointAnchor anchor) {
        return createSourceEndPoint(endPoints, anchor, DictWorkflowElem.STYLE_MAIN);
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
        connect(wfSource, wfTarget, sourcePoint, targetPoint);                
    }

    private void connect(WFConnectedElem wfSource, WFConnectedElem wfTarget, EndPoint sourcePoint, EndPoint targetPoint){
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
        
        if (workflow.createConnector(sourceAnchor, targetAnchor, scheme, label, errors) != null){  //если коннектор создался            
            if (StringUtils.isNotBlank(label)){
                Connection connection = findConnection(sourcePoint, targetPoint);
                if (connection != null){
                    connection.getOverlays().clear();
                    Overlay overlay = new LabelOverlay(MsgUtils.getBandleLabel(label), "flow-label", 0.5);
                    connection.getOverlays().add(overlay);                    
                }    
            }
            modelRefresh();
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
        disconnect(wfSource, wfTarget, sourcePoint, targetPoint);
    }

    public void onConnectionChange(ConnectionChangeEvent event) {
        WFConnectedElem wfSource = (WFConnectedElem) event.getOriginalSourceElement().getData();
        WFConnectedElem wfTarget = (WFConnectedElem) event.getOriginalTargetElement().getData();
        EndPoint sourcePoint = event.getOriginalSourceEndPoint();
        EndPoint targetPoint = event.getOriginalTargetEndPoint();
        disconnect(wfSource, wfTarget, sourcePoint, targetPoint);
    }

    private void disconnect(WFConnectedElem wfSource, WFConnectedElem wfTarget, EndPoint sourcePoint, EndPoint targetPoint){
        Set<String> errors = new HashSet <>();
        AnchorElem sourceAnchor = wfSource.getAnchorsById(sourcePoint.getId());
        AnchorElem targetAnchor = wfTarget.getAnchorsById(targetPoint.getId());
        workflow.removeConnector(sourceAnchor, targetAnchor, scheme, errors);
        onItemChange();
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
     * Добавление контекстного меню к элементам схемы процесса
     */
    private void addElementContextMenu(){
        //PrimeFaces.current().executeScript("addContextMenu('southFRM:diagramm')");
        StringBuilder sb = new StringBuilder("addElementMenu([");
        model.getElements().stream()
                .filter(element-> !element.getStyleClass().equals(DictWorkflowElem.STYLE_START))
                .forEach(element-> {            
            sb.append("'southFRM:diagramm-").append(element.getId()).append("', "); 
        });
        sb.append("]);");
        PrimeFaces.current().executeScript(sb.toString());
    }    
    
    /* ШАБЛОНЫ ПРОЦЕССА */    

    /**
     * Обработка события выбора шаблона для загрузки модели
     * @param event 
     */
    public void onTemplSelectorLoad(SelectEvent event){
        List<ProcTempl> procTempls = (List<ProcTempl>) event.getObject();
        if (CollectionUtils.isEmpty(procTempls)) return;
        selectedTempl = procTempls.get(0);
        changeSchemeName(selectedTempl);
        onLoadModelFromTempl();        
        MsgUtils.succesFormatMsg("TemplateLoad", new Object[]{selectedTempl.getNameEndElipse()} );
    }
    
    /**
     * Обработка события выбора шаблона для сохранения модели
     * @param event 
     */
    public void onTemplSelectorSave(SelectEvent event){
        List<ProcTempl> procTempls = (List<ProcTempl>) event.getObject();
        if (CollectionUtils.isEmpty(procTempls)) return;
        selectedTempl = procTempls.get(0);
        changeSchemeName(selectedTempl);
        onSaveModelAsTempl();        
    }
    
    /**
     * Загрузка визуальной схемы процесса из шаблона
     */
    private void onLoadModelFromTempl(){        
        Integer termHours = processTypesFacade.getProcTypeForOpt(process.getOwner()).getTermHours();
        if (termHours != null){
            Date planExecDate = DateUtils.addHour(new Date(), termHours);
            process.setPlanExecDate(planExecDate);
        }
        scheme.setElements(new WorkflowElements());  
        scheme.setPackElements(selectedTempl.getElements());        
        workflow.unpackScheme(scheme);        
        workflow.clearScheme(scheme);
        restoreModel();
        PrimeFaces.current().ajax().update("mainFRM");        
        PrimeFaces.current().ajax().update("southFRM:diagramm");
        addElementContextMenu();
        onItemChange();
    }
    
    /**
     * Сохранение модели процесса в шаблон
     */
    public void onSaveModelAsTempl(){
        Set<String> errors = new HashSet<>();
        workflow.validateScheme(scheme, false, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        workflow.packScheme(scheme);
        selectedTempl.setElements(scheme.getPackElements());
        processTemplFacade.edit(selectedTempl);
        processTemplFacade.addLogEvent(selectedTempl,  DictLogEvents.CHANGE_EVENT, getCurrentUser());
        MsgUtils.succesFormatMsg("TemplateSaved", new Object[]{selectedTempl.getNameEndElipse()} );
    }    
    
    /* *** КОПИРОВАНИЕ ВСТАВКА *** /
    
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
                } else 
                    if (baseElement instanceof MessageElem){
                        doCopyElement(new MessageElem());
                    } else
                        if (baseElement instanceof TimerElem){
                            doCopyElement(new TimerElem());
                        } else
                            if (baseElement instanceof ProcedureElem){
                                doCopyElement(new ProcedureElem());
                            } else {
                                MsgUtils.warnMsg("CopyingObjectsTypeNotProvided");
                            }                                 
    }
    
    private void doCopyElement(WFConnectedElem elem){
        try { 
            copiedElement = elem;
            BeanUtils.copyProperties(copiedElement, baseElement);
            PrimeFaces.current().executeScript("refreshContextMenu('southFRM:diagramm');");
            MsgUtils.succesFormatMsg("ObjectIsCopied", new Object[]{copiedElement.getCaption()}); 
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);        
        }  
    }
    
    /**
     * Обработка события вставки скопированного элемента
     */
    public void onElementPaste(){
        onItemChange();
        try {
            if (copiedElement instanceof TaskElem){           
                TaskElem newTaskElem = createTask(null, new HashSet<>());                
                Task newTask = newTaskElem.getTask();
                TaskElem sourceTaskElem = (TaskElem) copiedElement;
                Task sourceTask = sourceTaskElem.getTask();
                BeanUtils.copyProperties(newTask, sourceTask); 
                newTask.setTaskLinkUID(newTaskElem.getUid()); 
                StringBuilder sb = new StringBuilder();
                sb.append(MsgUtils.getBandleLabel("Copy")).append(" ").append(sourceTask.getName());                
                newTask.setName(sb.toString());
                finalAddElement();
            } else 
                if (copiedElement instanceof ConditionElem){
                    ConditionElem sourceElem = (ConditionElem) copiedElement;
                    Condition condition = conditionFacade.find(sourceElem.getConditonId());
                    createCondition(condition, new HashSet<>());
                    finalAddElement();
                } else
                    if (copiedElement instanceof ProcedureElem){
                        ProcedureElem sourceElem = (ProcedureElem) copiedElement;
                        Procedure procedure = procedureFacade.find(sourceElem.getProcedureId());
                        createProcedure(procedure, new HashSet<>());
                        finalAddElement();
                    } else
                        if (copiedElement instanceof StatusElem){
                            StatusElem sourceElem = (StatusElem) copiedElement;
                            StatusesDoc docStatus = statusesDocFacade.find(sourceElem.getDocStatusId());
                            createState(docStatus, sourceElem.getStyleType(), new HashSet<>());
                            finalAddElement();
                        } else
                            if (copiedElement instanceof TimerElem){
                                createTimer(new HashSet<>());
                                finalAddElement();
                            } else
                                if (copiedElement instanceof MessageElem){ 
                                    createMessage(new HashSet<>());
                                    finalAddElement();
                                }
        } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /* ПРОЧИЕ МЕТОДЫ */
       
    /**
     * Формирует заголовок элемента модели
     * @param wfElement
     * @return 
     */
    public String getElementCaption(WFConnectedElem wfElement){        
        String bundleName = wfElement.getCaption();
        if (StringUtils.isEmpty(bundleName)) return "";               
        try {
            return MsgUtils.getBandleLabel(bundleName);
        } catch(MissingResourceException ex){}
        return bundleName;
    } 
            
    /**
     * Формирует ссылку на изображение для элемента модели
     * @param wfElement
     * @return 
     */
    public String getElementImage(WFConnectedElem wfElement){
        String image = wfElement.getImage();
        if (org.apache.commons.lang.StringUtils.isEmpty(image)) return "";
        return "/resources/icon/" + image + ".png";
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
    
    public boolean isReadOnly(){        
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private String getX(){
        return (DECIMAL_FORMAT.format(defX)).replace(",", ".");
    }
    
    private String getY(){
        return (DECIMAL_FORMAT.format(defY)).replace(",", ".");
    }
    
    private void changeSchemeName(ProcTempl procTempl){
        scheme.setName(procTempl.getNameEndElipse());
    }
    
    /* GETS & SETS */     
    
    public WFConnectedElem getBaseElement() {
        return baseElement;
    }                      
    public WFConnectedElem getCopiedElement() {
        return copiedElement;
    }  
    
    public DiagramModel getModel() {
        return model;
    }        
        
    public ProcTempl getSelectedTempl() {
        return selectedTempl;
    }
    public void setSelectedTempl(ProcTempl selectedTempl) {
        this.selectedTempl = selectedTempl;
    }    
    
    public Task getCurrentTask() {
        return currentTask;
    }
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public Process getProcess() {
        return process;
    }

    public Scheme getScheme() {
        return scheme;
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_DIAGRAMMA;
    }

    @Override
    public String getFormHeader() {
        if (scheme == null) return "";
        return scheme.getName();
    }

    @Override
    public boolean isFullPageMode(){
        return false;
    }
    
    @Override
    public boolean isSouthShow(){
        return true;
    }
}