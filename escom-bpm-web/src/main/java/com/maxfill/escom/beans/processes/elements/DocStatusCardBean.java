package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.process.schemes.elements.StatusElem;
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
    
    private StatusesDoc selected = null;
    private final StatusElem editedItem = new StatusElem();
    private StatusElem sourceItem;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){            
            if (sourceBean != null){
                sourceItem = (StatusElem)((ProcessCardBean)sourceBean).getBaseElement(); 
                
                if (sourceItem.getDocStatusId() != null){
                    selected = statuseFacade.find(sourceItem.getDocStatusId());
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
    
    @Override
    public String onCloseCard(Object param){
        try {
            editedItem.setDocStatusId(selected.getId());
            editedItem.setCaption(selected.getBundleName());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
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


        
}
