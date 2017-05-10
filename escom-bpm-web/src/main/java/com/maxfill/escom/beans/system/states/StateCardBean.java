package com.maxfill.escom.beans.system.states;

import com.maxfill.facade.StateFacade;
import com.maxfill.model.states.State;
import com.maxfill.escom.beans.BaseCardBean;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Бин для состояний документа
 * @author mfilatov
 */
@ViewScoped
@Named
public class StateCardBean extends BaseCardBean<State>{
    private static final long serialVersionUID = 8416092044901389340L;

    @EJB
    private StateFacade docsStateFacade;

    @Override
    public StateFacade getItemFacade() {
        return docsStateFacade;
    }

    @Override
    protected void onAfterCreateItem(State item) {        
    }

    @Override
    public Class<State> getItemClass() {
        return State.class;
    }
}