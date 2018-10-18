package com.maxfill.services.workflow;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.schemes.elements.AnchorElem;
import com.maxfill.model.basedict.process.schemes.elements.ExitElem;
import com.maxfill.model.basedict.process.schemes.elements.TaskElem;
import com.maxfill.model.basedict.process.schemes.elements.ConditionElem;
import com.maxfill.model.basedict.process.schemes.elements.ConnectorElem;
import com.maxfill.model.basedict.process.schemes.elements.LogicElem;
import com.maxfill.model.basedict.process.schemes.elements.TimerElem;
import com.maxfill.model.basedict.process.schemes.elements.WFConnectedElem;
import com.maxfill.model.basedict.process.schemes.elements.ProcedureElem;
import com.maxfill.model.basedict.process.schemes.elements.StatusElem;
import com.maxfill.model.basedict.process.schemes.elements.MessageElem;
import com.maxfill.model.basedict.process.schemes.elements.EnterElem;
import com.maxfill.model.basedict.process.schemes.elements.StartElem;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.schemes.elements.SubProcessElem;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.user.User;
import java.util.Map;

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
    void addSubProcess(SubProcessElem subProcessElem, Scheme scheme, Set<String> errors);
    
    void removeElement(WFConnectedElem element, Scheme scheme, Set <String> errors);
    void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors);
    void packScheme(Scheme scheme);
    void unpackScheme(Scheme scheme);
    void validateScheme(Scheme scheme, Boolean checkTasks, Set<String> errors);
    void clearScheme(Scheme scheme);
    
    Set<BaseDict> run(Process process, WFConnectedElem startElement, Set<SubProcessElem> exeSubProc, User currentUser, Map<String, Object> params, Set<String> errors);
    void stop(Process process, User user, Set<String> errors);
    Set<BaseDict> start(Process process, User user, Map<String, Object> params, Set<String> errors);
    
    Set<BaseDict> executeTask(Process process, Task task, Result result, User user, Map<String, Object> params, Set<String> errors);
    void executeTimer(ProcTimer procTimer, Set<String> errors);
    
    void makeProcessReport(Process process, User user);
}