package com.maxfill.services.workflow;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import com.maxfill.model.process.schemes.task.Task;
import java.util.Set;
import javax.ejb.Local;

@Local
public interface Workflow {
    void addTask(Task task, Scheme scheme, Set<String> errors);
    void addConnector(SchemeElement from, SchemeElement to, Scheme scheme, Set<String> errors);
}