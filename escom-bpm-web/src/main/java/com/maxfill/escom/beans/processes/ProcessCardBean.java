package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StaffFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.staffs.Staff;
import com.maxfill.services.workflow.Workflow;
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

    private final List<EndPoint> endPoints = new ArrayList<>();
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

    @Override
    protected void checkItemBeforeSave(Process item, Set<String> errors) {
        super.checkItemBeforeSave(item, errors);
        workflow.validateScheme(scheme, errors);
    }

    @Override
    protected void onBeforeSaveItem(Process item) {
        workflow.packScheme(scheme);
        getEditedItem().setScheme(scheme);
        super.onBeforeSaveItem(item);
    }

    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Загрузка визуальной схемы процесса
     * @param scheme
     */
    public void loadModel(Scheme scheme){
        if (scheme == null){
            createScheme();
        } else {
            workflow.unpackScheme(scheme);
            this.scheme = scheme;
            restoreModel();
        }
        visualModelRefresh();
    }

    /**
     * Формирует графическую схему из данных модели
     */
    private void restoreModel(){
        model.clear();
        Set<String> errors = new HashSet <>();
        scheme.getStarts().stream().forEach(e-> );
        scheme.getExits().stream().forEach(e->);
        scheme.getConditions().stream().forEach(e->);
        scheme.getStates().stream().forEach(e->);
        scheme.getTasks().stream().forEach(e-> );
        scheme.getLogics().stream().forEach(e-> );
    }

    private List<EndPoint> restoreEndPoints(Set<AnchorElem> anchorElems){
        for (AnchorElem anchorElem : anchorElems){
            if (anchorElem.isSource()) {
                makeSourceEndPoints(EndPointAnchor.valueOf(anchorElem.getPosition()));
            } else {
                makeTargetEndPoints(EndPointAnchor.valueOf(anchorElem.getPosition()));
            }
        }
        return endPoints;
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
        scheme = new Scheme();
        model.clear();
        visualModelRefresh();
    }

    /**
     * Обоработка события удаления текущего визуального компонента со схемы
     * @param element
     */
    public void onDeleteElement(){
        Set<String> errors = new HashSet <>();
        workflow.removeElement((BaseElement)selectedElement.getData(), scheme, errors);
        if (errors.isEmpty()) {
            model.removeElement(selectedElement);
            visualModelRefresh();
        } else {
            EscomMsgUtils.showErrorsMsg(errors);
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
            BaseConnectedElement baseElement = (BaseConnectedElement) selectedElement.getData();
            baseElement.setPosX(Integer.valueOf(x));
            baseElement.setPosY(Integer.valueOf(y));
        }
    }

    /**
     * Обоработка события открытия карточки визуального компонента по двойному клику
     */
    public void onElementOpen(){
        onElementClicked();
        //ToDo
    }

    /**
     * Обработка события добавления визуального компонента "Логическоe ветвление" в схему процесса
     * @param name
     */
    public void onAddLogicElement(String name){
        makeSourceEndPoints(EndPointAnchor.BOTTOM, EndPointAnchor.RIGHT);
        makeTargetEndPoints(EndPointAnchor.TOP, EndPointAnchor.LEFT);
        String caption = EscomMsgUtils.getBandleLabel(name);
        createLogic(caption, defX, defY, endPoints, new HashSet <>());
        visualModelRefresh();
    }

    /**
     * Обработка события добавления визуального компонента "Состояния" в схему процесса
     * @param name
     */
    public void onAddStateElement(String name){
        makeSourceEndPoints(EndPointAnchor.BOTTOM, EndPointAnchor.RIGHT);
        makeTargetEndPoints(EndPointAnchor.TOP, EndPointAnchor.LEFT);
        createState("?", "success", defX, defY, endPoints, new HashSet<>());
        visualModelRefresh();
    }

    /**
     * Обработка события добавления визуального компонента "Условие" в схему процесса
     */
    public void onAddConditionElement(){
        createConditionEndPoints();
        createCondition("?", defX, defY, endPoints, new HashSet<>());
        visualModelRefresh();
    }


    /**
     * Добавление элементов в модель процесса
     * @param schemeElement
     */
    private Element modelAddElement(Element element, String styleClass){
        for (EndPoint endPoint: endPoints){
            element.addEndPoint(endPoint);
        }
        element.setStyleClass(styleClass);
        model.addElement(element);
        this.endPoints.clear();
        return element;
    }

    /**
     * Создание элемента "Условие" в модель процесса
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private ConditionElement createCondition(String nameCondition, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        ConditionElem condition = new ConditionElem(nameCondition, x, y, makeAnchorElems(endPoints));
        workflow.addCondition(condition, scheme, errors);
        if (errors.isEmpty()) {
            ConditionElement element = new ConditionElement(condition, x+ "em", y+ "em");
            modelAddElement(element, condition.getStyle());
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
    private Element createLogic(String name, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        LogicElem logic = new LogicElem(name, x, y, makeAnchorElems(endPoints));
        workflow.addLogic(logic, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(new Element(logic, x + "em", y + "em"), logic.getStyle());
        }
        return null;
    }

    /**
     * Создание элемента "Вход в процесс"
     * @param x
     * @param y
     * @param errors
     */
    private Element createStart(int x, int y, List<EndPoint> endPoints, Set<String> errors){
        StartElem start = new StartElem("", x, y, makeAnchorElems(endPoints));
        workflow.addStart(start, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(new Element(start, x + "em", y + "em"), start.getStyle());
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
        ExitElem exit = new ExitElem("", x, y, makeAnchorElems(endPoints));
        workflow.addExit(exit, scheme, errors);
        if (errors.isEmpty()) {
            return modelAddElement(new Element(exit, x + "em", y + "em"), exit.getStyle());
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
        Task task = new Task(taskName, executor, scheme, x, y, makeAnchorElems(endPoints));
        workflow.addTask(task, scheme, errors);
        if (errors.isEmpty()){
            return modelAddElement(new Element(task, x + "em", y + "em"), task.getStyle());
        }
        return null;
    }

    /**
     * Создание элемента "Состояние"
     * @param typeName
     * @param x
     * @param y
     * @param errors
     * @return
     */
    private Element createState(String caption, String typeName, int x, int y, List<EndPoint> endPoints, Set<String> errors){
        StateElem state = new StateElem(caption, x, y, makeAnchorElems(endPoints));
        if (errors.isEmpty()) {
            return modelAddElement(new Element(state, x + "em", y + "em"), state.getStyle());
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
        ConnectorElem connector = new ConnectorElem("");
        BaseElement schemeFrom = (BaseElement)from.getData();
        BaseElement schemeTo = (BaseElement)to.getData();
        workflow.addConnector(connector, schemeFrom, schemeTo, scheme, errors);
    }
    
    /**
     * Создание точки приёмника для элемента
     * @param anchor
     * @return 
     */
    private DotEndPoint createTargetEndPoint(EndPointAnchor anchor) {
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
    private RectangleEndPoint createSourceEndPoint(EndPointAnchor anchor) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setSource(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создаёт якори для компонента "Условие"
     */
    private void createConditionEndPoints(){
        EndPoint endPointYes = createSourceEndPoint(EndPointAnchor.RIGHT);
        EndPoint endPointNo = createSourceEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointIn = createTargetEndPoint(EndPointAnchor.TOP);
        endPointYes.setSource(true);
        endPointYes.setId("yes");
        endPointNo.setSource(true);
        endPointNo.setId("no");
        endPointIn.setTarget(true);
        endPointIn.setId("in");
        endPointYes.setStyle("{fillStyle:'#099b05'}");
        endPointNo.setStyle("{fillStyle:'#C33730'}");
        endPoints.add(endPointYes);
        endPoints.add(endPointNo);
        endPoints.add(endPointIn);
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
     * Формирует список якорей для элементов маршрута из списка точек визуальных компонент
     * @param endPoints
     * @return
     */
    private Set<AnchorElem> makeAnchorElems(List<EndPoint> endPoints){
        Set<AnchorElem> anchorElems =  new HashSet <>();
        for(EndPoint endPoint : endPoints){
            String position = endPoint.getAnchor().toString();
            anchorElems.add(new AnchorElem(position, endPoint.isSource()));
        }
        return anchorElems;
    }

    /**
     * Создание новой схемы процесса
     * @return
     */
    private void createScheme(){
        scheme = new Scheme();
        model.clear();
        Staff staff = staffFacade.findStaffByUser(getCurrentUser());
        Set<String> errors = new HashSet <>();

        makeSourceEndPoints(EndPointAnchor.RIGHT);
        makeTargetEndPoints(EndPointAnchor.BOTTOM);
        Element start = createStart(2, 4, endPoints, errors);

        makeSourceEndPoints(EndPointAnchor.RIGHT);
        makeTargetEndPoints(EndPointAnchor.LEFT);
        Element selfTask = createTask(staff, "Согласовать документ!", 10, 2, endPoints, errors);
        addConnector(start, selfTask, errors);

        createConditionEndPoints();
        ConditionElement condition = createCondition("Все одобрили?", 28, 18, endPoints, errors);
        addConnector(selfTask, condition, errors);

        makeSourceEndPoints(EndPointAnchor.RIGHT);
        makeTargetEndPoints(EndPointAnchor.TOP);
        Element stateYes = createState("Документ согласован", "success", 36, 26, endPoints, errors);
        addConnectorYes(condition, stateYes, errors);

        makeSourceEndPoints(EndPointAnchor.BOTTOM);
        makeTargetEndPoints(EndPointAnchor.TOP);
        Element stateNo = createState("Документ не согласован", "fail", 18, 26, endPoints, errors);
        addConnectorNo(condition, stateNo, errors);

        makeTargetEndPoints(EndPointAnchor.BOTTOM);
        makeSourceEndPoints(EndPointAnchor.TOP);
        Element remedialTask = createTask(staff, "Устранить замечания!", 2, 26, endPoints, errors);
        addConnector(stateNo, remedialTask, errors);
        addConnector(remedialTask, start, errors);

        makeTargetEndPoints(EndPointAnchor.LEFT);
        Element exit = createExit(50, 35, endPoints, errors);
        addConnector(stateYes, exit, errors);
    }

    private void makeSourceEndPoints(EndPointAnchor ... sources){
        for (EndPointAnchor anchor : sources) {
            endPoints.add(createSourceEndPoint(anchor));
        }
    }
    private void makeTargetEndPoints(EndPointAnchor ... targets){
        for (EndPointAnchor anchor : targets) {
            endPoints.add(createTargetEndPoint(anchor));
        }
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
            makeSourceEndPoints(EndPointAnchor.RIGHT);
            makeTargetEndPoints(EndPointAnchor.LEFT);
            createTask(executor, "Согласовать документ!", defX, defY, endPoints, metodErr);
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

    /* ПРОЧИЕ МЕТОДЫ */

    /**
     * Обработка события после закрытия карточки поручения
     */
    public void onAfterTaskEdit(){
        visualModelRefresh();
    }

    /* GETS & SETS */

    public DiagramModel getModel() {
        return model;
    }
}
