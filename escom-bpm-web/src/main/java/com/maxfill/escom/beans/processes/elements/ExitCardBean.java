package com.maxfill.escom.beans.processes.elements;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.process.schemes.elements.ExitElem;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Контролер формы "Свойства элемента процесса "Выход"
 */
@Named
@ViewScoped
public class ExitCardBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 5642232515643258977L;
        
    private ExitElem editedItem = new ExitElem();
    private ExitElem sourceItem;
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){            
            if (sourceBean != null){
                sourceItem = (ExitElem)((ProcessCardBean)sourceBean).getBaseElement();                 
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
        return DictDlgFrmName.FRM_EXIT;
    } 

    public ExitElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(ExitElem editedItem) {
        this.editedItem = editedItem;
    }
        
}
