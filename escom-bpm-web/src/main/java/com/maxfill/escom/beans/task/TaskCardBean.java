package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.model.process.schemes.elements.TaskElem;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.staffs.Staff;
import java.lang.reflect.InvocationTargetException;
import org.primefaces.event.SelectEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseViewBean{
    private static final long serialVersionUID = -2860068605023348908L;

    @EJB
    private TaskFacade taskFacade;
    
    private Task editedItem = new Task();
    private boolean readOnly;
    private Task sourceTask = null;
    
    @Override
    public void onBeforeOpenCard(){
        if (sourceTask == null){
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();            
            if (params.containsKey("itemUID")){            
                String uid = params.get("itemUID");
                sourceTask = taskFacade.findByLinkUID(uid);
            } else {
                String beanId = params.get("beanId");
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                Map map = (Map) session.getAttribute("com.sun.faces.application.view.activeViewMaps");
                ProcessCardBean myBean = null;            
                String beanName = ProcessCardBean.class.getSimpleName().substring(0, 1).toLowerCase() + ProcessCardBean.class.getSimpleName().substring(1);
                for (Object entry : map.values()) {
                  if (entry instanceof Map) {
                    Map viewScopes = (Map) entry;
                    if (viewScopes.containsKey(beanName)) {
                        myBean = (ProcessCardBean) viewScopes.get(beanName);
                        String id = myBean.toString();
                        if (beanId.equals(id)) break;
                    }
                  }
                }
                if (myBean != null){
                    sourceTask = ((TaskElem) myBean.getBaseElement()).getTask();
                }                
            }
            if (sourceTask != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceTask);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    Logger.getLogger(TaskCardBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    @Override
    public String onCloseCard(String param){
        try {
            BeanUtils.copyProperties(sourceTask, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(TaskCardBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
        
    /**
     * Обработка события сброса Исполнителя
     */
    public void onClearExecutor(){
        editedItem.setOwner(null);
    }
    
    /**
     * Обработка события выбора Исполнителя
     * @param event
     */
    public void onExecutorChanged(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()) return;
        editedItem.setOwner(items.get(0));
    }
    public void onExecutorChanged(ValueChangeEvent event){
         editedItem.setOwner((Staff) event.getNewValue());
    }
    
    /* GETS & SETS */

    public Task getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(Task editedItem) {
        this.editedItem = editedItem;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_TASK;
    }
}