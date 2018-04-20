package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
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
    private static final Logger LOGGER = Logger.getLogger(ProcessCardBean.class.getName());

    @EJB
    private ProcessFacade processFacade;

    private Staff selectedStaff;

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
     * Добавляет элемент в модель
     */
    public void addTaskElement(){
        Element element = new Element(new Task("Task", selectedStaff, getEditedItem()), "10em", "6em");
        EndPoint endPointCA = createRectangleEndPoint(EndPointAnchor.LEFT);
        EndPoint endPointSA = createDotEndPoint(EndPointAnchor.RIGHT);
        endPointCA.setSource(true);
        endPointSA.setTarget(true);
        element.addEndPoint(endPointSA);
        element.addEndPoint(endPointCA);
        model.addElement(element);
        PrimeFaces.current().ajax().update("process:mainTabView:diagramm");
    }

    private EndPoint createDotEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setTarget(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    private EndPoint createRectangleEndPoint(EndPointAnchor anchor) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setScope("network");
        endPoint.setSource(true);
        endPoint.setStyle("{fillStyle:'#98AFC7'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
     * Загрузка модели из шаблона
     */
    public void loadModel(){

    }

    /**
     * Перезагрузка модели из ранее выбранного шаблона
     */
    public void reloadModel(){

    }

    /**
     * Очистка модели
     */
    public void clearModel(){

    }

    /**
     * Сохранение модели в шаблон
     */
    public void saveModel(){

    }

    public void onElementClicked(){
        LOGGER.info(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("elementId"));
    }

    public void onConnect(ConnectEvent event){

    }
    public void onDisconnect(DisconnectEvent event) {

    }
    public void onConnectionChange(ConnectionChangeEvent event) {

    }


    /**
     * Обработка события выбора штатной единицы
     * @param event
     */
    public void onStaffSelected(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()) return;
        selectedStaff = items.get(0);
        onItemChange();
        addTaskElement();
    }
    public void onStaffSelected(ValueChangeEvent event){
        selectedStaff = (Staff) event.getNewValue();
        onItemChange();
        addTaskElement();
    }

    /* GETS & SETS */

    public Staff getSelectedStaff() {
        return selectedStaff;
    }
    public void setSelectedStaff(Staff selectedStaff) {
        this.selectedStaff = selectedStaff;
    }

    public DiagramModel getModel() {
        return model;
    }
}
