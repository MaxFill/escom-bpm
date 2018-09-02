package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.process.schemes.elements.ProcedureElem;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Процедура процесса"
 */
@Named
@ViewScoped
public class ProcedureCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -8295618285469838899L;
    
    private ProcedureElem editedItem = new ProcedureElem();
    private ProcedureElem sourceItem;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){
            if (sourceBean != null){
                sourceItem = (ProcedureElem)((DiagramBean)sourceBean).getBaseElement();
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
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_PROCEDURE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Procedure");
    }

    public ProcedureElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(ProcedureElem editedItem) {
        this.editedItem = editedItem;
    }  
}
