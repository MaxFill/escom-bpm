package com.maxfill.escom.beans.system.statuses;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.statuses.StatusesDoc;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/* Сервисный бин "Статусы документов" */
@SessionScoped
@Named
public class StatusesDocBean extends BaseTableBean<StatusesDoc>{
    private static final long serialVersionUID = 7864211951329104261L;
    
    @EJB
    private StatusesDocFacade itemFacade;     
            
    @Override
    public StatusesDocFacade getLazyFacade() {
        return itemFacade;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
         return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }
}