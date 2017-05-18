package com.maxfill.escom.beans.system.rights;

import com.maxfill.model.rights.Right;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.states.State;
import com.maxfill.facade.StateFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.Tuple;
import org.apache.commons.beanutils.BeanUtils;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

/* Карточка настройки прав доступа */
@Named
@ViewScoped
public class RightCardBean extends BaseDialogBean{    
    private static final long serialVersionUID = -3720038615067813059L;

    @Inject
    private UserBean userBean;
    @Inject
    private UserGroupsBean userGroupBean;    

    @EJB
    private StateFacade stateFacade;

    private Right selRight;
    private Right sourceRight;
    private User selUser;
    private UserGroups selUsGroup;
    private Integer editMode;
    private String keyRight;
    private List<User> users;
    private List<UserGroups> userGroupses;
    
    /* При открытии карточки объекта */
    @Override
    public void onOpenCard(){
        if (getSelRight() == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            editMode = Integer.valueOf(params.get("editMode"));
            
            switch (editMode){
                case DictEditMode.INSERT_MODE:{
                    Integer stateId = Integer.valueOf(params.get("stateId"));
                    State state = stateFacade.find(stateId);
                    selRight = new Right(0, 0, null, "<Не указано!>", state);
                    break;
                }
                case DictEditMode.EDIT_MODE:{
                    keyRight = params.get("keyRight");
                    sourceRight = getSessionBean().getSourceRight(keyRight);
                    try {
                        selRight = new Right();
                        BeanUtils.copyProperties(selRight, sourceRight);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        Logger.getLogger(RightCardBean.class.getName()).log(Level.SEVERE, null, ex);
                    }    
                    break;
                }
            }
            
            switch (selRight.getObjType()){
                case DictRights.TYPE_GROUP: {  
                    selUser = null;
                    selUsGroup = userGroupBean.findItem(selRight.getObjId());
                    break;
                }
                case DictRights.TYPE_USER: {
                    selUser = userBean.findItem(selRight.getObjId());
                    selUsGroup = null;
                    break;
                }
            }
        }
    }
    
    public String onSaveChangeRight() {        
        if (isItemChange()) {
            switch(editMode){
                case DictEditMode.INSERT_MODE:{                    
                    break;
                }
                case DictEditMode.EDIT_MODE:{
                    try {
                        BeanUtils.copyProperties(sourceRight, selRight);
                        selRight = null;
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        Logger.getLogger(RightCardBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }  
            }
        }        
        return onCloseCard();
    }    

    public String onCancelSave(){
        setItemChange(Boolean.FALSE);
        return onCloseCard();
    }

    @Override
    public String onCloseCard(){
        Tuple<Boolean, Right> tuple = new Tuple(isItemChange(), selRight);
        getSessionBean().removeSourceRight(keyRight);
        return super.onFinalCloseCard(tuple);
    }
    
    /* Событие изменения опции ВСЕ ПРАВА  */
    public void onChangeAllRight(ValueChangeEvent event){
        Boolean newValue = (Boolean) event.getNewValue();
        getSelRight().setCreate(newValue);
        getSelRight().setDelete(newValue);
        getSelRight().setUpdate(newValue);
        getSelRight().setRead(newValue);
        getSelRight().setChangeRight(newValue);
        getSelRight().setExecute(newValue);
    }
    
    /* Событие изменения пользователя в карточке права */ 
    public void onUserChange(ValueChangeEvent event){
        User user = (User) event.getNewValue();
        selRight.setName(user.getShortFIO());
        selRight.setObjId(user.getId());
    }
    
    /* Событие изменение группы на карточке права */ 
    public void onGroupChange(ValueChangeEvent event){     
        UserGroups usGroup = (UserGroups) event.getNewValue();
        if (usGroup != null){
            selRight.setObjId(usGroup.getId());
            selRight.setName(usGroup.getName());
        }
    }  
    
    /* Событие изменения типа права в карточке права  */ 
    public void onTypeChangeRight(ValueChangeEvent event){
        selRight.setObjType((Integer) event.getNewValue());
        String name = EscomBeanUtils.getBandleLabel("EmptySelData");
        switch (selRight.getObjType()){
            case DictRights.TYPE_GROUP: {
                if (selUsGroup != null){
                    name = selUsGroup.getName();
                } 
                break;
            }
            case DictRights.TYPE_USER: {
                if (selUser != null){
                    name = selUser.getShortFIO();
                }
                break;
            }
        }
        selRight.setName(name);
    } 
    
    /* GET & SET */

    public List<User> getUsers() {
        if (users == null){
            users = userBean.findAll();
        }
        return users;
    }

    public List<UserGroups> getUserGroupses() {
        if (userGroupses == null){
            userGroupses = userGroupBean.findAll();
        }
        return userGroupses;
    }
    
    public Right getSelRight() {
        return selRight;
    }
    public void setSelRight(Right selRight) {
        this.selRight = selRight;
    }
    
    public User getSelUser() {
        return selUser;
    }
    public void setSelUser(User selUser) {
        this.selUser = selUser;
    }
    
    public UserGroups getSelUsGroup() {
        return selUsGroup;
    }
    public void setSelUsGroup(UserGroups selUsGroup) {
        this.selUsGroup = selUsGroup;
    }
}
