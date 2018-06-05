package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.Process;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;

import javax.ejb.Local;
import java.util.Set;

@Local
public interface Workflow {
    void addTask(TaskElem task, Scheme scheme, Set<String> errors);
    ConnectorElem createConnector(AnchorElem from, AnchorElem to, Scheme scheme, String label, Set<String> errors);
    void addCondition(ConditionElem condition, Scheme scheme, Set<String> errors);
    void addLogic(LogicElem logic, Scheme scheme, Set<String> errors);
    void addState(StatusElem state, Scheme scheme, Set<String> errors);
    void addStart(StartElem start, Scheme scheme, Set<String> errors);
    void addEnter(EnterElem start, Scheme scheme, Set<String> errors);
    void addExit(ExitElem exit, Scheme scheme, Set<String> errors);
    void removeElement(WFConnectedElem element, Scheme scheme, Set <String> errors);
    void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors);
    void packScheme(Scheme scheme, Set <String> errors);
    void unpackScheme(Scheme scheme, Set <String> errors);
    void validateScheme(Scheme scheme, Set<String> errors);
    void run(Scheme scheme, WFConnectedElem startElement, Set<String> errors);
    void stop(Process process, Set<String> errors);
    void start(Process process, Set<String> errors);
    void executeTask(Task task, Result result, Set<String> errors);
}