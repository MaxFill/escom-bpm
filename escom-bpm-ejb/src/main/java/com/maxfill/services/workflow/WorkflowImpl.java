package com.maxfill.services.workflow;

import com.maxfill.facade.TaskFacade;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.utils.EscomUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
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
     * @param taskElem
     * @param scheme
     * @param errors 
     */
    @Override
    public void addTask(TaskElem taskElem, Scheme scheme, Set<String> errors){
        if (!errors.isEmpty()) return;
        if (taskElem == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getElements().getTasks().add(taskElem);
            if (taskElem != null && taskElem.getTask() != null){
                scheme.getTasks().add(taskElem.getTask());
            }
        }
    }

    /**
     * Добавление коннектора в схему процесса
     * @param connector
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
            scheme.getElements().getConnectors().add(connector);
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
            scheme.getElements().getConditions().add(condition);
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
            scheme.getElements().getLogics().add(logic);
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
            scheme.getElements().getStates().add(state);
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
            scheme.getElements().getStarts().add(start);
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
            scheme.getElements().getExits().add(exit);
        }
    }

    @Override
    public void removeElement(WorkflowConnectedElement element, Scheme scheme, Set <String> errors) {
        //ToDo проверка на возможность удаления
        if (element instanceof TaskElem){
            scheme.getElements().getTasks().remove((TaskElem)element);
        } else if (element instanceof StartElem){
            scheme.getElements().getStarts().remove((StartElem)element);
        } else if (element instanceof ExitElem){
            scheme.getElements().getExits().remove((ExitElem)element);
        } else if (element instanceof StateElem){
            scheme.getElements().getStates().remove((StateElem)element);
        } else if (element instanceof ConditionElem){
            scheme.getElements().getConditions().remove((ConditionElem)element);
        } else if (element instanceof LogicElem){
            scheme.getElements().getLogics().remove((LogicElem)element);
        }
        removeConnectors(element, scheme);        
    }
    
    @Override
    public void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors) {
        ConnectorElem connector = scheme.getElements().getConnectors().stream()
                .filter(c -> c.getFrom().equals(from) && c.getTo().equals(to))
                .findFirst().get();
        //ToDo проверка на возможность удаления данного соединения!
        if (connector == null){
            String message = MessageFormat.format("ImpossibleRemove", new Object[]{connector.toString()});
            errors.add(message);
        } else {
            scheme.getElements().getConnectors().remove(connector);
        }
    }

    @Override
    public void packScheme(Scheme scheme, Set <String> errors) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(scheme.getElements(), sw);
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
        try {
            String xml = EscomUtils.decompress(scheme.getPackElements());
            StringReader reader = new StringReader(xml);
            WorkflowElements elements = JAXB.unmarshal(reader, WorkflowElements.class);
            scheme.setElements(elements);            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void validateScheme(Scheme scheme, Set <String> errors) {
        //ToDo!
    }

    @Override
    public void saveTask(Scheme scheme, Set <String> errors) {
        scheme.getTasks().stream().forEach(task->taskFacade.create(task));
    }

    /**
     * Удаление соединений у элемента модели процесса
     * @param element
     * @param scheme
     */
    private void removeConnectors(WorkflowConnectedElement element, Scheme scheme){
        for (AnchorElem anchor : element.getAnchors()){ //сначала нужно удалить все соединения связанные с этим элементом!
            List<ConnectorElem> connectors = scheme.getElements().getConnectors().stream()
                    .filter(c -> c.getFrom().equals(anchor) || c.getTo().equals(anchor))
                    .collect(Collectors.toList());
            scheme.getElements().getConnectors().removeAll(connectors);
        }
    }
}
