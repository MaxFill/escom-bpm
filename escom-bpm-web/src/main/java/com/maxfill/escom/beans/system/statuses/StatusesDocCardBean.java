
package com.maxfill.escom.beans.system.statuses;

import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/* Карточка статуса документа */
@ViewScoped
@Named
public class StatusesDocCardBean extends BaseCardBean<StatusesDoc>{
    private static final long serialVersionUID = 3106765045991539220L;
    
    @EJB
    private StatusesDocFacade itemFacade;
   
    @Override
    public StatusesDocFacade getItemFacade() {
        return itemFacade;
    }

    @Override
    protected void afterCreateItem(StatusesDoc item) {        
    }

    @Override
    public Class<StatusesDoc> getItemClass() {
        return StatusesDoc.class;
    }
}