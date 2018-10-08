package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.services.files.FileService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/* Версии вложений, работа с прикреплёнными файлами  */
@Named
@SessionScoped
public class AttacheBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -5107683464380454618L;
    
    @EJB
    protected FileService fileService;
    @EJB
    private DocFacade docFacade;
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private AttacheService attacheService;    
    @EJB    
    private RemarkFacade remarkFacade;    
    @EJB    
    private StateFacade stateFacade;
    @EJB    
    private TaskFacade taskFacade;
    
    @Inject
    private DocBean docBean;
    
    private StreamedContent content;     
      
    private Process process;
    private Task task;
    private boolean remarkTabShow = false;
    private Doc doc;
    private final List<Remark> remarks = new ArrayList<>();
    protected String currentTab = "0";
    
    /* Копирует вложение */
    public Attaches copyAttache(Attaches sourceAttache){
        Attaches newAttache = attacheFacade.copyAttache(sourceAttache);        
        newAttache.setAuthor(sessionBean.getCurrentUser());                
        attacheService.doCopy(sourceAttache, newAttache);        
        return newAttache;
    }
    
    /* Загрузка файла вложения */ 
    public Attaches uploadAtache(UploadedFile uploadFile) throws IOException{
        if (uploadFile == null) return null;                

        String fileName = uploadFile.getFileName();

        Map<String, Object> params = new HashMap<>();
        params.put("contentType", uploadFile.getContentType());
        params.put("fileName", fileName);
        params.put("size", uploadFile.getSize());
        params.put("author", sessionBean.getCurrentUser());
        return attacheService.uploadAtache(params, uploadFile.getInputstream());
    }        
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){        
        String path = null;
        if (params.containsKey("itemId")){
            Integer docId = Integer.valueOf(params.get("itemId"));
            doc = docFacade.find(docId);
            if (doc == null) return;
            docBean.getLazyFacade().actualizeRightItem(doc, getCurrentUser());
            if (docBean.getLazyFacade().isHaveRightView(doc)) {
                Attaches attache = doc.getMainAttache();
                if (attache == null) return;
                path = conf.getUploadPath() + attache.getFullNamePDF();
            } else {
                MsgUtils.warnMsg("RightViewNo");
            }    
        } else {
            path = params.get("path");
        }
        
        if (path == null) {
            LOGGER.log(Level.SEVERE, null, "ESCOM_BPM ERROR: file path is null!");
            return;            
        } 
        if (sourceBean instanceof ProcessCardBean){            
            process = (Process)sourceBean.getSourceItem();            
        }
        if (params.containsKey("taskID")){
            Integer taskId = Integer.valueOf(params.get("taskID"));
            task = taskFacade.find(taskId);
            if (process == null){
                process = task.getScheme().getProcess();
            }
        }
        
        if (appBean.isCanUsesProcess() && doc != null && process != null){            
            remarkTabShow = true;
            List<Remark> remarklist = doc.getDetailItems();
            if (!remarklist.isEmpty()){
                remarks.addAll(remarklist);
            }
        }
        try {
            content = new DefaultStreamedContent(new FileInputStream(path), "application/pdf");                
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String onCancelItemSave() {
        sourceBean = null;
        remarks.clear();
        return super.onCancelItemSave(); 
    }    
    
    public String getAttachePath(Attaches attache){
        return conf.getUploadPath() + attache.getFullName();
    }
            
    /* ЗАМЕЧАНИЯ */
    
    public void onCreateRemark(){
        Remark remark = remarkFacade.createItem(getCurrentUser(), null, doc, new HashMap<>());
        remark.setProcess(process);
        remarkFacade.create(remark);
        remarks.add(remark);
    }
    
    public void onSaveRemark(Remark remark){
        remarkFacade.edit(remark);
        MsgUtils.succesMessage(getLabelFromBundle("ObjectSaved"));
    }
    
    public List<Remark> getMyRemarks(){
        return remarks.stream()
                .filter(remark -> remark.getAuthor().equals(getCurrentUser()))
                .collect(Collectors.toList());
    }
    
    public List<Remark> getOtherRemarks(){
        return remarks.stream()
                .filter(remark -> !remark.getAuthor().equals(getCurrentUser()))
                .collect(Collectors.toList());
    }
        
    public void onChangeStateRemark(Remark remark, Integer stateId){
        State state = stateFacade.find(stateId);
        remark.getState().setCurrentState(state);
        if (state.equals(stateFacade.getIssuedState())){
            remark.setChecked(false);
        }
        remarkFacade.edit(remark);
    }
    
    public void onDeleteRemark(Remark remark){
        remarkFacade.remove(remark);
        remarks.remove(remark);
    }
    
    public String getRemarkHeader(Remark remark){
        StringBuilder sb = new StringBuilder();
        //String stateLocalName = stateBean.getBundleName(remark.getState().getCurrentState());
        sb.append(remark.getAuthor().getName());
        return StringUtils.abbreviate(sb.toString(), 35);
    }
    
    public void onRemarkCheck(Remark remark){
        remark.setChecked(true);
        remarkFacade.edit(remark);
    }
    
    public void onRemarkUnCheck(Remark remark){
        remark.setChecked(false);
        remarkFacade.edit(remark);
    }
    
    public void onTabChange(TabChangeEvent event) {
        Tab tab = event.getTab();
        String tabId = tab.getId();
        switch (tabId) {
            case "tabMyRemarks": {
                currentTab = "0";                
                break;
            }
            case "tabFilter": {
                currentTab = "1";
                break;
            }
        }
    }
    
    public void onNotifyRemark(Remark remark){
        onSaveRemark(remark);
        Map<String, List<String>> params = getParamsMap();        
        params.put("remarkID", Collections.singletonList(remark.getId().toString()));
        sessionBean.openDialogFrm(DictFrmName.FRM_NOTIFY, params);
    }
    
    /**
     * Возможность изменять замечания доступна автору замечания и когда находится в работе
     * Если есть задача и она находится в работе
     * @param remark
     * @return 
     */
    public boolean isCanModifyRemark(Remark remark) {
        if (task == null)return false;
        if (!remark.getAuthor().equals(getCurrentUser())) return false;
        State stateRun = stateFacade.getRunningState();
        return Objects.equals(stateRun, task.getState().getCurrentState());
    }
    
    /**
     * Возможность создавать замечания
     * Если есть задача и она находится в работе
     * @return 
     */
    public boolean isCanCreateRemark(){
        if (task == null)return false;
        State stateRun = stateFacade.getRunningState();
        return Objects.equals(stateRun, task.getState().getCurrentState());
    }
    
    /**
     * Возможность отмечать замечание, как снятое
     * @return 
     */
    public boolean isCanCheckRemark(){
        if (process == null) return false;        
        Staff curator = process.getCurator();
        if (Objects.equals(getCurrentStaff(), curator)){
            Integer processState = process.getState().getCurrentState().getId();
            return processState.equals(DictStates.STATE_DRAFT) || processState.equals(DictStates.STATE_CANCELLED);
        }
        return false;
    }
    
    @Override
    public boolean isEastShow(){
       return isRemarkTabShow(); 
    }
    
    /* GETS & SETS  */

    public String getCurrentTab() {
        return currentTab;
    }
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }
        
    public List<Remark> getRemarks(){        
        return remarks;
    }
    
    public StreamedContent getContent() {
        return content;
    }
    public void setContent(StreamedContent content) {
        this.content = content;
    }

    public boolean isRemarkTabShow() {
        return remarkTabShow;
    }
    public void setRemarkTabShow(boolean remarkTabShow) {
        this.remarkTabShow = remarkTabShow;
    }

    public Process getProcess() {
        return process;
    }
        
    @Override
    public String getFormName(){
        return DictFrmName.FRM_DOC_VIEWER;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("View");
    }
}