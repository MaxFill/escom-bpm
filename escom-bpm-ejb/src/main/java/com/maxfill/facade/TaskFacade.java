package com.maxfill.facade;

import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.process.schemes.task.Task;

import javax.ejb.Stateless;

/**
 * Фасад для сущности "Поручения"
 */
@Stateless
public class TaskFacade extends BaseLazyLoadFacade<Task>{

    public TaskFacade() {
        super(Task.class);
    }

}
