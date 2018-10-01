package com.maxfill.escom.beans.task;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.task.Task;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Сервисный бин для "Задачи"
 * @author maksim
 */

@Named
@SessionScoped
public class TaskBean extends BaseTableBean<Task>{
    private static final long serialVersionUID = -339599488264482676L;

    @EJB
    private TaskFacade taskFacade;
    
    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getLazyFacade() {
        return taskFacade;
    }
    
    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }
}
