package com.maxfill.services.workflow;

import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StateFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task;
import com.maxfill.utils.EscomUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private StateFacade stateFacade;
    
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
    public void addEnter(EnterElem enter, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (enter == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getElements().getEnters().add(enter);
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
            scheme.getElements().setStartElem(start);
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
    public void removeElement(WFConnectedElement element, Scheme scheme, Set <String> errors) {
        //ToDo проверка на возможность удаления
        if (element instanceof TaskElem){
            scheme.getElements().getTasks().remove((TaskElem)element);
        } else if (element instanceof StartElem){
            scheme.getElements().getEnters().remove((EnterElem)element);
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
            elements.getTasks().forEach(task->task.setTask(taskFacade.findByLinkUID(task.getUid())));            
            scheme.setElements(elements);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void validateScheme(Scheme scheme, Set <String> errors) {
        List<ExitElem> exitElems = scheme.getElements().getExits();
        if (exitElems.isEmpty()){
            errors.add("DiagramNotHaveExit");
        }
        StartElem startElem = scheme.getElements().getStartElem();
        if (startElem == null){
            errors.add("DiagramNotHaveStart");
        }
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
    private void removeConnectors(WFConnectedElement element, Scheme scheme){
        for (AnchorElem anchor : element.getAnchors()){ //сначала нужно удалить все соединения связанные с этим элементом!
            List<ConnectorElem> connectors = scheme.getElements().getConnectors().stream()
                    .filter(c -> c.getFrom().equals(anchor) || c.getTo().equals(anchor))
                    .collect(Collectors.toList());
            scheme.getElements().getConnectors().removeAll(connectors);
        }
    }

    @Override
    public void start(Process process, Set<String> errors) {        
        Scheme scheme = process.getScheme();
        run(scheme, scheme.getElements().getStartElem(), errors);
        if (errors.isEmpty()){
            State state = stateFacade.getRunningState();
            process.getState().setCurrentState(state);
            processFacade.edit(process);
        }
    }

    @Override
    public void stop(Process process, Set<String> errors) {
        State state = stateFacade.getCanceledState();
        process.getState().setCurrentState(state);
        processFacade.edit(process);
    }
    
    private void finish(Process process) {
        State state = stateFacade.getCompletedState();
        process.getState().setCurrentState(state);
        processFacade.edit(process);
    }

    /**
     * Выполняет движение процесса по маршруту
     * Движение начинается от указанного начального элемента
     * и останавливается после запуска актуальных задач или достижения конца процесса
     * @param scheme
     * @param startElement
     * @param errors 
     */
    @Override
    public void run(Scheme scheme, WFConnectedElement startElement, Set<String> errors) {
        Set<Task> tasks = new HashSet<>();
        doMove(startElement.getAnchors(), scheme, tasks, errors);
        startTasks(tasks);
    }
    
    private void doMove(Set<AnchorElem> anchors, Scheme scheme, Set<Task> tasks, Set<String> errors){
        anchors.stream()
                .filter(anchor->anchor.isSource())
                .forEach(anchor->{
                    Set<WFConnectedElement> targetElements = findTargetElement(anchor, scheme.getElements().getConnectors(), getMapElements(scheme));
                });
    }
    
    private Map<String, WFConnectedElement> getMapElements(Scheme scheme){
        Map<String, WFConnectedElement> elements = new HashMap<>();
        scheme.getElements().getConditions().forEach(el-> elements.put(el.getUid(), el));
        scheme.getElements().getEnters().forEach(el-> elements.put(el.getUid(), el));
        scheme.getElements().getExits().forEach(el-> elements.put(el.getUid(), el));
        scheme.getElements().getTasks().forEach(el-> elements.put(el.getUid(), el));
        scheme.getElements().getLogics().forEach(el-> elements.put(el.getUid(), el));
        scheme.getElements().getStates().forEach(el-> elements.put(el.getUid(), el));
        return elements;
    }
    
    /**
     * Находит целевые элементы модели процесса по исходящему якорю
     * @param sourceAnchor
     * @return 
     */
    private Set<WFConnectedElement> findTargetElement(AnchorElem sourceAnchor, List<ConnectorElem> connectors, Map<String, WFConnectedElement> elements){
        return connectors.stream()
                .filter(connector->connector.getFrom().equals(sourceAnchor))
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .collect(Collectors.toSet());
    }
    
    /**
     * Отправляет задачи в работу
     * @param tasks 
     */
    private void startTasks(Set<Task> tasks){
        tasks.stream().forEach(task->{
            task.setBeginDate(new Date());
            task.getState().setCurrentState(stateFacade.getRunningState());
            taskFacade.edit(task);
        });
    }
    
}
