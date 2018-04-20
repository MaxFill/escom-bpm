package com.maxfill.escom.beans.task;

import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.facade.TaskFacade;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.task.Task;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Поручение"
 */
@Named
@ViewScoped
public class TaskCardBean extends BaseCardBean<Task>{
    private static final long serialVersionUID = -2860068605023348908L;

    @EJB
    private TaskFacade taskFacade;

    @Override
    protected BaseDictFacade getFacade() {
        return taskFacade;
    }
}
