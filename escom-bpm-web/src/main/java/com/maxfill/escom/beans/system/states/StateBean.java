package com.maxfill.escom.beans.system.states;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.StateFacade;
import com.maxfill.model.states.State;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.inject.Named;
import java.util.List;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Состояния документа" */
@SessionScoped
@Named
public class StateBean implements Serializable{
    private static final long serialVersionUID = -3106225231045015183L;
    
    @EJB
    private StateFacade stateFacade;
    
    public List<State> findAll(){
        return stateFacade.findAll();
    }
    
    public StateFacade getItemFacade(){
        return stateFacade;
    }

    public String getBundleName(State state){
        if (state == null) return null;
        return EscomMsgUtils.getBandleLabel(state.getName());
    }
     
}