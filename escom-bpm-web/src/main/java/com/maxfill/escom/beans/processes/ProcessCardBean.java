package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
import java.util.Iterator;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.diagram.ConnectEvent;
import org.primefaces.event.diagram.ConnectionChangeEvent;
import org.primefaces.event.diagram.DisconnectEvent;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.*;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Контролер формы "Карточка процесса"
 */
@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process>{
    @EJB
    private ProcessFacade processFacade;

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
     * Определяет возможность отображения facet в элементе модели для строковых объектов
     * @param el
     * @return
     */
    public boolean canShowElFacet(Object el){
        return el != null && el instanceof String;
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
     */
    public void onAddLogicElement(String logic){
        //addElement("logicId", 10, 8, logic, "ui-diagram-logic-element");
        Element element = new Element(logic, "10em", "8em");
        element.setId("logicId");
        element.setStyleClass("ui-diagram-logic-element");

        EndPoint endPointBT = createRectangleEndPoint(EndPointAnchor.CONTINUOUS_BOTTOM, 1);
        EndPoint endPointRG = createRectangleEndPoint(EndPointAnchor.CONTINUOUS_RIGHT, 1);
        EndPoint endPointLF = createDotEndPoint(EndPointAnchor.CONTINUOUS_TOP, 1);
        EndPoint endPointTP = createDotEndPoint(EndPointAnchor.CONTINUOUS_LEFT, 1);
        endPointRG.setSource(true);
        endPointBT.setSource(true);
        endPointTP.setTarget(true);
        endPointLF.setTarget(true);
        element.addEndPoint(endPointLF);
        element.addEndPoint(endPointTP);
        element.addEndPoint(endPointRG);
        element.addEndPoint(endPointBT);
        modelAddElement(element);
    }

    /**
     * Обработка события добавления Условия в визуальную модель процесса
     */
    public void onAddConditionElement(){
        //addElement("condId", 12, 10, "", "d16");
        Element element = new Element("If", "10em", "8em");
        element.setId("conditionId");
        EndPoint endPointYes = createRectangleEndPoint(EndPointAnchor.LEFT, 1);
        EndPoint endPointNo = createRectangleEndPoint(EndPointAnchor.RIGHT, 1);
        EndPoint endPointIn = createDotEndPoint(EndPointAnchor.TOP, 1);
        endPointYes.setSource(true);
        endPointNo.setSource(true);
        endPointIn.setTarget(true);
        endPointYes.setStyle("{fillStyle:'#099b05'}");
        endPointNo.setStyle("{fillStyle:'#C33730'}");
        element.addEndPoint(endPointYes);
        element.addEndPoint(endPointNo);
        element.addEndPoint(endPointIn);
        element.setStyleClass("d16");
        modelAddElement(element);
    }

    /**
     * Обработка события добавления Состояния в визуальную модель процесса
     */
    public void onAddStateElement(String name){
        Element element = new Element(null, "16em", "12em");
        element.setId("stateId");
        EndPoint endPointCA = createRectangleEndPoint(EndPointAnchor.RIGHT, 1);
        EndPoint endPointSA = createDotEndPoint(EndPointAnchor.LEFT, 1);
        endPointCA.setSource(true);
        endPointSA.setTarget(true);
        element.addEndPoint(endPointSA);
        element.addEndPoint(endPointCA);
        element.setStyleClass("ui-diagram-" + name + "-state");
        element.setTitle(name);
        modelAddElement(element);
    }

    private void modelAddElement(Element el){
        model.addElement(el);
        modelRefresh();
    }

    /**
     * Добавляет элемент "Поручение" в модель
     * @param executor
     * @param x
     * @param y
     */
    private void addTaskElement(Staff executor, int x, int y){
        Element element = new Element(new Task("Task", executor, getEditedItem()), x + "em", y + "em");
        EndPoint endPointCA = createRectangleEndPoint(EndPointAnchor.LEFT, 1);
        EndPoint endPointSA = createDotEndPoint(EndPointAnchor.RIGHT, 1);
        endPointCA.setSource(true);
        endPointSA.setTarget(true);
        element.addEndPoint(endPointSA);
        element.addEndPoint(endPointCA);
        model.addElement(element);        
    }

    /**
     * Обоработка события удаления элемента со схемы
     */
    public void onDeleteElement(Element element){
        model.removeElement(element);
        modelRefresh();
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
        modelRefresh();
    }
    
    /**
     * Создание точки приёмника для поручения
     * @param anchor
     * @return 
     */
    private EndPoint createDotEndPoint(EndPointAnchor anchor, int maxConnections) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setTarget(true);
        endPoint.setMaxConnections(maxConnections);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создание точки источника
     * @param anchor
     * @return
     */
    private BlankEndPoint createBlankSourcePoint(EndPointAnchor anchor) {
        BlankEndPoint endPoint = new BlankEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setSource(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Создание точки источника для поручения
     * @param anchor
     * @return 
     */
    private EndPoint createRectangleEndPoint(EndPointAnchor anchor, int maxConnections) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setMaxConnections(maxConnections);
        endPoint.setSource(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Загрузка модели из шаблона
     */
    public void loadModel(){
        //ToDo !
        modelRefresh();
    }

    /**
     * Перезагрузка модели из ранее выбранного шаблона
     */
    public void reloadModel(){
        //ToDo !
        modelRefresh();
    }

    /**
     * Очистка модели
     */
    public void clearModel(){
        model.clear();
        modelRefresh();
    }

    /**
     * Сохранение модели в шаблон
     */
    public void saveModel(){
        //ToDo !
    }

    public void onConnect(ConnectEvent event){

    }
    public void onDisconnect(DisconnectEvent event) {

    }
    public void onConnectionChange(ConnectionChangeEvent event) {

    }

    /**
     * Обработка события добавления в модель штатных единиц - исполнителей 
     * @param event
     */
    public void onStaffsSelected(SelectEvent event){
        List<Staff> executors = (List<Staff>) event.getObject();        
        if (executors.isEmpty()) return;
        int x = 8;
        int y = 6;
        for (Staff executor : executors) {
            addTaskElement(executor, x++, y++);        
        }
        onItemChange(); 
        modelRefresh();
    }

    /**
     * Перерисовка модели на странице формы
     */
    private void modelRefresh(){
        PrimeFaces.current().ajax().update("process:mainTabView:diagramm");
    }
    
    /* GETS & SETS */

    public DiagramModel getModel() {
        return model;
    }
}
