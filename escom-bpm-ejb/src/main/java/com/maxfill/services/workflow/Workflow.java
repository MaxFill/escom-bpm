package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.schemes.task.Task;

import javax.ejb.Local;
import java.util.Set;

@Local
public interface Workflow {
    void addTask(Task task, Scheme scheme, Set<String> errors);
    void addConnector(ConnectorElem connector, Scheme scheme, Set<String> errors);
    void addCondition(ConditionElem condition, Scheme scheme, Set<String> errors);
    void addLogic(LogicElem logic, Scheme scheme, Set<String> errors);
    void addState(StateElem state, Scheme scheme, Set<String> errors);
    void addStart(StartElem start, Scheme scheme, Set<String> errors);
    void addExit(ExitElem exit, Scheme scheme, Set<String> errors);
    void removeElement(WorkflowConnectedElement element, Scheme scheme, Set <String> errors);
    void packScheme(Scheme scheme);
    void unpackScheme(Scheme scheme);
    void validateScheme(Scheme scheme, Set<String> errors);
}