package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.services.workflow.Workflow;
import java.util.HashSet;
import java.util.Iterator;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.diagram.ConnectEvent;
import org.primefaces.event.diagram.ConnectionChangeEvent;
import org.primefaces.event.diagram.DisconnectEvent;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.endpoint.RectangleEndPoint;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
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
        StraightConnector connector = new StraightConnector();
        connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:3}");
        connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
        model.setDefaultConnector(connector);        
        loadModel(getEditedItem().getScheme());
        super.initBean();
    }

    
    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Добавляет элемент "Поручение" в визуальную схему процесса
     * @param executor
     * @param x
     * @param y
     */
    private void addTask(Task task,  int x, int y){
        Element element = new Element(task, x + "em", y + "em");
        EndPoint endPointCA = createRectangleEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointSA = createDotEndPoint(EndPointAnchor.RIGHT);
        endPointCA.setSource(true);
        endPointSA.setTarget(true);
        element.addEndPoint(endPointSA);
        element.addEndPoint(endPointCA);
        model.addElement(element);        
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
        endPoint.setMaxConnections(1);
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
        endPoint.setMaxConnections(1);
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
                addTask(task, x++, y++);
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
