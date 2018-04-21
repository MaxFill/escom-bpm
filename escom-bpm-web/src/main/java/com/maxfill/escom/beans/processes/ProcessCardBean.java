package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
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
        StraightConnector connector = new StraightConnector();
        connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:3}");
        connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
        model.setDefaultConnector(connector);
        super.initBean();
    }

    /* МЕТОДЫ РАБОТЫ С МОДЕЛЬЮ */

    /**
     * Добавляет элемент "Поручение" в модель
     * @param executor
     * @param x
     * @param y
     */
    private void addTaskElement(Staff executor, int x, int y){
        Element element = new Element(new Task("Task", executor, getEditedItem()), x + "em", y + "em");
        EndPoint endPointCA = createRectangleEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointSA = createDotEndPoint(EndPointAnchor.RIGHT);
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
