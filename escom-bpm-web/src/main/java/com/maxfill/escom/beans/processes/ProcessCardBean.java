package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.services.workflow.Workflow;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.diagram.ConnectEvent;
import org.primefaces.event.diagram.ConnectionChangeEvent;
import org.primefaces.event.diagram.DisconnectEvent;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.*;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.overlay.LabelOverlay;

/**
 * Контролер формы "Карточка процесса"
 */
@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process>{
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private StaffFacade staffFacade;

    private Element selectedElement = null;
    private Scheme scheme;
    private int defX = 8;
    private int defY = 8;

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
        loadModel(getEditedItem().getScheme());
    }


    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Обработка события после закрытия карточки поручения
     */
    public void onAfterTaskEdit(){
        visualModelRefresh();
    }

    /**
     * Обоработка события удаления элемента со схемы
     * @param element
     */
    public void onDeleteElement(Element element){
        model.removeElement(element);
        visualModelRefresh();
    }

    /**
     * Определяет возможность отображения facet в элементе модели Поручение панели с кнопкой
     * @param el
     * @return
     */
    public boolean canShowTaskFacet(Object el){
        boolean flag = el != null && el instanceof Task;
        return flag;
    }

    /**
     * Обработка события выделения мышью элемента на визуальной схеме процесса
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
        }
    }

    /**
     * Обоработка события открытия карточки элемента по двойному клику
     */
    public void onElementOpen(){
        onElementClicked();
        //ToDo
    }

    /**
     * Обработка события добавления Логического элемента в визуальную модель процесса
     * @param name
     */
    public void onAddLogicElement(String name){
        EndPointAnchor[] source = {EndPointAnchor.BOTTOM, EndPointAnchor.RIGHT};
        EndPointAnchor[] target = {EndPointAnchor.TOP, EndPointAnchor.LEFT};
        String caption = EscomMsgUtils.getBandleLabel(name);
        addLogic(caption, defX, defY, source, target, new HashSet <>());
        visualModelRefresh();
    }

    /**
     * Обработка события добавления Условия в визуальную модель процесса
     */
    public void onAddConditionElement(){
        addCondition("?", defX, defY, new HashSet<>());
        visualModelRefresh();
    }

    /**
     * Обработка события добавления Состояния в визуальную модель процесса
     * @param name
     */
    public void onAddStateElement(String name){
        EndPointAnchor[] source = {EndPointAnchor.BOTTOM, EndPointAnchor.RIGHT};
        EndPointAnchor[] target = {EndPointAnchor.TOP, EndPointAnchor.LEFT};
        addState("?", "success", defX, defY, source, target, new HashSet<>());
        visualModelRefresh();
    }

    /**
     * Добавление условия в модель
     * @param condition
     * @param x
     * @param y
     * @return
     */
    private ConditionElement modelAddCondition(Condition condition, int x, int y){
        ConditionElement element = new ConditionElement(condition, x + "em", y + "em");
        EndPoint endPointYes = createRectangleEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointNo = createRectangleEndPoint(EndPointAnchor.RIGHT);
        EndPoint endPointIn = createDotEndPoint(EndPointAnchor.TOP);
        endPointYes.setSource(true);
        endPointYes.setId("yes");
        endPointNo.setSource(true);
        endPointNo.setId("no");
        endPointIn.setTarget(true);
        endPointIn.setId("in");
        endPointYes.setStyle("{fillStyle:'#099b05'}");
        endPointNo.setStyle("{fillStyle:'#C33730'}");
        element.addEndPoint(endPointYes);
        element.addEndPoint(endPointNo);
        element.addEndPoint(endPointIn);
        element.setStyleClass("ui-diagram-condition");
        element.setTitle(condition.getCaption());
        model.addElement(element);
        return element;
    }

    /**
     * Добавляет элемент в модель
     * @param schemeElement
     */
    private Element modelAddElement(SchemeElement schemeElement, int x, int y, EndPointAnchor[] source, EndPointAnchor[]  target, String styleClass){
        Element element = new Element(schemeElement, x + "em", y + "em");
        for (EndPointAnchor anchor : source){
            EndPoint pointSource = createRectangleEndPoint(anchor);
            pointSource.setSource(true);
            element.addEndPoint(pointSource);
        }
        for (EndPointAnchor anchor : target){
            EndPoint pointTarget = createDotEndPoint(anchor);
            pointTarget.setTarget(true);
            element.addEndPoint(pointTarget);
        }
        element.setTitle(schemeElement.getCaption());
        element.setStyleClass(styleClass);
        model.addElement(element);
        return element;
    }

    /**
     * Добавляет элемент Логическое ветвление в визуальную схему процесса
     * @param x
     * @param y
     * @param errors
     */
    private Element addLogic(String name, int x, int y, EndPointAnchor[] source, EndPointAnchor[]  target, Set<String> errors){
        Logic logic = new Logic(name);
        workflow.addLogic(logic, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(logic, x, y, source, target, "ui-diagram-logic");
        }
        return null;
    }

    /**
     * Добавляет элемент Логическое ветвление в визуальную схему процесса
     * @param x
     * @param y
     * @param errors
     */
    private Element addStart(int x, int y, EndPointAnchor[] source, EndPointAnchor[]  target, Set<String> errors){
        Start start = new Start();
        workflow.addStart(start, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(start, x, y, source, target, "ui-diagram-start");
        }
        return null;
    }

    /**
     * Добавляет элемент "Поручение" в визуальную схему процесса
     * @param executor
     * @param x
     * @param y
     */
    private Element addTask(Staff executor, String taskName, int x, int y, EndPointAnchor[] source, EndPointAnchor[] target, Set<String> errors){
        Task task = new Task(taskName, executor, scheme);
        workflow.addTask(task, scheme, errors);
        if (errors.isEmpty()){
            return modelAddElement(task, x, y, source, target, "ui-diagram-task");
        }
        return null;
    }

    /**
     * Добавляет элемент "Коннектор" в визуальную схему процесса
     * @param from
     * @param to
     * @param errors
     */
    private void addConnector(Element from, Element to, Set<String> errors){
        makeConnector(from, to, errors);
        if (errors.isEmpty()) {
            Connection connection = createConnection(getSourcePoint(from), getTargetPoint(to), null);
            if (connection != null) {
                model.connect(connection);
            }else {
                errors.add("WorkflowIncorrectData");
            }
        }
    }
    private void addConnectorYes(ConditionElement from, Element to, Set<String> errors){
        makeConnector(from, to, errors);
        if (errors.isEmpty()) {
            Connection connection = createConnection(from.getYesPoint(), getTargetPoint(to), EscomMsgUtils.getBandleLabel("Yes"));
            if (connection != null) {
                model.connect(connection);
            }else {
                errors.add("WorkflowIncorrectData");
            }
        }
    }
    private void addConnectorNo(ConditionElement from, Element to, Set<String> errors){
        makeConnector(from, to, errors);
        if (errors.isEmpty()) {
            Connection connection = createConnection(from.getNoPoint(), getTargetPoint(to), EscomMsgUtils.getBandleLabel("No"));
            if (connection != null) {
                model.connect(connection);
            } else {
                errors.add("WorkflowIncorrectData");
            }
        }
    }
    private void makeConnector(Element from, Element to, Set<String> errors){
        Connector connector = new Connector();
        SchemeElement schemeFrom = (SchemeElement)from.getData();
        SchemeElement schemeTo = (SchemeElement)to.getData();
        workflow.addConnector(connector, schemeFrom, schemeTo, scheme, errors);
    }

    /**
     * Добавляет элемент "Условие" в визуальную схему процесса
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private ConditionElement addCondition(String nameCondition, int x, int y, Set<String> errors){
        Condition condition = new Condition(nameCondition);
        workflow.addCondition(condition, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddCondition(condition, x, y);
        }
        return null;
    }

    /**
     * Добавляет элемент "Состояние" в визуальную схему процесса
     * @param typeName
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private Element addState(String caption, String typeName, int x, int y, EndPointAnchor[] source, EndPointAnchor[] target, Set<String> errors){
        State state = new State(caption);
        if (errors.isEmpty()) {
            return modelAddElement(state, x, y, source, target, "ui-diagram-" + typeName + "-state");
        }
        return null;
    }
    
    /**
     * Создание точки приёмника для элемента
     * @param anchor
     * @return 
     */
    private DotEndPoint createDotEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setTarget(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создание точки источника для элемента
     * @param anchor
     * @return 
     */
    private RectangleEndPoint createRectangleEndPoint(EndPointAnchor anchor) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setSource(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создание соединения объектов
     * @param from
     * @param to
     * @param label
     * @return 
     */
    private Connection createConnection(EndPoint from, EndPoint to, String label) {
        if (from == null || to == null) return null;
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));         
        if(label != null) {
            conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));
        }         
        return conn;
    }

    private EndPoint getSourcePoint(Element element){
        for (EndPoint ep : element.getEndPoints()){
            if (ep.isSource()) {
                return ep;
            }
        }
        return null;
    }

    private EndPoint getTargetPoint(Element element){
        for (EndPoint ep : element.getEndPoints()){
            if (ep.isTarget()) {
                return ep;
            }
        }
        return null;
    }

    /**
     * Загрузка визуальной схемы процесса из схемы
     * @param scheme
     */
    public void loadModel(Scheme scheme){
        if (scheme == null){
            this.scheme = createScheme();
        } else {
            this.scheme = scheme;
        }
        visualModelRefresh();
    }

    /**
     * Создание новой схемы процесса
     * @return
     */
    private Scheme createScheme(){
        Scheme scheme = new Scheme();
        model.clear();
        Staff staff = staffFacade.findStaffByUser(getCurrentUser());
        Set<String> errors = new HashSet <>();
        EndPointAnchor[] srcPointsLogic = {EndPointAnchor.RIGHT};
        EndPointAnchor[] trgPointsLogic = {EndPointAnchor.BOTTOM};
        Element start = addStart(2, 4, srcPointsLogic, trgPointsLogic, errors);
        start.setId("start");
        start.setDraggable(false);
        EndPointAnchor[] srcPointsTask = {EndPointAnchor.RIGHT};
        EndPointAnchor[] trgPointsTask = {EndPointAnchor.LEFT};
        Element selfTask = addTask(staff, "Согласовать документ!", 14, 2, srcPointsTask, trgPointsTask, errors);
        addConnector(start, selfTask, errors);
        ConditionElement condition = addCondition("Все одобрили?", 28, 18, errors);
        addConnector(selfTask, condition, errors);
        EndPointAnchor[] srcStateYes = {};
        EndPointAnchor[] trgStateYes = {EndPointAnchor.TOP};
        Element stateYes = addState("Документ согласован", "success", 18, 26, srcStateYes, trgStateYes, errors);
        addConnectorYes(condition, stateYes, errors);
        EndPointAnchor[] srcStateNo = {EndPointAnchor.BOTTOM};
        EndPointAnchor[] trgStateNo = {EndPointAnchor.TOP};
        Element stateNo = addState("Документ не согласован", "fail", 36, 26, srcStateNo, trgStateNo, errors);
        addConnectorNo(condition, stateNo, errors);
        EndPointAnchor[] trgRemedialTask = {EndPointAnchor.BOTTOM};
        EndPointAnchor[] srcRemedialTask = {EndPointAnchor.TOP};
        Element remedialTask = addTask(staff, "Устранить замечания!", 2, 26, srcRemedialTask, trgRemedialTask, errors);
        addConnector(stateNo, remedialTask, errors);
        addConnector(remedialTask, start, errors);
        return scheme;
    }

    /**
     * Обработка события перезагрузки визуальной схемы процесса
     */
    public void onReloadModel(){
        loadModel(getEditedItem().getScheme());
    }
    
    /**
     * Очистка визуальной схемы процесса
     */
    public void clearModel(){
        createScheme();
        visualModelRefresh();
    }

    /**
     * Загрузка визуальной схемы процесса из шаблона
     */
    public void loadModelFromTempl(){
        //ToDo !
        visualModelRefresh();
    }
    
    /**
     * Сохранение модели процесса в шаблон
     */
    public void saveModelToTempl(){
        //ToDo !
    }

    /**
     * Обработка события соединения объектов визуальной модели
     * @param event 
     */
    public void onConnect(ConnectEvent event){
        event.getSourceElement().getData();
        event.getTargetElement().getData();
    }

    public void onDisconnect(DisconnectEvent event) {

    }

    public void onConnectionChange(ConnectionChangeEvent event) {
        //OriginalSource = event.getOriginalSourceElement().getData();
        //NewSource = event.getNewSourceElement().getData();
        //OriginalTarget = event.getOriginalTargetElement().getData();
        //NewTarget = event.getNewTargetElement().getData();
    }

    /**
     * Обработка события добавления в визуальную схему процесса поручений с выбранными в селекторе штатными единицами - исполнителями
     * @param event
     */
    public void onStaffsSelected(SelectEvent event){
        List<Staff> executors = (List<Staff>) event.getObject();        
        if (executors.isEmpty()) return;
        Set<String> errors = new HashSet<>();
        for (Staff executor : executors) {
            Set<String> metodErr = new HashSet<>();
            EndPointAnchor[] srcTask = {EndPointAnchor.RIGHT};
            EndPointAnchor[] trgTask = {EndPointAnchor.LEFT};
            addTask(executor, "Согласовать документ!", defX, defY, srcTask, trgTask, metodErr);
            defX = defX + 5;
            defY = defY + 5;
            if (!metodErr.isEmpty()){
                errors.addAll(metodErr);
            }
        }
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
        }
        onItemChange(); 
        visualModelRefresh();
    }

    /**
     * Перерисовка модели на странице формы
     */
    private void visualModelRefresh(){
        PrimeFaces.current().ajax().update("process:mainTabView:diagramm");
    }
    
    /* GETS & SETS */

    public DiagramModel getModel() {
        return model;
    }
}
