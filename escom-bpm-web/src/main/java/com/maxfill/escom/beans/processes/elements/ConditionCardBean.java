package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.process.conditions.ConditionFacade;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.schemes.elements.ConditionElem;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Свойства условия процесса"
 */
@Named
@ViewScoped
public class ConditionCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -5186880746110498838L;
    
    @EJB
    private ConditionFacade conditionFacade;
    
    private Condition selected = null;
    private ConditionElem sourceItem = null;
    private ConditionElem editedItem = new ConditionElem();    
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){                        
            if (sourceBean != null){
                sourceItem = (ConditionElem)((ProcessCardBean)sourceBean).getBaseElement(); 
                if (sourceItem.getConditonId() != null){
                    selected = conditionFacade.find(sourceItem.getConditonId());
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
            if (selected != null){
                editedItem.setConditonId(selected.getId());
                editedItem.setCaption(selected.getName());
            }
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_CONDITION;
    }     

    /* GETS & SETS */
    
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

    
}
