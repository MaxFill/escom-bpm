package com.maxfill.escom.beans.task;

/* Сервисный бин "Поручения" */

import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.process.schemes.task.Task;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class TaskBean extends LazyLoadBean<Task>{
    private static final long serialVersionUID = -3502688297345485315L;

    @EJB
    private TaskFacade taskFacade;


    @Override
    protected BaseLazyLoadFacade getFacade() {
        return taskFacade;
    }

    @Override
    public String getFormName() {
        return null;
    }
}
