package com.maxfill.escom.beans.system.rights;

import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.RightFacade;
import com.maxfill.facade.UserFacade;
import com.maxfill.facade.treelike.UserGroupsFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import com.maxfill.model.states.State;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Сервисный бин для работы с правами доступа
 */
@Named
@SessionScoped
public class RightsBean implements Serializable{
    private static final long serialVersionUID = -1448442298980350090L;
    
    @EJB
    private RightFacade rightFacade;
    @EJB
    private UserGroupsFacade userGroupsFacade;
    @EJB
    private UserFacade userFacade;

    /**
     * Создание нового права доступа
     * @param typeObj
     * @param objId
     * @param name
     * @param state
     * @param metadateObj
     * @return
     */
    public Right createRight(Integer typeObj, Integer objId, String name, State state, Metadates metadateObj){
        Right right = new Right(typeObj, objId, name, state, metadateObj);
        prepareRightForView(right, new ArrayList <>());
        rightFacade.create(right);
        return right;
    }

    /* Подготовка списка прав для визуализации на форме */
    public void prepareRightsForView(List<Right> sourceRights){
        List<Right> incorrects =  new ArrayList <>();
        sourceRights.stream().forEach(right->prepareRightForView(right, incorrects));
        sourceRights.removeAll(incorrects);
    }

    /* Подготовка права для визуализации на форме */
    private void prepareRightForView(Right right, List<Right> incorrects){
        String accessorName = right.getName();
        switch (right.getObjType()){
            case (DictRights.TYPE_GROUP):{
                right.setIcon("folder_open20");
                break;
            }
            case (DictRights.TYPE_ROLE):{
                right.setIcon("roles20");
                break;
            }
            case (DictRights.TYPE_USER):{
                right.setIcon("user20");
                break;
            }
        }
        //хардкодные проверки
        if (StringUtils.isBlank(accessorName)) {
            if(right.getObjId() == 0 && right.getObjType() == DictRights.TYPE_GROUP) {
                accessorName = MsgUtils.getBandleLabel("All");
            } else if(DictRights.GROUP_ADMIN_ID.equals(right.getObjId()) && right.getObjType().equals(DictRights.TYPE_GROUP)) {
                accessorName = MsgUtils.getBandleLabel("Administrators");
            } else if(DictRights.USER_ADMIN_ID.equals(right.getObjId()) && right.getObjType().equals(DictRights.TYPE_USER)) {
                accessorName = MsgUtils.getBandleLabel("Administrator");
            } else { //тогда ищем названия в базе
                if (right.getObjType() == DictRights.TYPE_USER ){
                    User user = userFacade.find(right.getObjId());
                    if (user != null) {
                        accessorName = user.getShortFIO();
                    }else {
                        incorrects.add(right);
                    }
                } else { //значит это роль или группа
                    UserGroups userGroups = userGroupsFacade.find(right.getObjId());
                    if (userGroups != null) {
                        accessorName = userGroups.getName();
                    } else {
                        incorrects.add(right);
                    }
                }
            }
            right.setName(accessorName);
        }
    }

}
