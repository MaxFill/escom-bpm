package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.elements.*;
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
        if (!errors.isEmpty()) return;
        if (task == null){
            errors.add("WorkflowIncorrectData");
        }
        if (errors.isEmpty()) {
            //сохранить поручение в схему!
        }
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
        if (!errors.isEmpty()) return;
        if (from == null){
            errors.add("WorkflowIncorrectData");
        }
        if (to == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo
        //проверить на возможность установки соединения!
        if (errors.isEmpty()) {
            //сохранить коннектор в схему!
        }
    }

    @Override
    public void addCondition(Condition condition, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (condition == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo
        if (errors.isEmpty()) {
            //сохранить условие в схему!
        }
    }

    @Override
    public void addLogic(Logic logic, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (logic == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo
        if (errors.isEmpty()) {
            //сохранить логическое ветвление в схему!
        }
    }

    @Override
    public void addState(State state, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (state == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo
        if (errors.isEmpty()) {
            //сохранить состояние в схему!
        }
    }

    @Override
    public void addStart(Start start, Scheme scheme, Set<String> errors) {
        //ToDo
    }

}
