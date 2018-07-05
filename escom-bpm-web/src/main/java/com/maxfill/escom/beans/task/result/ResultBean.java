package com.maxfill.escom.beans.task.result;

import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.task.result.ResultFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.task.result.Result;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Сервисный бин "Результаты выполнения задачи" 
 * @author maksim
 */
@Named
@SessionScoped
public class ResultBean extends BaseTableBean<Result> {    
    private static final long serialVersionUID = -3352797318187287815L;

    @EJB
    private ResultFacade resultFacade;
    
    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getFacade() {
        return resultFacade;
    }
    
}
