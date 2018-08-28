package com.maxfill.escom.beans.processes.remarks;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.model.process.remarks.Remark;
import com.maxfill.model.process.remarks.RemarkFacade;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.inject.Inject;

/* Сервисный бин "Замечания" */
@Named
@SessionScoped
public class RemarkBean extends BaseTableBean<Remark>{
    private static final long serialVersionUID = -257932838724865134L;
    
    @Inject
    private ProcessBean processBean;
    
    @EJB
    private RemarkFacade itemFacade;   
        
    @Override
    public RemarkFacade getFacade() {
        return itemFacade;
    }            

    @Override
    public BaseDetailsBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return processBean;
    }
}
