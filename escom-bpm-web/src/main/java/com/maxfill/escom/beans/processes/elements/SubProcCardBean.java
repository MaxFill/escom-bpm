package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.process.schemes.elements.SubProcessElem;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.SelectEvent;
import org.springframework.util.CollectionUtils;

/**
 * Контролер формы "Подпроцесс"
 */
@Named
@ViewScoped
public class SubProcCardBean extends BaseViewBean<BaseView>{

    @EJB
    private ProcessTypesFacade procTypesFacade;
    @EJB
    private ProcessFacade procFacade;
    @EJB
    private ProcessTemplFacade procTemplFacade;
    
    private final SubProcessElem editedItem = new SubProcessElem();
    private SubProcessElem sourceItem;
    private ProcessType selProcType;
    private ProcTempl selProcTempl;
    private Process process;
        
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){
            if (sourceBean != null){
                sourceItem = (SubProcessElem)((DiagramBean)sourceBean).getBaseElement();
                if (sourceItem.getProcessId() != null){                    
                    process = procFacade.find(sourceItem.getProcessId());
                }
                if (sourceItem.getProctypeId() != null){
                    selProcType = procTypesFacade.find(sourceItem.getProctypeId());
                }
                if (sourceItem.getProctemplId() != null){
                    selProcTempl = procTemplFacade.find(sourceItem.getProctemplId());
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
        
    public String onSaveAndCloseCard(Object param){
        try {
            if (selProcType != null){
                editedItem.setProctypeId(selProcType.getId());
            }
            editedItem.setProctemplId(selProcTempl.getId());
            editedItem.setCaption(makeCaption());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return onCloseCard(param);
    }
    
    public String makeCaption(){        
        if (selProcTempl != null){
            return selProcTempl.getName();
        } else 
            if (selProcType != null){
                return selProcType.getName();
            }
        return "???";
    } 
        
    @Override
    public String getFormName() {
        return DictFrmName.FRM_SUB_PROCESS;
    }
    
    public void onProcTypeSelected(SelectEvent event){
        List<ProcessType> processTypes = (List<ProcessType>) event.getObject();
        if (CollectionUtils.isEmpty(processTypes)) return;
        selProcType = processTypes.get(0);
    }
    public void onProcTypeSelected(ValueChangeEvent event){
        selProcType = (ProcessType) event.getNewValue();        
    } 
        
    public void onProcTemplSelected(SelectEvent event){
        List<ProcTempl> procTemplates = (List<ProcTempl>) event.getObject();
        if (CollectionUtils.isEmpty(procTemplates)) return;
        selProcTempl = procTemplates.get(0);
    }
    public void onProcTemplSelected(ValueChangeEvent event){
        selProcTempl = (ProcTempl) event.getNewValue();;
    }
    
    public boolean isReadOnly(){
        return false;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("SubProcess");
    }

    public ProcessType getSelProcType() {
        return selProcType;
    }
    public void setSelProcType(ProcessType selProcType) {
        this.selProcType = selProcType;
    }

    public ProcTempl getSelProcTempl() {
        return selProcTempl;
    }
    public void setSelProcTempl(ProcTempl selProcTempl) {
        this.selProcTempl = selProcTempl;
    }
    
    public SubProcessElem getEditedItem() {
        return editedItem;
    }
       
}