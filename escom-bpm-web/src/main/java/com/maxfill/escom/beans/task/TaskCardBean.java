package com.maxfill.escom.beans.task;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.ContainsTask;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.ResultFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.metadates.MetadatesStates;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.Process;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.task.TaskStates;
import com.maxfill.services.workflow.Workflow;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import org.primefaces.event.SelectEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import liquibase.util.StringUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.model.DualListModel;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseViewBean{
    private static final long serialVersionUID = -2860068605023348908L;

    @EJB
    private TaskFacade taskFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private ResultFacade resultFacade;
    
    @Inject
    private ProcessBean processBean;
    @Inject
    private DocBean docBean;
            
    private Task editedItem = new Task();
    private boolean readOnly;
    private Task sourceTask = null;
    private ContainsTask sourceBean = null;
    private String beanName;   
    private List<Result> taskResults;
    private DualListModel<Result> results;
    
    @Override
    public void onBeforeOpenCard(){
        if (sourceTask == null){
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();            
            beanId = params.get(SysParams.PARAM_BEAN_ID);
            beanName = params.get(SysParams.PARAM_BEAN_NAME);
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            Map map = (Map) session.getAttribute("com.sun.faces.application.view.activeViewMaps");          
            for (Object entry : map.values()) {
              if (entry instanceof Map) {
                Map viewScopes = (Map) entry;
                if (viewScopes.containsKey(beanName)) {
                    sourceBean = (ContainsTask) viewScopes.get(beanName);
                    String id = sourceBean.toString();
                    if (beanId.equals(id)) break;
                }
              }
            }
            if (sourceBean != null){
                sourceTask = sourceBean.getTask();
                TaskStates taskStates = sourceTask.getState();
                State state = taskStates.getCurrentState();
                int id = state.getId();
                if (sourceTask.getScheme() != null && DictStates.STATE_DRAFT != id){
                    readOnly = true;
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
        Set<String> errors = new HashSet<>();
        saveTask(errors);
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return "";
        } 
        return super.onCloseCard(param);
    }
    
    /**
     * Сохранение изменений в задаче
     * @param errors 
     */
    private void saveTask(Set<String> errors){
        validateTask(errors);
        if (errors.isEmpty()){            
            try {
                BeanUtils.copyProperties(sourceTask, editedItem);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                errors.add("InternalErrorSavingTask");
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void validateTask(Set<String> errors){
        if (StringUtils.isEmpty(editedItem.getAvaibleResultsJSON())){
            errors.add("TaskNoHaveListResult");
        }
    }
    
    /**
     * Обработка события выполнения задачи
     * @param result
     * @return 
     */
    public String onExecute(Result result){        
        Set<String> errors = new HashSet<>();
        saveTask(errors);
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return "";
        } 
        ProcessCardBean bean = (ProcessCardBean)sourceBean;
        workflow.executeTask(bean.getEditedItem(), sourceTask, result, errors);
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return "";
        }
        return super.onCloseCard("run");
    }
    
    /**
     * Обработка события открытия карточки процесса 
     */
    public void onOpenProcess(){        
        Process process = getProcess();
        if (process != null){
            processBean.prepEditItem(process);
        } else {
            EscomMsgUtils.errorMsg("LinkProcessIncorrect");
        }
    }
    
    /**
     * Обработка события открытия карточки документа 
     */
    public void onOpenDocument(){        
        Process process = getProcess();        
        if (process != null){ 
            Doc doc = process.getDoc();
            if (doc != null){
               docBean.prepEditItem(doc);
            } else {
               EscomMsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            }
        } else {
            EscomMsgUtils.errorMsg("LinkProcessIncorrect");
        }
    }
    
    /**
     * Обработка события просмотра документа
     */
    public void onViewDocument(){
        Process process = getProcess();
        if (process != null){ 
            Doc doc = process.getDoc();
            if (doc != null){
               docBean.onViewMainAttache(doc);
            } else {
               EscomMsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            }
        } else {
            EscomMsgUtils.errorMsg("LinkProcessIncorrect");
        }
    }
    
    private Process getProcess(){
        Scheme scheme = editedItem.getScheme();
        if (scheme == null) return null;
        return scheme.getProcess();
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
    
    public Boolean isShowExtTaskAtr(){
        if (sourceBean == null) return false;
        return sourceBean.isShowExtTaskAtr();
    }

    /* Возвращает список состояний доступных объекту из его текущего состояния */
    public List<State> getAvailableStates(){        
        Metadates metaObj = getMetadatesObj();
        List<MetadatesStates> metadatesStates = metaObj.getMetadatesStates();
        List<State> result = metadatesStates.stream()
                .map(metadatesState -> metadatesState.getStateTarget())
                .collect(Collectors.toList());        
        return result;
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
    
    /* Получение ссылки на объект метаданных  */
    public Metadates getMetadatesObj() {        
        return taskFacade.getMetadatesObj();        
    }
    
    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_TASK+"-card";
    }

    public String getBeanName() {
        return beanName;
    }

    public DualListModel<Result> getResults() {
        if (results == null){
            List<Result> allResults = resultFacade.findAll();            
            results = new DualListModel<>(allResults, getTaskResults());
        }
        return results;
    }
    public void setResults(DualListModel<Result> results) {
        this.results = results;
        editedItem.setTaskResults(results.getTarget());                
    }   
        
    public List<Result> getTaskResults() {
        if (taskResults == null){
            taskResults = resultFacade.findTaskResults(editedItem);
        }
        return taskResults;
    }
        
}
