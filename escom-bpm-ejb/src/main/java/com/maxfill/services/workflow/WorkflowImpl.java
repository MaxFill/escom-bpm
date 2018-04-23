package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.elements.Condition;
import com.maxfill.model.process.schemes.elements.Connector;
import com.maxfill.model.process.schemes.elements.Logic;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.process.schemes.elements.State;
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
     * @param connector
     * @param from
     * @param to
     * @param scheme
     * @param errors 
     */
    @Override
    public void addConnector(Connector connector, SchemeElement from, SchemeElement to, Scheme scheme, Set<String> errors) {
        
    }

    @Override
    public void addCondition(Condition condition, Scheme scheme, Set<String> errors) {        
    }

    @Override
    public void addLogic(Logic logic, Scheme scheme, Set<String> errors) {        
    }

    @Override
    public void addState(State state, Scheme scheme, Set<String> errors) {        
    }

}
