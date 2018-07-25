package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.process.schemes.elements.StatusElem;
import com.maxfill.model.states.State;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.statuses.StatusesDoc;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Карточка элемента процесса "Статус документа""
 */
@Named
@ViewScoped
public class DocStatusCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -5286296381383874923L;

    @EJB
    private StatusesDocFacade statuseFacade;
    @EJB
    private StateFacade stateFacade;
    
    private StatusesDoc selected = null;
    private State selectedState = null;
    private final StatusElem editedItem = new StatusElem();
    private StatusElem sourceItem;
    private String style = "success";
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){            
            if (sourceBean != null){
                sourceItem = (StatusElem)((ProcessCardBean)sourceBean).getBaseElement(); 
                if (sourceItem.getDocStatusId() != null){
                    selected = statuseFacade.find(sourceItem.getDocStatusId());
                }
                if (sourceItem.getDocStateId() != null){
                    selectedState = stateFacade.find(sourceItem.getDocStateId());
                }
            }
            if (sourceItem != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceItem);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            } 
            style = sourceItem.getStyleType();
        }
    }
    
    public String onSaveAndClose(Object param){
         try {
            editedItem.setDocStatusId(selected.getId());
            editedItem.setDocStateId(selectedState.getId());
            editedItem.setCaption(selected.getBundleName());
            editedItem.setStyleType(style);
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return onCloseCard(param);
    }    

    /* GETS & SETS */
    
    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_STATE;
    }

    public StatusesDoc getSelected() {
        return selected;
    }
    public void setSelected(StatusesDoc selected) {
        this.selected = selected;
    }

    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }

    public State getSelectedState() {
        return selectedState;
    }
    public void setSelectedState(State selectedState) {
        this.selectedState = selectedState;
    }

      
}
