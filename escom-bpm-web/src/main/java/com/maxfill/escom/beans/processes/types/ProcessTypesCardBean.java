package com.maxfill.escom.beans.processes.types;

import com.maxfill.dictionary.DictRoles;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.process.options.RunOptions;
import com.maxfill.model.basedict.process.options.RunOptionsFacade;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.result.ResultFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.roles.ProcessRole;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.utils.EscomUtils;
import java.util.ArrayList;
import java.util.Collections;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DualListModel;

/**
 * Контролер формы "Вид процесса"
 */
@Named
@ViewScoped
public class ProcessTypesCardBean extends BaseCardTree<ProcessType>{
    private static final long serialVersionUID = 1741713396857933255L;

    @Inject
    private ProcessTypesBean processTypesBean;
    @Inject
    private ProcessBean processBean;

    @EJB
    private ProcessTypesFacade itemsFacade;
    @EJB
    private ResultFacade resultFacade;
    @EJB
    private RunOptionsFacade runOptionsFacade;
    
    private List<Result> taskResults;
    
    private DualListModel<Result> results;
    private DualListModel<RunOptions> runOptions;
    
    private int deadLineDeltaDay = 0;
    private int deadLineDeltaHour = 0;    
    private String accordTab = null;    
    
    private final List<SelectItem> ROLES = Collections.unmodifiableList(
        new ArrayList<SelectItem>() {
            private static final long serialVersionUID = 3109256773218160485L;
            {
                add(new SelectItem(DictRoles.ROLE_CHIEF, MsgUtils.getBandleLabel(DictRoles.ROLE_CHIEF)));
                add(new SelectItem(DictRoles.ROLE_CONCORDER, MsgUtils.getBandleLabel(DictRoles.ROLE_CONCORDER)));
                add(new SelectItem(DictRoles.ROLE_CONTROLLER, MsgUtils.getBandleLabel(DictRoles.ROLE_CONTROLLER)));
                add(new SelectItem(DictRoles.ROLE_CURATOR, MsgUtils.getBandleLabel(DictRoles.ROLE_CURATOR)));
                add(new SelectItem(DictRoles.ROLE_EDITOR, MsgUtils.getBandleLabel(DictRoles.ROLE_EDITOR)));
                add(new SelectItem(DictRoles.ROLE_EXECUTOR, MsgUtils.getBandleLabel(DictRoles.ROLE_EXECUTOR)));
                add(new SelectItem(DictRoles.ROLE_INSPECTOR, MsgUtils.getBandleLabel(DictRoles.ROLE_INSPECTOR)));
                add(new SelectItem(DictRoles.ROLE_OWNER, MsgUtils.getBandleLabel(DictRoles.ROLE_OWNER)));
                add(new SelectItem(DictRoles.ROLE_REGISTRATOR, MsgUtils.getBandleLabel(DictRoles.ROLE_REGISTRATOR)));                
            }
        });
        
    @Override
    public void doPrepareOpen(ProcessType processType){  
        initDateFields(processType);
    }
    
    @Override
    protected void onBeforeSaveItem(ProcessType processType){
        if (StringUtils.isEmpty(processType.getGuide())){            
            processType.setGuide(EscomUtils.generateGUID());
        }        
        if (StringUtils.isBlank(processType.getNameReports())){
            processType.setShowReports(false);
        } else {
            processType.setShowReports(true);
        }
        saveDateFields(processType);
        super.onBeforeSaveItem(processType);
    }
           
    private void initDateFields(ProcessType processType){
        long hoursInMilli = 3600;
        long daysInMilli = hoursInMilli * 24;
        if (processType.getDefaultDeltaDeadLine() != null){
            long deltaSec = processType.getDefaultDeltaDeadLine();

            Long elapsedDays = deltaSec / daysInMilli;
            deadLineDeltaDay = elapsedDays.intValue();
            deltaSec = deltaSec % daysInMilli;

            Long elapsedHours = deltaSec / hoursInMilli;
            deadLineDeltaHour = elapsedHours.intValue();        
        }                
    }
    
    private void saveDateFields(ProcessType processType){
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        processType.setDefaultDeltaDeadLine(seconds);
    }
    
    @Override
    protected BaseTreeBean getTreeBean() {
        return processTypesBean;
    }

    public void onDeleteRole(ProcessRole role){
        getEditedItem().getProcessRoles().remove(role);
        onItemChange();
    }
    
    public void onAddRole(){
        getEditedItem().getProcessRoles().add(new ProcessRole(getEditedItem()));
        onItemChange();
    }
    
    /* GETS & SETS */
    
    public List<SelectItem> getROLES() {
        return ROLES;
    }  
    
    @Override
    public List<State> getStateForChild() {
        return processBean.getMetadatesObj().getStatesList();
    }
    
    public List<Result> getTaskResults() {
        if (taskResults == null){
            taskResults = resultFacade.findTaskResults(getEditedItem());
        }
        return taskResults;
    }
    
    public DualListModel<Result> getResults() {
        if (results == null){
            List<Result> allResults = resultFacade.findAll(getCurrentUser());
            allResults.removeAll(getTaskResults());
            results = new DualListModel<>(allResults, getTaskResults());
        }
        return results;
    }
    public void setResults(DualListModel<Result> results) {
        this.results = results;
        getEditedItem().setResults(results.getTarget());                
    }  

    public DualListModel<RunOptions> getRunOptions() {
        if (runOptions == null){
            List<RunOptions> allOptions = runOptionsFacade.findAll();
            List<RunOptions> currentOptions = runOptionsFacade.findRunOptionsByProcType(getEditedItem());
            if (!getEditedItem().isInheritRunOptions() && currentOptions.isEmpty()){
                currentOptions.addAll(runOptionsFacade.getBaseRunOptions());
            }
            allOptions.removeAll(currentOptions);
            runOptions = new DualListModel<>(allOptions, currentOptions);
        }
        return runOptions;
    }
    public void setRunOptions(DualListModel<RunOptions> runOptions) {
        this.runOptions = runOptions;
        getEditedItem().setRunOptions(runOptions.getTarget());
    }
        
    @Override
    public ProcessType getEditedItem() {
        return super.getEditedItem(); 
    }

    public int getDeadLineDeltaDay() {
        return deadLineDeltaDay;
    }
    public void setDeadLineDeltaDay(int deadLineDeltaDay) {
        this.deadLineDeltaDay = deadLineDeltaDay;
    }

    public int getDeadLineDeltaHour() {
        return deadLineDeltaHour;
    }
    public void setDeadLineDeltaHour(int deadLineDeltaHour) {
        this.deadLineDeltaHour = deadLineDeltaHour;
    }

    public String getAccordTab() {
        return accordTab;
    }
    public void setAccordTab(String accordTab) {
        this.accordTab = accordTab;
    }
     
    @Override
    public ProcessTypesFacade getFacade() {
        return itemsFacade;
    }
}