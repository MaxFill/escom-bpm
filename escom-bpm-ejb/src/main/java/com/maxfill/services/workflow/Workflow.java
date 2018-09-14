package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.Process;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.users.User;

import javax.ejb.Local;
import java.util.Set;

@Local
public interface Workflow {
    void addTask(TaskElem task, Scheme scheme, Set<String> errors);
    ConnectorElem createConnector(AnchorElem from, AnchorElem to, Scheme scheme, String label, Set<String> errors);
    
    void addCondition(ConditionElem condition, Scheme scheme, Set<String> errors);
    void addTimer(TimerElem timer, Scheme scheme, Set<String> errors);
    void addLogic(LogicElem logic, Scheme scheme, Set<String> errors);
    void addState(StatusElem state, Scheme scheme, Set<String> errors);
    void addStart(StartElem start, Scheme scheme, Set<String> errors);
    void addEnter(EnterElem start, Scheme scheme, Set<String> errors);
    void addExit(ExitElem exit, Scheme scheme, Set<String> errors);
    void addMessage(MessageElem exit, Scheme scheme, Set<String> errors);
    void addProcedure(ProcedureElem exit, Scheme scheme, Set<String> errors);
    
    void removeElement(WFConnectedElem element, Scheme scheme, Set <String> errors);
    void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors);
    void packScheme(Scheme scheme);
    void unpackScheme(Scheme scheme);
    void validateScheme(Scheme scheme, Boolean checkTasks, Set<String> errors);
    void run(Process process, WFConnectedElem startElement, Set<String> errors, User currentUser);
    void stop(Process process, User user, Set<String> errors);
    void start(Process process, User user, Set<String> errors);
    void executeTask(Process process, Task task, Result result, User user, Set<String> errors);
    void replaceReportExecutor(Task task, User user);
}