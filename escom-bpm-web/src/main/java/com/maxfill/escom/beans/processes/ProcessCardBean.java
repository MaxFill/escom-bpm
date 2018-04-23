package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.Condition;
import com.maxfill.model.process.schemes.elements.Logic;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.process.schemes.elements.State;
import com.maxfill.services.workflow.Workflow;
import java.util.HashSet;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    private Scheme scheme = new Scheme();
    
    private final DefaultDiagramModel model = new DefaultDiagramModel();

    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }

    @Override
    protected void initBean() {
        model.setMaxConnections(-1);
        model.getDefaultConnectionOverlays().add(new ArrowOverlay(20, 20, 1, 1));
        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:2}");
        connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
        connector.setCornerRadius(10);
        model.setDefaultConnector(connector);
        super.initBean();
    }
   
    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Определяет возможность отображения facet в элементе модели Поручение панели с кнопкой
     * @param el
     * @return
     */
    public boolean canShowTaskFacet(Object el){
        return el != null && el instanceof Element && ((Element)el).getData() instanceof Task;
    }

    /**
     * Обработка события выделения мышью элемента на визуальной схеме процесса
     * используется для получения и сохранения координат элементов
     */
    public void onElementClicked() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String paramId = params.get("elementId");
        String x = params.get("posX");
        String y = params.get("posY");
        String id = paramId.substring(paramId.indexOf("-") + 1);
        Element el = model.findElement(id);
        el.setX(x + "em");
        el.setY(y + "em");
    }

    /**
     * Обработка события добавления Логического элемента в визуальную модель процесса
     * @param name
     */
    public void onAddLogicElement(String name){
        Logic logic = new Logic(name);
        Element element = new Element(logic, "10em", "8em");
        modelAddElement(element, "ui-diagram-logic");
    }

    /**
     * Обработка события добавления Условия в визуальную модель процесса
     */
    public void onAddConditionElement(){
        Condition condition = new Condition();
        Set<String> metodErr = new HashSet<>();
        workflow.addCondition(condition, scheme, metodErr);
        Element element = new Element(condition, "10em", "8em");
        EndPoint endPointYes = createRectangleEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointNo = createRectangleEndPoint(EndPointAnchor.RIGHT);
        EndPoint endPointIn = createDotEndPoint(EndPointAnchor.TOP);
        endPointYes.setSource(true);
        endPointNo.setSource(true);
        endPointIn.setTarget(true);
        endPointYes.setStyle("{fillStyle:'#099b05'}");
        endPointNo.setStyle("{fillStyle:'#C33730'}");
        element.addEndPoint(endPointYes);
        element.addEndPoint(endPointNo);
        element.addEndPoint(endPointIn);        
        element.setStyleClass("d16");
        model.addElement(element);
        visualModelRefresh();
    }

    /**
     * Обработка события добавления Состояния в визуальную модель процесса
     * @param name
     */
    public void onAddStateElement(String name){
        State state = new State();
        Element element = new Element(state, "16em", "12em");
        modelAddElement(element, "ui-diagram-" + name + "-state");
    }

    /**
     * Добавляет элемент в визуальную схему процесса
     * @param element 
     */
    private void modelAddElement(Element element, String styleClass){
        EndPoint endPointBT = createRectangleEndPoint(EndPointAnchor.BOTTOM);
        EndPoint endPointRG = createRectangleEndPoint(EndPointAnchor.RIGHT);
        EndPoint endPointLF = createDotEndPoint(EndPointAnchor.TOP);
        EndPoint endPointTP = createDotEndPoint(EndPointAnchor.LEFT);
        endPointRG.setSource(true);
        endPointBT.setSource(true);
        endPointTP.setTarget(true);
        endPointLF.setTarget(true);
        element.addEndPoint(endPointLF);
        element.addEndPoint(endPointTP);
        element.addEndPoint(endPointRG);
        element.addEndPoint(endPointBT);
        element.setStyleClass(styleClass);
        model.addElement(element);
        visualModelRefresh();
    }

    /**
     * Добавляет элемент "Поручение" в визуальную схему процесса
     * @param executor
     * @param x
     * @param y
     */
    private void addElementTask(Task task,  int x, int y){
        Element element = new Element(task, x + "em", y + "em");
        modelAddElement(element, "ui-diagram-task");        
    }

    /**
     * Добавляет элемент "Коннектор" в визуальную схему процесса
     */
    private void addConnector(EndPoint from, EndPoint to){
        model.connect(createConnection(from, to, null));
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
     * Обоработка события открытия карточки элемента
     * @param element
     */
    public void onOpenElement(Element element){
        
    }
    
    /**
     * Обработка события после закрытия карточки поручения
     */
    public void afterTaskEdit(){
        visualModelRefresh();
    }
    
    /**
     * Создание точки приёмника для поручения
     * @param anchor
     * @return 
     */
    private EndPoint createDotEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setTarget(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создание точки источника для поручения
     * @param anchor
     * @return 
     */
    private EndPoint createRectangleEndPoint(EndPointAnchor anchor) {
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
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));         
        if(label != null) {
            conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));
        }         
        return conn;
    }
       
    /**
     * Загрузка визуальной схемы процесса из схемы
     * @param scheme
     */
    public void loadModel(Scheme scheme){
        if (scheme == null) return;        
        this.scheme = scheme; 
        //ToDo !
        visualModelRefresh();
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
        model.clear();
        scheme = new Scheme();
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
        int x = 8;
        int y = 6;
        Set<String> errors = new HashSet<>();
        for (Staff executor : executors) {
            Task task = new Task("Task", executor, scheme);
            Set<String> metodErr = new HashSet<>();
            workflow.addTask(task, scheme, metodErr);
            if (metodErr.isEmpty()){
                addElementTask(task, x++, y++);
            } else {
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
