package com.maxfill.escom.beans.processes.elements;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.model.process.schemes.elements.MessageElem;
import com.maxfill.model.states.State;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

/**
 * Контролер формы "Сообщение процесса"
 */
@Named
@ViewScoped
public class MessageCardBean extends BaseViewBean<BaseView>{    
    private static final long serialVersionUID = -1566412000716670159L;

    private MessageElem editedItem = new MessageElem();
    private MessageElem sourceItem;
    
    private DualListModel<String> roles;
     
    private List<String> liveRoles = new ArrayList<>();
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){            
            if (sourceBean != null){
                sourceItem = (MessageElem)((ProcessCardBean)sourceBean).getBaseElement();                 
            }
            if (sourceItem != null){
                try {
                    BeanUtils.copyProperties(editedItem, sourceItem);
                    restoreFields();
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }    
    
    @Override
    public String onCloseCard(Object param){
        try {
            saveRoleToJson();            
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    /**
     * Сохранение ролей в строку для полседующей записи в базу     
     */
    public void saveRoleToJson(){
        Gson gson = new Gson();
        String json = gson.toJson(liveRoles);
        editedItem.setRecipientsJSON(json);
    }
    
    private void restoreFields(){
        Gson gson = new Gson();
        liveRoles = gson.fromJson(sourceItem.getRecipientsJSON(), List.class);        
    }
    
    public void onTransfer(TransferEvent event){
        
    }
    
    /* GETS & SETS */
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_PROCEDURE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Message");
    }

    public MessageElem getEditedItem() {
        return editedItem;
    }
    public void setEditedItem(MessageElem editedItem) {
        this.editedItem = editedItem;
    }    

    public DualListModel<String> getRoles() {
        return roles;
    }
    
    
}