package com.maxfill.escom.beans.system.statuses;

import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.model.statuses.StatusesDoc;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.util.List;

/* Сервисный бин "Статусы документов" */
@SessionScoped
@Named
public class StatusesDocBean extends BaseTableBean<StatusesDoc, StatusesDoc>{
    private static final long serialVersionUID = 7864211951329104261L;
    
    @EJB
    private StatusesDocFacade itemFacade;     
            
    @Override
    public StatusesDocFacade getFacade() {
        return itemFacade;
    }
  
    @Override
    public BaseTableBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseTableBean getDetailBean() {
         return null;
    }
    
    @Override
    public List<StatusesDoc> getGroups(StatusesDoc item) {
        return null;
    }

    @Override
    public Class<StatusesDoc> getOwnerClass() {
        return null;
    }

}