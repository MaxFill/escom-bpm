package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
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
    private static final long serialVersionUID = -846503348131436516L;
    
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
    private Process selProcess;
    private List<ProcTempl> templates;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){
            if (sourceBean != null){
                sourceItem = (SubProcessElem)((DiagramBean)sourceBean).getBaseElement();
                if (sourceItem.getProcessId() != null){                    
                    selProcess = procFacade.find(sourceItem.getProcessId());
                }
                if (sourceItem.getProctypeId() != null){
                    selProcType = procTypesFacade.find(sourceItem.getProctypeId());
                    initTemplates(selProcType);
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
            editedItem.setProctypeId(selProcType.getId());            
            if (selProcTempl != null){
                editedItem.setProctemplId(selProcTempl.getId());
            } else {
                editedItem.setProctemplId(null);
            }
            editedItem.setCaption(makeCaption());
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return onCloseCard(param);
    }
    
    public String makeCaption(){        
        StringBuilder sb = new StringBuilder();
        if (selProcType != null){
            sb.append(selProcType.getName()).append(" ");        
            if (selProcTempl != null){
                sb.append(selProcTempl.getName());            
            }
            return sb.toString();
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
        initTemplates(selProcType);
    }
        
    public void onProcTemplSelected(){        
    }
    
    public boolean isReadOnly(){
        return editedItem.getProcessId() != null; //если подпроцесс создан, то менять его парамерты на этой карточке нельзя!
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("SubProcess");
    }

    private void initTemplates(ProcessType processType){         
        templates = processType.getTemplates();
        selProcTempl = templates.stream().filter(templ -> templ.getIsDefault()).findFirst().orElse(null);
    }
    
    /* GETS & SETS */

    public List<ProcTempl> getTemplates() {
        return templates;
    }
    public void setTemplates(List<ProcTempl> templates) {
        this.templates = templates;
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