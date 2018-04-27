package com.maxfill.services.workflow;

import com.maxfill.facade.TaskFacade;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.schemes.task.Task;
import com.maxfill.utils.EscomUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Сервис реализует методы управления бизнес-процессами
 */
@Stateless
public class WorkflowImpl implements Workflow {
    protected static final Logger LOGGER = Logger.getLogger(WorkflowImpl.class.getName());

    @EJB
    private TaskFacade taskFacade;

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
    public void addConnector(ConnectorElem connector, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (connector.getFrom() == null){
            errors.add("WorkflowIncorrectData");
        }
        if (connector.getTo() == null){
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
            scheme.getElements().add(condition);
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
            scheme.getElements().add(logic);
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
            scheme.getElements().add(state);
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
            scheme.getElements().add(start);
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
            scheme.getElements().add(exit);
        }
    }

    @Override
    public void removeElement(WorkflowConnectedElement element, Scheme scheme, Set <String> errors) {
        //ToDo проверка на возможность удаления данного соединения!

        for (AnchorElem anchor : element.getAnchors()){ //сначала нужно удалить все соединения связанные с этим элементом!
            List<ConnectorElem> connectors = scheme.getConnectors().stream()
                    .filter(c -> c.getFrom().equals(anchor) || c.getTo().equals(anchor))
                    .collect(Collectors.toList());
            scheme.getConnectors().removeAll(connectors);
        }
        scheme.getElements().remove(element);
    }

    @Override
    public void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors) {
        ConnectorElem connector = scheme.getConnectors().stream()
                .filter(c -> c.getFrom().equals(from) && c.getTo().equals(to))
                .findFirst().get();
        //ToDo проверка на возможность удаления данного соединения!
        if (connector == null){
            String message = MessageFormat.format("ImpossibleRemove", new Object[]{connector.toString()});
            errors.add(message);
        } else {
            scheme.getConnectors().remove(connector);
        }
    }

    @Override
    public void packScheme(Scheme scheme, Set <String> errors) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(scheme.getWorkflowElements().getElements().get(0), sw);
        StringWriter sw1 = new StringWriter();
        JAXB.marshal(scheme.getWorkflowElements(), sw1);
        String xml = sw.toString();
        try {
            byte[] compress = EscomUtils.compress(xml);
            scheme.setPackElements(compress);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void unpackScheme(Scheme scheme, Set <String> errors) {

    }

    @Override
    public void validateScheme(Scheme scheme, Set <String> errors) {
        //ToDo!
    }

    @Override
    public void saveTask(Scheme scheme, Set <String> errors) {
        scheme.getTasks().stream().forEach(task->taskFacade.create(task));
    }

}
