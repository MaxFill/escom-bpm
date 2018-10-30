package com.maxfill.escom.beans.processes.remarks;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.core.states.State;
import com.maxfill.model.core.states.StateFacade;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

/**
 * Контролер карточки "Замечание"
 */
@Named
@ViewScoped
public class RemarkCardBean extends BaseCardBean<Remark>{
    private static final long serialVersionUID = -6399475562664755663L;       
    
    @EJB    
    private RemarkFacade remarkFacade;    
    @EJB    
    private StateFacade stateFacade;           
    @EJB    
    private TaskFacade taskFacade;
    @EJB
    private DocFacade docFacade;
    
    private Process process;
    private Doc doc;    
    private final List<Remark> remarks = new ArrayList<>();
    private List<User> authors;
    protected String currentTab = "0";
    private Task task;
    private boolean remarkTabShow = false;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){ 
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
        if (params.containsKey("itemId")){
            Integer docId = Integer.valueOf(params.get("itemId"));
            doc = docFacade.find(docId);
            if (doc == null) return;

            if (appBean.isCanUsesProcess() && doc != null){            
                remarkTabShow = true;
                List<Remark> remarklist = doc.getDetailItems();
                if (!remarklist.isEmpty()){
                    remarks.addAll(remarklist);
                }
            }
        }    
    }
    
    public void onSaveChange(){
        remarks.stream()
                .filter(remark->Objects.equals(getCurrentUser(), remark.getAuthor()))
                .forEach(remark->remarkFacade.edit(remark));
    }
    
    @Override
    public boolean isEastShow(){
       return isRemarkTabShow(); 
    }
    
    public void onCreateRemark(){
        Remark remark = remarkFacade.createItem(getCurrentUser(), null, doc, new HashMap<>());
        remark.setProcess(process);
        remarkFacade.create(remark);
        remarks.add(remark);
    }    
    
    public List<User> getAuthors(){
        if (authors == null){
            authors = new ArrayList<>(remarks.stream().map(remark->remark.getAuthor()).collect(Collectors.toSet()));
            if (!authors.contains(getCurrentUser())){
                authors.add(getCurrentUser());
            }
        }
        return authors;
    }
    
    public List<Remark> getAuthorRemarks(User author){
        return remarks.stream()
                .filter(remark -> remark.getAuthor().equals(author))
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
    
    @Override
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
        remarkFacade.edit(remark);
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
        return Objects.equals(stateRun, process.getState().getCurrentState());
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
    
    public Process getProcess() {
        return process;
    }
    
    @Override
    public RemarkFacade getFacade() {        
        return remarkFacade;
    }
    
    public boolean isRemarkTabShow() {
        return remarkTabShow;
    }
    public void setRemarkTabShow(boolean remarkTabShow) {
        this.remarkTabShow = remarkTabShow;
    }
        
    public String getCurrentTab() {
        return currentTab;
    }
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }
        
    public List<Remark> getRemarks(){        
        return remarks;
    }
}