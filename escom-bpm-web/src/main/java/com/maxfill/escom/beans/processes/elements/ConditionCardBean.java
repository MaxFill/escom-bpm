package com.maxfill.escom.beans.processes.elements;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.process.conditions.ConditionFacade;
import com.maxfill.model.basedict.process.conditions.Condition;
import com.maxfill.model.basedict.process.schemes.elements.ConditionElem;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.states.StateFacade;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;

/**
 * Контролер формы "Свойства условия процесса"
 */
@Named
@ViewScoped
public class ConditionCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -5186880746110498838L;
    
    @EJB
    private ConditionFacade conditionFacade;
    @EJB
    private StateFacade stateFacade;
    
    private Condition selected = null;
    private ConditionElem sourceItem = null;
    private ConditionElem editedItem = new ConditionElem();    
    private Staff selectedStaff;
    private State selectedState;
    private List<Staff> concorders;
    private String caption;
    
    private Map<String, Object> params = new HashMap<>();;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){             
            if (sourceBean != null){
                sourceItem = (ConditionElem)((DiagramBean)sourceBean).getBaseElement(); 
                caption = sourceItem.getCaption();
                if (sourceItem.getConditonId() != null){
                    selected = conditionFacade.find(sourceItem.getConditonId());
                    loadParams();                    
                }
            }
            if (sourceItem != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceItem);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public String onSaveAndCloseCard(Object param){        
        try {
            if (selected != null){
                editedItem.setConditonId(selected.getId());
                editedItem.setCaption(caption);
                if (isCanStaffSelect()){
                    params.put("staff", selectedStaff.getId());
                }
                if (isCanStateSelect()){
                    params.put("stateId", selectedState.getId());
                }
                editedItem.setParams(params);
            }
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return onCloseCard(param);
    }       
    
    private void loadParams(){        
        params = sourceItem.getParams();
        if (params.isEmpty()){
            loadParamsFromCondition();            
        }    
        
        if (params.containsKey("staff")){            
            Integer staffId = (Integer) params.get("staff");
            if (staffId != null){
                selectedStaff = staffFacade.find(staffId);
            }
        }
        if (params.containsKey("stateId")){
            Integer stateId = (Integer) params.get("stateId");
            if (stateId != null){
                selectedState = stateFacade.find(stateId);
            }
        }
    }
    
    private void loadParamsFromCondition(){
        params.clear();
        if (selected == null) return;        
        String json = selected.getParamJson();
        if (StringUtils.isBlank(json)) return;
        Gson gson = new Gson();
        params = gson.fromJson(json, Map.class);        
    }
    
    public boolean isCanStaffSelect(){
        return params.containsKey("staff");            
    }
    
    public boolean isCanStateSelect(){
        return params.containsKey("stateId");            
    }
    
    /**
     * Обработка события выбора условия на карточке
     */    
    public void onConditionSelect(){                
        loadParamsFromCondition();
    }    
       
    public void makeCaption(){
        if (selected == null) return;
        StringBuilder sb = new StringBuilder(MsgUtils.getBandleLabel(selected.getName()));
        if (isCanStaffSelect() && selectedStaff != null){
            sb.append(" ").append(selectedStaff.getEmployeeFIO()).append("?");
        }
        if (isCanStateSelect() && selectedState != null){
            sb.append(" ").append(getLabelFromBundle(selectedState.getName())).append("?");
        }
        caption = sb.toString();
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:name");
    } 
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_CONDITION;
    }     

    /* GETS & SETS */

    public State getSelectedState() {
        return selectedState;
    }
    public void setSelectedState(State selectedState) {
        this.selectedState = selectedState;
    }
    
    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
        
    public Condition getSelected() {
        return selected;
    }
    public void setSelected(Condition selected) {
        this.selected = selected;
    }

    public ConditionElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(ConditionElem editedItem) {
        this.editedItem = editedItem;
    }

    public List<Staff> getConcorders() {
        if (concorders == null){
            concorders = ((DiagramBean)sourceBean).getProcess().getScheme().getTasks().stream()
                    .filter(t->t.getOwner() != null)
                    .map(t->t.getOwner()).collect(Collectors.toList());            
        }
        return concorders;
    }
    public void setConcorders(List<Staff> concorders) {
        this.concorders = concorders;
    }
    
    public Staff getSelectedStaff() {
        return selectedStaff;
    }
    public void setSelectedStaff(Staff selectedStaff) {
        this.selectedStaff = selectedStaff;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Condition");
    }
}
