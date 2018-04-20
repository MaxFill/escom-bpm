package com.maxfill.facade;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.TaskLog;
import com.maxfill.model.task.TaskStates;
import com.maxfill.model.users.User;

import javax.ejb.Stateless;

/**
 * Фасад для сущности "Поручения"
 */
@Stateless
public class TaskFacade extends BaseDictFacade<Task, User, TaskLog, TaskStates>{

    public TaskFacade() {
        super(Task.class, TaskLog.class, TaskStates.class);
    }

    @Override
    public Class <Task> getItemClass() {
        return Task.class;
    }

    @Override
    public int replaceItem(Task oldItem, Task newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 22;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.TASK.toLowerCase();
    }
}
