package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.model.process.schemes.elements.StatusElem;
import com.maxfill.model.statuses.StatusesDoc;
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
 * Контролер формы "Карточка элемента процесса "Статус документа""
 */
@Named
@ViewScoped
public class DocStatusCardBean extends BaseViewBean{    
    private static final long serialVersionUID = -5286296381383874923L;

    @EJB
    private StatusesDocFacade statuseFacade;
    
    private StatusesDoc selected = null;
    private final StatusElem editedItem = new StatusElem();
    private StatusElem sourceItem;
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
                sourceItem = (StatusElem)sourceBean.getBaseElement(); 
                
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
    public String onCloseCard(String param){
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
