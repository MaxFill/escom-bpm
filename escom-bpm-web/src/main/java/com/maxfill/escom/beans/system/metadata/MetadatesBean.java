package com.maxfill.escom.beans.system.metadata;

import com.maxfill.RightsDef;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.metadates.MetadatesFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.RightFacade;
import com.maxfill.model.states.State;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.system.rights.RightsBean;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DualListModel;

/* Контроллер формы обозревателя настройки объектов */
@ViewScoped
@Named
public class MetadatesBean implements Serializable{
    private static final long serialVersionUID = -3106225231045015183L;
    
    private Metadates selectedObject;
    private List<Metadates> allItems;

    private List<Right> rights = null;
    private List<State> objectStates;
    private LayoutOptions layoutOptions;

    private State stateAdd;
    private State startState;

    private Integer typeAddRight = DictRights.TYPE_GROUP;
    private User selUser;
    private State selState;
    private UserGroups selUsGroup;
    private UserGroups selUserRole;

    private DualListModel<State> states;

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

    /**
     * Обработка события изменения права
     * @param event
     */
    public void onRightChange(RowEditEvent event){
        Right right = (Right) event.getObject();
        rightFacade.edit(right);
    }

    /**
     * Добавление права доступа
     * @param state
     */
    public void onAddRight(){
        Set<String> errors = new HashSet <>();
        BaseDict obj = null;
        switch(typeAddRight){
            case DictRights.TYPE_GROUP :{
                if (selUsGroup == null){
                    errors.add(MsgUtils.getMessageLabel("UserGroupNotSet"));
                } else {
                    obj = selUsGroup;
                }
                break;
            }
            case DictRights.TYPE_USER :{
                if (selUser == null){
                    errors.add(MsgUtils.getMessageLabel("UserNotSet"));
                } else {
                    obj = selUser;
                }
                break;
            }
            case DictRights.TYPE_ROLE :{
                if (selUserRole == null){
                    errors.add(MsgUtils.getMessageLabel("RoleNotSet"));
                } else {
                    obj = selUserRole;
                }
                break;
            }
        }
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        } else {
            Right right = rightsBean.createRight(typeAddRight, obj.getId(), obj.getName(), selState, selectedObject);
            rights.add(right);
        }
    }

    /* Удаление записи права из базы данных  */
    public void onDeleteRight(Right right){        
        rightFacade.remove(right);
        rights.remove(right);
    }

    /**
     * Проверка возможности удалить состояние у объекта
     * @param state
     */
    private void checkStateBeforeDelete(State state,  Set<String> errors){
        if (Objects.equals(state, startState)){
            errors.add(MsgUtils.getMessageLabel("CantDeleteStartState"));
        }
        BaseTableBean bean = sessionBean.getItemBeanByClassName(selectedObject.getObjectName());
        if (bean != null && bean.getFacade().countItemsByState(state) > 0){
            String message = MessageFormat.format(MsgUtils.getMessageLabel("CantRemoveUsedState"), new Object[]{state.getName()});
            errors.add(message);
        }
    }

    /**
     * Удаление прав состояния
     * @param state
     */
    private void deleteStateRigts(State state){
        List<Right> rights = rightFacade.findDefaultRightState(selectedObject, state);  //получили права, которые надо удалить (связанные с состоянием)
        rights.stream().forEach(right -> rightFacade.remove(right));    //удалили права
    }

    /**
     * Обработка события изменения списка состояний объекта в pickList
     */
    public void onTransfer(TransferEvent event){
        List<State> newStates = states.getTarget();
        objectStates = newStates;
    }

    /**
     * Сохранение изменений в объекте
     */
    public void onSaveChange(){
        List<State> oldStates = selectedObject.getStatesList();
        List<State> newStates = states.getTarget();
        oldStates.removeAll(newStates);     //определяем, какие состояния нужно удалить
        Set<String> errors = new HashSet<>();
        if (!newStates.contains(startState)){
            String message = MessageFormat.format(MsgUtils.getMessageLabel("StartStateNotPresentinListsStates"), new Object[]{startState.getName()});
            errors.add(message);
        } else {
            selectedObject.setStateForNewObj(startState);
        }
        if (!oldStates.isEmpty()){
            oldStates.stream().forEach(state->checkStateBeforeDelete(state, errors));
        }
        if (!errors.isEmpty()){
            MsgUtils.showErrors(errors);
            return;
        }
        oldStates.stream().forEach(state->deleteStateRigts(state));
        selectedObject.setStatesList(states.getTarget());
        metadatesFacade.edit(selectedObject);
        rightsDef.reloadDefaultRight(selectedObject);
        rights = null;
        MsgUtils.succesFormatMsg("DataIsSaved", new Object[]{getBundleName(selectedObject)});
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
        rights = null;
        objectStates =  selectedObject.getStatesList();
        startState = selectedObject.getStateForNewObj();
    } 
      
    public String getBundleName(Metadates metadate){
        if (metadate == null || StringUtils.isBlank(metadate.getBundleName())) return null;
        return MsgUtils.getBandleLabel(metadate.getBundleName());
    }

    /* *** GETS & SETS *** */

    public List <State> getObjectStates() {
        return objectStates;
    }
    public void setObjectStates(List <State> objectStates) {
        this.objectStates = objectStates;
    }

    public DualListModel<State> getStates() {
        if (selectedObject != null && states == null){
            List<State> allStates = stateFacade.findAll();
            List<State> objectStates = selectedObject.getStatesList();
            allStates.removeAll(objectStates);
            states = new DualListModel<>(allStates, objectStates);
        }
        return states;
    }
    public void setStates(DualListModel <State> states) {
        this.states = states;
    }

    public List <Right> getRights() {
        if (selectedObject != null && rights == null){
            rights = rightFacade.findDefaultRight(selectedObject);
            rightsBean.prepareRightsForView(rights);
        }
        return rights;
    }
    public void setRights(List <Right> rights) {
        this.rights = rights;
    }

    public State getStartState() {
        return startState;
    }
    public void setStartState(State startState) {
        this.startState = startState;
    }

    public Integer getTypeAddRight() {
        return typeAddRight;
    }
    public void setTypeAddRight(Integer typeAddRight) {
        this.typeAddRight = typeAddRight;
    }

    public State getSelState() {
        return selState;
    }
    public void setSelState(State selState) {
        this.selState = selState;
    }

    public User getSelUser() {
        return selUser;
    }
    public void setSelUser(User selUser) {
        this.selUser = selUser;
    }

    public UserGroups getSelUsGroup() {
        return selUsGroup;
    }
    public void setSelUsGroup(UserGroups selUsGroup) {
        this.selUsGroup = selUsGroup;
    }

    public UserGroups getSelUserRole() {
        return selUserRole;
    }
    public void setSelUserRole(UserGroups selUserRole) {
        this.selUserRole = selUserRole;
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
        
    public MetadatesFacade getItemFacade(){
        return metadatesFacade;
    }   

    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }
     
}