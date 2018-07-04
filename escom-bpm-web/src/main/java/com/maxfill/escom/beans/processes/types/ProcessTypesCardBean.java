package com.maxfill.escom.beans.processes.types;

import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.facade.ProcessTypesFacade;
import com.maxfill.facade.ResultFacade;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.states.State;
import com.maxfill.model.task.result.Result;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
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
    
    private List<Result> taskResults;
    private DualListModel<Result> results;
    
    @Override
    public ProcessTypesFacade getFacade() {
        return itemsFacade;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return processTypesBean;
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
            List<Result> allResults = resultFacade.findAll();
            allResults.removeAll(getTaskResults());
            results = new DualListModel<>(allResults, getTaskResults());
        }
        return results;
    }
    public void setResults(DualListModel<Result> results) {
        this.results = results;
        getEditedItem().setResults(results.getTarget());                
    }  

    @Override
    public ProcessType getEditedItem() {
        return super.getEditedItem(); 
    }
    
    
}