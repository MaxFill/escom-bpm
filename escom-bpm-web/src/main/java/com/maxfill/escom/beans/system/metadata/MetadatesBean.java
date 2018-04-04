package com.maxfill.escom.beans.system.metadata;

import com.maxfill.RightsDef;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.MetadatesFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.states.State;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.system.rights.RightsBean;
import com.maxfill.facade.StateFacade;
import com.maxfill.utils.Tuple;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.StringUtils;

/* Контроллер формы обозревателя объектов */
@ViewScoped
@Named
public class MetadatesBean implements Serializable{
    private static final long serialVersionUID = -3106225231045015183L;
    
    private Metadates selectedObject;
    private List<Metadates> allItems;
    private List<State> states;
    private LayoutOptions layoutOptions;
    private Right selRight;
    private Integer editMode;
    private State stateAdd;
    private State startState;
    
    @Inject
    private SessionBean sessionBean;
    
    @Inject
    private RightsBean rightsBean;
    
    @EJB
    private MetadatesFacade metadatesFacade;
    @EJB
    private RightFacade rightFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private RightsDef rightsDef;

    @PostConstruct
    public void init() {
        initLayoutOptions();
    }  
    
    /* ПРАВА ДОСТУПА */

    /* Открытие карточки для создание нового права к объекту  */
    public void onAddRight(State state) {
        editMode = DictEditMode.INSERT_MODE;
        selRight = new Right();
        Map<String, String> params = new HashMap <>();
        params.put("showCreate", "true");
        sessionBean.openRightCard(DictEditMode.INSERT_MODE, state, "", params);
    }
    
    /* Открытие карточки для редактирования права объекта  */
    public void onEditRight(Right right) {
        selRight = right;
        Integer hashCode = selRight.hashCode();
        String keyRight = hashCode.toString();
        editMode = DictEditMode.EDIT_MODE;
        sessionBean.addSourceRight(keyRight, selRight);
        Map<String, String> params = new HashMap <>();
        params.put("showCreate", "true");
        sessionBean.openRightCard(DictEditMode.EDIT_MODE, selRight.getState(), keyRight, params);
    }

    /**
     * Обработка события закрытия карточки редактирования права
     * @param event
     */
    public void onCloseRightCard(SelectEvent event) {
        Tuple<Boolean, Right> tuple = (Tuple)event.getObject();
        Boolean isChange = tuple.a;
        if (isChange) {
            switch (editMode){
                case DictEditMode.EDIT_MODE: { 
                    rightFacade.edit(selRight);
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    selRight = tuple.b;
                    selectedObject.getRightList().add(selRight);
                    rightFacade.create(selRight);
                    break;
                }
            }
            rightsDef.reloadDefaultRight(selectedObject);
        }                
    }
    
    /* Возвращает список прав текущего объекта в заданном состоянии  */     
    public List<Right> getRightsInState(State state) {
        if (selectedObject != null){
            List<Right> rights = rightFacade.findDefaultRightState(selectedObject, state);
            rightsBean.prepareRightsForView(rights);
            return rights;
        }
        return null;
    }    
    
    /* Удаление записи права из базы данных через */ 
    public void onDeleteRight(Right right){        
        rightFacade.remove(right);  
    }
    
    public void onAddState(){
        if (selectedObject == null) return;
        selectedObject.getStatesList().add(stateAdd);
        metadatesFacade.edit(selectedObject);
        Right right = new Right(DictRights.TYPE_GROUP, DictRights.GROUP_ADMIN_ID, "", stateAdd, selectedObject);
        rightFacade.create(right);
        right = new Right(DictRights.TYPE_GROUP, DictRights.GROUP_ALL_USER_ID, "", stateAdd, selectedObject);
        right.setAddChild(false);
        right.setChangeRight(false);
        right.setExecute(false);
        right.setDelete(false);
        rightFacade.create(right);
        EscomMsgUtils.succesMsg("StateIsAdd");
    }

    /**
     * Удаление состояния у объекта
     * @param state
     */
    public void onDeleteState(State state){
        Set<String> errors = new HashSet<>();
        if (Objects.equals(state, startState)){
            errors.add("CantDeleteStartState");
        }
        BaseExplBean bean = sessionBean.getItemBeanByClassName(selectedObject.getObjectName());
        if (bean != null && bean.getItemFacade().countItemsByState(state) > 0){
            errors.add("CantRemoveUsedState");
        }
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return;
        }
        selectedObject.getStatesList().remove(state);   //удалили состояние
        metadatesFacade.edit(selectedObject);           //сохранили изменения в объекте
        List<Right> rights = rightFacade.findDefaultRightState(selectedObject, state);  //получили права, которые надо удалить (связанные с состоянием)
        rights.stream().forEach(right -> rightFacade.remove(right));    //удалили права
        rightsDef.reloadDefaultRight(selectedObject);
    }

    /**
     * Установка начального состояния объекта
     * @param state
     */
    public void onSetStartState(State state){
        startState = state;
        selectedObject.setStateForNewObj(startState);
        metadatesFacade.edit(selectedObject);
        EscomMsgUtils.succesMsg("StartSateIsChange");
    }
    
    /* СЛУЖЕБНЫЕ МЕТОДЫ  */
    
    /**
     * Инициализация областей формы обозревателя объектов
     */
    private void initLayoutOptions() {
        layoutOptions = new LayoutOptions();

        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        layoutOptions.setPanesOptions(panes);

        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 38);
        layoutOptions.setSouthOptions(south);

        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 270);
        west.addOption("minSize", 150);
        west.addOption("maxSize", 450);
        west.addOption("resizable", true);
        layoutOptions.setWestOptions(west);

        LayoutOptions east = new LayoutOptions();
        east.addOption("size", 400);
        east.addOption("minSize", 0);
        east.addOption("maxSize", 300);
        layoutOptions.setEastOptions(east);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("minWidth", 200);
        center.addOption("minHeight", 100);
        layoutOptions.setCenterOptions(center);
        
    }
    
    /**
     * Событие выбора текущего объекта в дереве объектов
     * @param event 
     */
    public void onSelectedItem(SelectEvent event){
        if (event.getObject() == null) return;
        selectedObject = ((Metadates) event.getObject());
        states = null;
    } 
      
    public String getBundleName(Metadates metadate){
        if (metadate == null || StringUtils.isBlank(metadate.getBundleName())) return null;
        return EscomMsgUtils.getBandleLabel(metadate.getBundleName());
    }

    /* *** GET & SET *** */

    public List <State> getStates() {
        if (selectedObject != null || states == null){
            states = stateFacade.findAll();
            states.removeAll(selectedObject.getStatesList());
        }
        return states;
    }

    public State getStartState() {
        if (selectedObject == null) return null;
        startState = selectedObject.getStateForNewObj();
        return startState;
    }
    public void setStartState(State startState) {
        this.startState = startState;
    }

    public void setStateAdd(State stateAdd) {
        this.stateAdd = stateAdd;
    }
    public State getStateAdd() {
        return stateAdd;
    }
  
    public Metadates getSelectedObject() {
        return selectedObject;
    }
    public void setSelectedObject(Metadates selectedObject) {
        this.selectedObject = selectedObject;
    }
        
    public List<Metadates> getAllItems() {
        if (allItems == null){
            allItems = getItemFacade().findAll();
        }
        return allItems;
    }

    public Right getSelRight() {
        return selRight;
    }
    public void setSelRight(Right selRight) {
        this.selRight = selRight;
    }
        
    public MetadatesFacade getItemFacade(){
        return metadatesFacade;
    }   

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }
     
}