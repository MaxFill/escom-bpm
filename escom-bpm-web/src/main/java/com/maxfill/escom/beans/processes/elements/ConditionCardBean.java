package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.facade.ConditionFacade;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.schemes.elements.ConditionElem;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Свойства условия процесса"
 */
@Named
@ViewScoped
public class ConditionCardBean extends BaseViewBean{    
    private static final long serialVersionUID = -5186880746110498838L;
    
    @EJB
    private ConditionFacade conditionFacade;
    
    private Condition selected;
    private ConditionElem sourceItem = null;
    private ConditionElem editedItem = new ConditionElem();
    private ProcessCardBean sourceBean;
    
     @Override
    public void onBeforeOpenCard(){
        if (sourceItem == null){
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();            
            beanId = params.get(SysParams.PARAM_BEAN_ID);
            String beanName = params.get(SysParams.PARAM_BEAN_NAME);
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            Map map = (Map) session.getAttribute("com.sun.faces.application.view.activeViewMaps");          
            for (Object entry : map.values()) {
              if (entry instanceof Map) {
                Map viewScopes = (Map) entry;
                if (viewScopes.containsKey(beanName)) {
                    sourceBean = (ProcessCardBean) viewScopes.get(beanName);
                    String id = sourceBean.toString();
                    if (beanId.equals(id)) break;
                }
              }
            }
            if (sourceBean != null){
                sourceItem = (ConditionElem)sourceBean.getBaseElement(); 
                selected = conditionFacade.find(sourceItem.getConditonId());
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
    public String onCloseCard(String param){
        try {
            editedItem.setConditonId(selected.getId());
            editedItem.setCaption(selected.getName());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_CONDITION;
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
