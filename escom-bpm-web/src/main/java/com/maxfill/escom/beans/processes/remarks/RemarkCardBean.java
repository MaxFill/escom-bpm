package com.maxfill.escom.beans.processes.remarks;

import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.model.process.remarks.RemarkFacade;

import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/**
 * Контролер карточки "Замечание"
 */
@Named
@ViewScoped
public class RemarkCardBean extends BaseCardBean<Post>{
    private static final long serialVersionUID = -6399475562664755663L;
    
    @EJB
    private RemarkFacade itemsFacade;
        
    @Override
    public RemarkFacade getFacade() {        
        return itemsFacade;
    }

}