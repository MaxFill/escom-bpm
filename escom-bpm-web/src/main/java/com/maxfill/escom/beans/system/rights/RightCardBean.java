package com.maxfill.escom.beans.system.rights;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.rights.Right;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.states.State;
import com.maxfill.facade.StateFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.beans.system.states.StateBean;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.beans.users.groups.UserGroupsBean;
import com.maxfill.model.metadates.Metadates;
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
import org.apache.commons.lang3.StringUtils;

/* Контролер формы "Карточка прав доступа" */
@Named
@ViewScoped
public class RightCardBean extends BaseDialogBean{    
    private static final long serialVersionUID = -3720038615067813059L;

    @Inject
    private UserBean userBean;
    @Inject
    private UserGroupsBean userGroupBean;    
    @Inject
    private RightsBean rightsBean;    
    @Inject
    private StateBean stateBean;
    
    @EJB
    private StateFacade stateFacade;
    
    private Right selRight;
    private Right sourceRight;
    private User selUser;
    private UserGroups selUsGroup;
    private UserGroups selUsRole;
    private Integer editMode;
    private String keyRight;
    private List<User> users;
    private List<UserGroups> userGroupses; 
    private List<UserGroups> roles;
    private Boolean showCreateRight;
    private Integer objType;

    @Override
    protected void initBean(){    
    }
    
    /* При открытии карточки права */
    @Override
    public void onBeforeOpenCard(){
        if (selRight == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            editMode = Integer.valueOf(params.get("editMode"));
            showCreateRight = Boolean.valueOf(params.get("showCreate"));
            switch (editMode){
                case DictEditMode.INSERT_MODE:{
                    Integer stateId = Integer.valueOf(params.get("stateId"));
                    State state = stateFacade.find(stateId);
                    selRight = new Right(DictRights.TYPE_GROUP, null, null, "<Не указано!>", state);
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

            objType = selRight.getObjType();
            switch (objType){
                case DictRights.TYPE_GROUP: {  
                    selUser = null;
                    selUsRole = null;
                    if (selRight.getObjId() != null){
                        selUsGroup = userGroupBean.findItem(selRight.getObjId());
                    }
                    break;
                }
                case DictRights.TYPE_USER: {
                    if (selRight.getObjId() != null){
                        selUser = userBean.findItem(selRight.getObjId());
                    }    
                    selUsGroup = null;
                    selUsRole = null;
                    break;
                }
                case DictRights.TYPE_ROLE: {
                    selUser = null;
                    selUsGroup = null;
                    if (selRight.getObjId() != null){
                        selUsRole = userGroupBean.findItem(selRight.getObjId());
                    }    
                    break;
                }
            }
        }
    }
    
    public String onSaveChangeRight() {        
        if (isItemChange()) {
            selRight.setObjType(objType);
            String name = EscomMsgUtils.getBandleLabel("EmptySelData");
            switch (objType){
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
                case DictRights.TYPE_ROLE: {
                    if (selUsRole != null){
                        name = selUsRole.getName();
                    }
                    break;
                }
            }
            selRight.setName(name);

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
        getSelRight().setAddChild(newValue);
    }
    
    /* Событие изменения пользователя в карточке права */ 
    public void onUserChange(ValueChangeEvent event){
        User user = (User) event.getNewValue();
        if (user != null){
            selRight.setName(user.getShortFIO());
            selRight.setObjId(user.getId());
        }
    }
    
    /* Событие изменение группы на карточке права */ 
    public void onGroupChange(ValueChangeEvent event){     
        UserGroups usGroup = (UserGroups) event.getNewValue();
        if (usGroup != null){
            selRight.setObjId(usGroup.getId());
            selRight.setName(usGroup.getName());
        }
    }  
    
    /* Событие изменение группы на карточке права */ 
    public void onRoleChange(ValueChangeEvent event){     
        UserGroups usRole = (UserGroups) event.getNewValue();
        if (usRole != null){
            selRight.setObjId(usRole.getId());
            selRight.setName(usRole.getName());
        }
    } 
    
    /* Событие изменения типа права в карточке права  */
    /*
    public void onTypeChangeRight(ValueChangeEvent event){
        if (event.getNewValue() == null) return;
        selRight.setObjType((Integer) event.getNewValue());
        String name = EscomMsgUtils.getBandleLabel("EmptySelData");
        switch (selRight.getObjType()){
            case DictRights.TYPE_GROUP: {
                if (selUsGroup != null){
                    name = selUsGroup.getName();
                    selUser = null;
                    selUsRole = null;
                } 
                break;
            }
            case DictRights.TYPE_USER: {
                if (selUser != null){
                    selUsRole = null;
                    selUsGroup = null;
                    name = selUser.getShortFIO();
                }
                break;
            }
            case DictRights.TYPE_ROLE: {
                if (selUsRole != null){
                    selUsGroup = null;
                    selUser = null;
                    name = selUsRole.getName();
                }
                break;
            }
        }
        selRight.setName(name);
    } 
    */
    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_RIGHT_CARD;
    }
    
    public String getTypeName(){
        return rightsBean.getTypeName(selRight.getObjType());
    }
    
    public String getStateName(){
        return stateBean.getBundleName(selRight.getState());
    }
    
    public String getObjName(Metadates metadate){
        if (metadate == null || StringUtils.isBlank(metadate.getBundleName())) return null;
        return EscomMsgUtils.getBandleLabel(metadate.getBundleName());
    }

    /* GETS & SETS */

    public Integer getObjType() {
        return objType;
    }
    public void setObjType(Integer objType) {
        this.objType = objType;
    }

    public List<User> getUsers() {
        if (users == null){
            users = userBean.findAll();
        }
        return users;
    }

    public List<UserGroups> getUserGroupses() {
        if (userGroupses == null){
            userGroupses = userGroupBean.findOnlyGroups();
        }
        return userGroupses;
    }
    
    public List<UserGroups> getRoles() {
        if (roles == null){
            roles = userGroupBean.findOnlyRoles();
        }
        return roles;
    }

    public Boolean isShowCreateRight() {
        return showCreateRight;
    }

    public Right getSelRight() {
        return selRight;
    }
    public void setSelRight(Right selRight) {
        this.selRight = selRight;
    }

    public UserGroups getSelUsRole() {
        return selUsRole;
    }
    public void setSelUsRole(UserGroups selUsRole) {
        this.selUsRole = selUsRole;
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
