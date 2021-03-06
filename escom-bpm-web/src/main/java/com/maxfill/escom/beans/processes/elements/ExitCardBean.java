package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.model.basedict.docStatuses.StatusesDocFacade;
import com.maxfill.model.basedict.process.schemes.elements.ExitElem;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.states.StateFacade;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Свойства элемента процесса "Выход"
 */
@Named
@ViewScoped
public class ExitCardBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 5642232515643258977L;
    
    @EJB
    private StateFacade stateFacade;
    @EJB
    private StatusesDocFacade statuseFacade;
    
    @Inject
    private ProcessBean processBean;
            
    private ExitElem editedItem = new ExitElem();
    private ExitElem sourceItem;
    private String style = "completed";
    private State seletedState;    
    private StatusesDoc selectedStatus;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){            
            if (sourceBean != null){
                sourceItem = (ExitElem)((DiagramBean)sourceBean).getBaseElement();
                if (sourceItem.getStatusId() != null){
                    selectedStatus = statuseFacade.find(sourceItem.getStatusId());
                }
            }
            if (sourceItem != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceItem);
                    if (editedItem.getFinishStateId() != null){
                        seletedState = stateFacade.find(editedItem.getFinishStateId());
                    }
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            style = sourceItem.getStyleType();
        }
    }
        
    public String onSaveAndCloseCard(Object param){
        try {
            if (seletedState != null){
                editedItem.setFinishStateId(seletedState.getId());
            } 
            if (selectedStatus != null){
                editedItem.setStatusId(selectedStatus.getId());
            } else {
                editedItem.setStatusId(null);
            }
            editedItem.setStyleType(style);
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return onCloseCard(param);
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_EXIT;
    } 

    public List<State> getProcessStates(){
        return processBean.getAvailableStates();
    }
    
    public ExitElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(ExitElem editedItem) {
        this.editedItem = editedItem;
    }

    public State getSeletedState() {
        return seletedState;
    }
    public void setSeletedState(State seletedState) {
        this.seletedState = seletedState;
    }

    public StatusesDoc getSelectedStatus() {
        return selectedStatus;
    }
    public void setSelectedStatus(StatusesDoc selectedStatus) {
        this.selectedStatus = selectedStatus;
    }
    
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }
          
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ElementExit");
    }
}
