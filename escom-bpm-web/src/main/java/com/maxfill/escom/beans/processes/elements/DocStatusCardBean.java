package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.process.schemes.elements.StatusElem;
import com.maxfill.model.states.State;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.statuses.StatusesDoc;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
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
                sourceItem = (StatusElem)((DiagramBean)sourceBean).getBaseElement(); 
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

    @Override
    public String onCloseCard(Object param){
        try {
            if (selected != null){
                editedItem.setDocStatusId(selected.getId());
                editedItem.setCaption(selected.getBundleName());
            }
            if (selectedState != null){
                editedItem.setDocStateId(selectedState.getId());
            }
            editedItem.setStyleType(style);
            BeanUtils.copyProperties(sourceItem, editedItem);            
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    /* GETS & SETS */
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_DOC_STATUS;
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

    public StatusElem getEditedItem() {
        return editedItem;
    }
        
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("SetDocStatus");
    }
      
}
