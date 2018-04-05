package com.maxfill.escom.beans.system.rights;

import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import com.maxfill.model.states.State;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
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
        prepareRightForView(right);
        rightFacade.create(right);
        return right;
    }

    /* Подготовка списка прав для визуализации на форме */
    public void prepareRightsForView(List<Right> sourceRights){
        sourceRights.stream().forEach(right->prepareRightForView(right));
    }

    /* Подготовка права для визуализации на форме */
    private void prepareRightForView(Right right){
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
                accessorName = EscomMsgUtils.getBandleLabel("All");
            } else if(DictRights.GROUP_ADMIN_ID.equals(right.getObjId()) && right.getObjType().equals(DictRights.TYPE_GROUP)) {
                accessorName = EscomMsgUtils.getBandleLabel("Administrators");
            } else if(DictRights.USER_ADMIN_ID.equals(right.getObjId()) && right.getObjType().equals(DictRights.TYPE_USER)) {
                accessorName = EscomMsgUtils.getBandleLabel("Administrator");
            } else { //тогда ищем названия в базе
                accessorName = getAccessName(right);
            }
            right.setName(accessorName);
        }
    }

    /* Возвращает название пользователя или группы для которого назначаются права */ 
    private String getAccessName(Right rg) {
        String accessorName = "";
        if (rg.getObjType() == 1 ){
            accessorName = rightFacade.findUserById(rg.getObjId()).getShortFIO();          
        } else {
            accessorName = rightFacade.findGroupUserById(rg.getObjId()).getName();
        }
        return accessorName;
    } 

}
