package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.model.basedict.process.procedures.Procedure;
import com.maxfill.model.basedict.process.procedures.ProcedureFacade;
import com.maxfill.model.basedict.process.schemes.elements.ProcedureElem;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import org.omnifaces.cdi.ViewScoped;
import javax.ejb.EJB;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 * Контролер формы "Процедура процесса"
 */
@Named
@ViewScoped
public class ProcedureCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -8295618285469838899L;

    @EJB
    private ProcedureFacade procedureFacade;

    private Procedure selected;

    private ProcedureElem editedItem = new ProcedureElem();
    private ProcedureElem sourceItem;
    
    private String caption;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){
            if (sourceBean != null){
                sourceItem = (ProcedureElem)((DiagramBean)sourceBean).getBaseElement();
                if (sourceItem.getProcedureId() != null){
                    selected = procedureFacade.find(sourceItem.getProcedureId());
                    if (StringUtils.isEmpty(sourceItem.getCaption())){
                        caption = getLabelFromBundle(selected.getName());
                    } else {
                        caption = sourceItem.getCaption();
                    }
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
    public String onCloseCard(Object param){
        try {
            if (selected != null){
                editedItem.setProcedureId(selected.getId());
                editedItem.setCaption(caption);
            }
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    public void onActionChange(){        
        if (selected == null) return;
        if (StringUtils.isNotBlank(selected.getName())){
            String key = selected.getName();
            setCaption(getLabelFromBundle(key));
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:name");
        }
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_PROCEDURE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Procedure");
    }

    /* GETS & SETS */

    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public Procedure getSelected() {
        return selected;
    }
    public void setSelected(Procedure selected) {
        this.selected = selected;
    }

    public ProcedureElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(ProcedureElem editedItem) {
        this.editedItem = editedItem;
    }  
}
