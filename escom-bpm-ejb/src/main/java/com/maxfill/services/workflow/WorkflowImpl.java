package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.schemes.task.Task;

import javax.ejb.Stateless;
import java.util.Set;

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
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getTasks().add(task);
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
    public void addConnector(ConnectorElem connector, BaseElement from, BaseElement to, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (from == null){
            errors.add("WorkflowIncorrectData");
        }
        if (to == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDO проверить на возможность установки соединения!
        if (errors.isEmpty()) {
            scheme.getConnectors().add(connector);
        }
    }

    @Override
    public void addCondition(ConditionElem condition, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (condition == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getConditions().add(condition);
        }
    }

    @Override
    public void addLogic(LogicElem logic, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (logic == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getLogics().add(logic);
        }
    }

    @Override
    public void addState(StateElem state, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (state == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getStates().add(state);
        }
    }

    @Override
    public void addStart(StartElem start, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (start == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getStarts().add(start);
        }
    }

    @Override
    public void addExit(ExitElem exit, Scheme scheme, Set <String> errors) {
        if (!errors.isEmpty()) return;
        if (exit == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getExits().add(exit);
        }
    }

    @Override
    public void removeElement(BaseElement element, Scheme scheme, Set <String> errors) {

    }

    @Override
    public void packScheme(Scheme scheme) {

    }

    @Override
    public void unpackScheme(Scheme scheme) {

    }

    @Override
    public void validateScheme(Scheme scheme, Set <String> errors) {

    }

}
