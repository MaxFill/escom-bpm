package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.escom.beans.ContainsTask;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.task.Task;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
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
import java.util.stream.Collectors;
import org.primefaces.model.diagram.connector.Connector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;

/**
 * Контролер формы "Карточка процесса"
 */
@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process> implements ContainsTask{
    private static final long serialVersionUID = -5558740260204665618L;

    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private TaskFacade taskFacade;
        
    private Element selectedElement = null;
    private Scheme scheme;
    private int defX = 8;
    private int defY = 8;
    private WFConnectedElem baseElement;

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

    @Override
    protected void checkItemBeforeSave(Process item, Set<String> errors) {
        super.checkItemBeforeSave(item, errors);
        Set<String> validateKeysMsg = new HashSet<>();            
        workflow.packScheme(scheme, validateKeysMsg);
        validateKeysMsg.forEach(key->errors.add(EscomMsgUtils.getMessageLabel(key)));
    }

    /**
     * Перед сохранением процесса
     * @param item
     */
    @Override
    protected void onBeforeSaveItem(Process item) {      
        if (getEditedItem().getScheme() != null){
            List<Task> liveTask = getTasks();
            List<Task> forRemove = new ArrayList<>();
            forRemove.addAll(getEditedItem().getScheme().getTasks());
            forRemove.removeAll(liveTask); //в списке остались только те элементы, которые нужно удалить
            if (!forRemove.isEmpty()){
                scheme.getTasks().removeAll(forRemove);
                editedTasks.removeAll(forRemove);
            }
        }    
        getEditedItem().setScheme(scheme);
        super.onBeforeSaveItem(item);
    }

    @Override
    protected void onAfterSaveItem(Process item){
        if (editedTasks.isEmpty()) return;        
        editedTasks.forEach(task -> taskFacade.edit(task));
    }
        
    @Override
    public void onAfterFormLoad(String beanId) {
        super.onAfterFormLoad(beanId);
        if (!isReadOnly()){
            addContextMenu();
        }
    }
    
    /* МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ */
    
    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        onItemChange();
        if (doSaveItem()){
            Set<String> errors = new HashSet<>();
            workflow.start(getEditedItem(), errors);
            if (!errors.isEmpty()){
                EscomMsgUtils.showErrorsMsg(errors);            
            } else {                
                EscomMsgUtils.succesMsg("ProcessSuccessfullyLaunched");
            }
            onReloadModel();
        }
    }
    
    /**
     * Обработка события прерывания процесса
     */
    public void onStop(){
        Set<String> errors = new HashSet<>();
        workflow.stop(getEditedItem(), errors);
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
        loadModel(getEditedItem().getScheme());
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
            Set<String> errors = new HashSet <>();
            workflow.unpackScheme(scheme, errors);
            this.scheme = scheme;
            if (errors.isEmpty()) {
                restoreModel();
            } else {
                EscomMsgUtils.showErrors(errors);
            }
        }        
    }

    /**
     * Перерисовка модели на странице формы
     */
    private void modelRefresh(){
        PrimeFaces.current().ajax().update("process:mainTabView:diagramm");        
        if (!isReadOnly()){
            addContextMenu();
        }
    }
    
    /**
     * Формирует графическую схему из данных модели процесса
     */
    private void restoreModel(){        
        model.clear();
        Map<String, Element> elementMap = new HashMap <>();
        scheme.getElements().getTasks().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getExits().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getLogics().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getEnters().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getStates().forEach((k, v)->elementMap.put(k, createElement(v)));
        scheme.getElements().getConditions().forEach((k, v)->elementMap.put(k, createElement(v)));
        StartElem startElem = scheme.getElements().getStartElem();
        elementMap.put(startElem.getUid(), createElement(startElem));
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
     * Очистка визуальной схемы процесса
     */
    public void onClearModel(){
        scheme = new Scheme(getEditedItem());
        model.clear();
        modelRefresh();
    }

    /**
     * Обоработка события удаления текущего визуального компонента со схемы
     */
    public void onElementDelete(){
        Set<String> errors = new HashSet <>();
        workflow.removeElement((WFConnectedElem)selectedElement.getData(), scheme, errors);
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
       } 
    }
    
    /**
     * Открытие карточки задачи
     * @param beanId 
     */
    @Override
    public void onOpenTask(String beanId){
        onOpenTask();
    }    
    public void onOpenTask(){   
        String beanName = ProcessCardBean.class.getSimpleName().substring(0, 1).toLowerCase() + ProcessCardBean.class.getSimpleName().substring(1);        
        sessionBean.openTask(beanId, beanName);
    }
    
    /**
     * Обработка события закрытия карточки свойств визуального компонента
     * @param event
     */
    public void onElementClose(SelectEvent event){
        if (event.getObject() == null) return;
        if (baseElement instanceof TaskElem){
            TaskElem taskElem = (TaskElem)baseElement;
            editedTasks.add(taskElem.getTask());            
        }
        onItemChange();
        modelRefresh();        
    }
    
    /**
     * Обработка события закрытия карточки задачи
     * @param event
     */
    public void onTaskClose(SelectEvent event){
        if (event.getObject() == null) return;
        editedTasks.add(currentTask);
        onItemChange();
        String updateElement = getFormName()+":mainTabView:concorderList";
        PrimeFaces.current().ajax().update(updateElement);
        modelRefresh();        
    }
    
    /**
     * Обработка события добавления в схему процесса одного или нескольких визуальных компонентов "Задача" из выбранных в селекторе штатных единиц
     * @param event
     */
    public void onStaffsSelected(SelectEvent event){
        List<Staff> executors = (List<Staff>) event.getObject();
        if (executors.isEmpty()) return;
        Set<String> errors = new HashSet<>();
        for (Staff executor : executors) {
            Set<String> metodErr = new HashSet<>();
            List<EndPoint> endPoints = new ArrayList<>();
            createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
            createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
            createTask(executor, "Согласовать документ!", defX, defY, endPoints, metodErr);
            defX = defX + 5;
            defY = defY + 5;         
            if (!metodErr.isEmpty()){
                errors.addAll(metodErr);
            }
        }
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrors(errors);
        } else { 
            modelRefresh();
            onItemChange();
        }
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Логическоe ветвление" 
     * @param name
     */
    public void onAddLogicElement(String name){
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        String caption = EscomMsgUtils.getBandleLabel(name);
        createLogic(caption, defX, defY, endPoints, new HashSet <>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Состояния" 
     * @param name
     */
    public void onAddStateElement(String name){
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        createState("?", "success", defX, defY, endPoints, new HashSet<>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Условие" 
     */
    public void onAddConditionElement(){
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT, DictWorkflowElem.STYLE_YES);
        createSourceEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_NO);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createCondition("?", defX, defY, endPoints, new HashSet<>());        
        finalAddElement();
    }

    /**
     * Обработка события добавления в схему процесса визуального компонента "Вход" 
     */
    public void onAddEnterElement(){
        List<EndPoint> endPoints = new ArrayList<>();
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createSourceEndPoint(endPoints, EndPointAnchor.TOP);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createEnter(defX, defY, endPoints, new HashSet<>());        
        finalAddElement();
    }
    
    /**
     * Обработка события добавления в схему процесса визуального компонента "Выход" 
     */
    public void onAddExitElement(){
        List<EndPoint> endPoints = new ArrayList<>();
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);                
        createExit(defX, defY, endPoints, new HashSet<>());        
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
    private Element createCondition(String nameCondition, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        ConditionElem condition = new ConditionElem(nameCondition, x, y);
        condition.setAnchors(makeAnchorElems(condition, endPoints));
        workflow.addCondition(condition, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(condition);
        }
        return null;
    }

    /**
     * Создание элемента "Логическое ветвление"
     * @param x
     * @param y
     * @param errors
     */
    private Element createLogic(String name, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        LogicElem logic = new LogicElem(name, x, y);
        logic.setAnchors(makeAnchorElems(logic, endPoints));
        workflow.addLogic(logic, scheme, errors);
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
    private Element createEnter(int x, int y, List<EndPoint> endPoints, Set<String> errors){
        EnterElem enter = new EnterElem("", x, y);
        enter.setAnchors(makeAnchorElems(enter, endPoints));
        workflow.addEnter(enter, scheme, errors);
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
    private Element createStart(int x, int y, List<EndPoint> endPoints, Set<String> errors){
        StartElem start = new StartElem("", x, y);
        start.setAnchors(makeAnchorElems(start, endPoints));
        workflow.addStart(start, scheme, errors);
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
    private Element createExit(int x, int y, List<EndPoint> endPoints, Set<String> errors){
        ExitElem exit = new ExitElem("", x, y);
        exit.setAnchors(makeAnchorElems(exit, endPoints));
        workflow.addExit(exit, scheme, errors);
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
    private Element createTask(Staff executor, String taskName, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        TaskElem taskElem = new TaskElem(taskName, x, y);
        Task task = taskFacade.createTask(taskName, executor, scheme, taskElem.getUid());
        taskElem.setTask(task);
        taskElem.setAnchors(makeAnchorElems(taskElem, endPoints));
        workflow.addTask(taskElem, scheme, errors);
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
    private Element createState(String caption, String styleType, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        StateElem state = new StateElem(caption, x, y);
        state.setAnchors(makeAnchorElems(state, endPoints));
        state.setStyleType(styleType);
        workflow.addState(state, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(state);
        }
        return null;
    }

    /**
     * Создание элемента "Коннектор"
     * @param from
     * @param to
     * @param errors
     */
    private void createConnector(AnchorElem from, AnchorElem to, String label, Set<String> errors){
        ConnectorElem connector = new ConnectorElem(label,  from, to);
        workflow.addConnector(connector, scheme, errors);
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
        scheme = new Scheme(getEditedItem());
        model.clear();
        Staff staff = staffFacade.findStaffByUser(getCurrentUser());
        Set<String> errors = new HashSet <>();
        List<EndPoint> endPoints = new ArrayList<>();
        
        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createStart(2, 4, endPoints, errors);        
        endPoints.clear();                

        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.LEFT);
        createTask(staff, "Согласовать документ!", 10, 2, endPoints, errors);
        endPoints.clear();

        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT, DictWorkflowElem.STYLE_YES);
        createSourceEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_NO);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createCondition("Все одобрили?", 28, 18, endPoints, errors);
        endPoints.clear();

        createSourceEndPoint(endPoints, EndPointAnchor.RIGHT);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createState("Документ согласован", "success", 36, 26, endPoints, errors);
        endPoints.clear();

        createSourceEndPoint(endPoints, EndPointAnchor.BOTTOM);
        createTargetEndPoint(endPoints, EndPointAnchor.TOP);
        createState("Документ не согласован", "fail", 18, 26, endPoints, errors);
        endPoints.clear();

        createTargetEndPoint(endPoints, EndPointAnchor.LEFT, DictWorkflowElem.STYLE_MAIN);
        createExit(50, 35, endPoints, errors);
      
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
        createConnector(sourceAnchor, targetAnchor, label, errors);
        onItemChange();
        
        if (StringUtils.isNotBlank(label)){
            Connection connection = findConnection(sourcePoint, targetPoint);
            connection.getOverlays().clear();
            connection.getOverlays().add(new LabelOverlay(EscomMsgUtils.getBandleLabel(label), "flow-label", 0.5));
            modelRefresh();            
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
        workflow.removeConnector(sourceAnchor, targetAnchor, scheme, errors);
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
    private void addContextMenu(){
        StringBuilder sb = new StringBuilder("addMenu([");
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

    public String getTaskStatus(Task task){
        if (task == null) return "";
        if (task.getBeginDate() == null) return EscomMsgUtils.getBandleLabel("NotStarted");
        StringBuilder sb = new StringBuilder();
        Date dateStart;
        if (task.getFactExecDate() == null){
            dateStart = DateUtils.today();                  
        } else {
            dateStart = task.getFactExecDate();            
        }
        String delta = DateUtils.differenceDays(dateStart.toInstant(), task.getPlanExecDate().toInstant()); 
        if (dateStart.before(task.getPlanExecDate())){
            sb.append(EscomMsgUtils.getBandleLabel("Remained"));
        } else {
            sb.append(EscomMsgUtils.getBandleLabel("Overdue")).append(" ").append(EscomMsgUtils.getBandleLabel("On"));
        }
        sb.append(" ").append(delta);
        return sb.toString();
    }
              
    public boolean isDisableBtnStop(){
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || !getEditedItem().isRunning();
    }
    
    @Override
    public boolean isReadOnly(){
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || getEditedItem().isRunning();
    }
       
    /* GETS & SETS */
    
    public List<Task> getTasks(){
        return scheme.getElements().getTasks().entrySet().stream()
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
    public Task getTask(){        
        return currentTask;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
        
    @Override
    public Boolean isShowExtTaskAtr() {
        return false;
    }
}
