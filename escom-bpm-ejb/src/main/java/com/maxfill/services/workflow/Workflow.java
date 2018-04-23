package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.elements.Condition;
import com.maxfill.model.process.schemes.elements.Connector;
import com.maxfill.model.process.schemes.elements.Logic;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.model.process.schemes.elements.State;
import java.util.Set;
import javax.ejb.Local;

@Local
public interface Workflow {
    void addTask(Task task, Scheme scheme, Set<String> errors);
    void addConnector(Connector connector, SchemeElement from, SchemeElement to, Scheme scheme, Set<String> errors);
    void addCondition(Condition condition, Scheme scheme, Set<String> errors);
    void addLogic(Logic logic, Scheme scheme, Set<String> errors);
    void addState(State state, Scheme scheme, Set<String> errors);
}