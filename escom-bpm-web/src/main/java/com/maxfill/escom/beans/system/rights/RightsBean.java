package com.maxfill.escom.beans.system.rights;

import com.maxfill.dictionary.DictRights;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.RightFacade;
import com.maxfill.model.rights.Right;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class RightsBean implements Serializable{
    private static final long serialVersionUID = -1448442298980350090L;
    
    @EJB
    private RightFacade rightFacade;
    
    /* Подготовка прав для визуализации на форме */
    public void prepareRightsForView(List<Right> sourceRights){
        String accessorName;
        for (Right rg: sourceRights){
            switch (rg.getObjType()){
                case (DictRights.TYPE_GROUP):{
                    rg.setIcon("folder_open20");
                    break;
                }
                case (DictRights.TYPE_ROLE):{
                    rg.setIcon("roles20");
                    break;
                }
                case (DictRights.TYPE_USER):{
                    rg.setIcon("user20");
                    break;
                }
            }
            //хардкодные проверки
            if (rg.getObjId() == 0 && rg.getObjType() == DictRights.TYPE_GROUP){
               accessorName = EscomMsgUtils.getBandleLabel("All");
            } else
                if (DictRights.GROUP_ADMIN_ID.equals(rg.getObjId()) && rg.getObjType().equals(DictRights.TYPE_GROUP)){
                  accessorName = EscomMsgUtils.getBandleLabel("Administrators");
                } else
                    if (DictRights.USER_ADMIN_ID.equals(rg.getObjId()) && rg.getObjType().equals(DictRights.TYPE_USER)){
                        accessorName = EscomMsgUtils.getBandleLabel("Administrator");
                      } else{ //тогда ищем названия в базе
                            accessorName = getAccessName(rg);
                        }
            rg.setName(accessorName);
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
    
    // Формирует именя типа для отображения на карточке
    public String getTypeName(Integer objType){         
        switch (objType){
            case (DictRights.TYPE_GROUP):{  
                return EscomMsgUtils.getBandleLabel("RightForGroup");
            }
            case (DictRights.TYPE_USER): {
                return EscomMsgUtils.getBandleLabel("RightForUser");
            }
            case (DictRights.TYPE_ROLE): {
                return EscomMsgUtils.getBandleLabel("RightForRole");
            }
        }   
        return "";
    }
}
