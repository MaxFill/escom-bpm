package com.maxfill.escom.beans.processes.elements;

import com.maxfill.model.basedict.process.conditions.ConditionFacade;
import com.maxfill.model.basedict.process.conditions.Condition;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Сервисный бин сущности "Условие процесса"
 */
@SessionScoped
@Named
public class ConditionBean implements Serializable{    
    private static final long serialVersionUID = 3800339371613792171L;
    
    @EJB
    private ConditionFacade conditionFacade;
    
    public ConditionFacade getItemFacade(){
        return conditionFacade;
    }
    
    /**
     * Возвращает список всех доступных условий для добавления их в схему процесса
     * @return 
     */
    public List<Condition> findAll(){
        return conditionFacade.findAll();
    } 
}