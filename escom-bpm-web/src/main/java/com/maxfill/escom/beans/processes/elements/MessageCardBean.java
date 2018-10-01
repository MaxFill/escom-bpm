package com.maxfill.escom.beans.processes.elements;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.processes.DiagramBean;
import com.maxfill.model.basedict.process.schemes.elements.MessageElem;
import java.lang.reflect.InvocationTargetException;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.ProcessFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DualListModel;

/**
 * Контролер формы "Сообщение процесса"
 */
@Named
@ViewScoped
public class MessageCardBean extends BaseViewBean<DiagramBean>{    
    private static final long serialVersionUID = -1566412000716670159L;

    @EJB
    private ProcessFacade processFacade;
        
    private MessageElem editedItem = new MessageElem();
    private MessageElem sourceItem;

    private DualListModel<String> roles;     
    private List<String> liveRoles = new ArrayList<>();
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceItem == null){
            if (sourceBean != null){
                sourceItem = (MessageElem)((DiagramBean)sourceBean).getBaseElement();
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
            liveRoles = roles.getTarget();
            saveRoleToJson();
            BeanUtils.copyProperties(sourceItem, editedItem);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(param);
    }
    
    /**
     * Сохранение ролей в строку для последующей записи в базу
     */
    public void saveRoleToJson(){
        Gson gson = new Gson();
        String json = gson.toJson(liveRoles);
        editedItem.setRecipientsJSON(json);
    }
    
    private void restoreFields(){
        if (StringUtils.isNoneEmpty(sourceItem.getRecipientsJSON())){
            Gson gson = new Gson();
            liveRoles = gson.fromJson(sourceItem.getRecipientsJSON(), List.class);        
        }
    }

    public String getLabelForRoleFromBundle(String role){
        String key = StringUtils.capitalize(role.toLowerCase());
        return getLabelFromBundle(key);
    }

    /* GETS & SETS */
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_MESSAGE;
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
        if (roles == null){            
            Process process = sourceBean.getProcess();
            List<String> source = processFacade.getRoles(process);
            if (liveRoles == null){
                liveRoles = new ArrayList<>();
            }
            source.removeAll(liveRoles);
            roles = new DualListModel<>(source, liveRoles);
        }
        return roles;
    }
    public void setRoles(DualListModel<String> roles) {
        this.roles = roles;
    }        
    
}