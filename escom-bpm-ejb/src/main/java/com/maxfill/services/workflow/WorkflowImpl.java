package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.task.Task;
import java.util.Set;
import javax.ejb.Stateless;

/**
 * Сервис реализует методы управления бизнес-процессами
 */
@Stateless
public class WorkflowImpl implements Workflow {
    
    /**
     * Добавление поручения в схему процесса
     * @param task
     * @param scheme
     * @param errors 
     */
    @Override
    public void addTask(Task task, Scheme scheme, Set<String> errors){

    }

    /**
     * Добавление коннектора в схему процесса
     * @param from
     * @param to
     * @param scheme
     * @param errors 
     */
    @Override
    public void addConnector(SchemeElement from, SchemeElement to, Scheme scheme, Set<String> errors) {
        
    }

}
