package com.maxfill.services.workflow;

import com.maxfill.facade.ProcessFacade;
import com.maxfill.facade.StateFacade;
import com.maxfill.facade.TaskFacade;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.states.State;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;
import com.maxfill.utils.EscomUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        } else {
            scheme.getElements().getTasks().put(taskElem.getUid(), taskElem);
            if (taskElem.getTask() != null){
                scheme.getTasks().add(taskElem.getTask());
            }
        }
    }

    /**
     * Создание элемента "Коннектор"
     * @param from
     * @param to
     * @param scheme
     * @param label
     * @param errors
     * @return 
     */
    @Override
    public ConnectorElem createConnector(AnchorElem from, AnchorElem to, Scheme scheme, String label, Set<String> errors){
        if (from == null || to == null){
            errors.add("WorkflowIncorrectData");
            return null;
        }
        
        if (findConnector(from, to, scheme) != null) return null;
        
        ConnectorElem connector = new ConnectorElem(label, from, to);        

        //ToDO проверить на возможность установки соединения!        
        scheme.getElements().getConnectors().add(connector);
        return connector;
    }
    
    @Override
    public void addCondition(ConditionElem condition, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (condition == null){
            errors.add("WorkflowIncorrectData");
        }
        //ToDo проверки!
        if (errors.isEmpty()) {
            scheme.getElements().getConditions().put(condition.getUid(), condition);
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
            scheme.getElements().getLogics().put(logic.getUid(), logic);
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
            scheme.getElements().getStates().put(state.getUid(), state);
        }
    }

    @Override
    public void addEnter(EnterElem enter, Scheme scheme, Set<String> errors) {
        if (!errors.isEmpty()) return;
        if (enter == null){
            errors.add("WorkflowIncorrectData");
        } else {
            scheme.getElements().getEnters().put(enter.getUid(), enter);
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
        } else {
            scheme.getElements().getExits().put(exit.getUid(), exit);
        }
    }

    @Override
    public void removeElement(WFConnectedElem element, Scheme scheme, Set <String> errors) {
        //ToDo проверка на возможность удаления
        if (element instanceof TaskElem){
            scheme.getElements().getTasks().remove(element.getUid());            
        } else if (element instanceof EnterElem){
            scheme.getElements().getEnters().remove(element.getUid());
        } else if (element instanceof ExitElem){
            scheme.getElements().getExits().remove(element.getUid());
        } else if (element instanceof StateElem){
            scheme.getElements().getStates().remove(element.getUid());
        } else if (element instanceof ConditionElem){
            scheme.getElements().getConditions().remove(element.getUid());
        } else if (element instanceof LogicElem){
            scheme.getElements().getLogics().remove(element.getUid());
        }
        removeConnectors(element, scheme);        
    }
    
    @Override
    public void removeConnector(AnchorElem from, AnchorElem to, Scheme scheme, Set <String> errors) {
        ConnectorElem connector = findConnector(from, to, scheme);
        //ToDo проверка на возможность удаления данного соединения!
        if (connector == null){
            String message = MessageFormat.format("ImpossibleRemove", new Object[]{connector.toString()});
            errors.add(message);
        } else {
            scheme.getElements().getConnectors().remove(connector);
        }
    }

    /**
     * Выполняет поиск коннектора
     * @param from
     * @param to
     * @param scheme
     * @return 
     */
    private ConnectorElem findConnector(AnchorElem from, AnchorElem to, Scheme scheme){
        return scheme.getElements().getConnectors().stream()
                .filter(c -> c.getFrom().equals(from) && c.getTo().equals(to))
                .findFirst()
                .orElse(null);
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
    public void unpackScheme(Scheme scheme, Set<String> errors) {
        try {
            String xml = EscomUtils.decompress(scheme.getPackElements());
            StringReader reader = new StringReader(xml);
            WorkflowElements elements = JAXB.unmarshal(reader, WorkflowElements.class);
            elements.getTasks().forEach((key, task)-> task.setTask(taskFacade.findByLinkUID(key)));
            scheme.setElements(elements);
        } catch (IOException ex) {
            errors.add("ErrorUnpackingProcessDiagram");
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void validateScheme(Scheme scheme, Set<String> errors) {       
        if (scheme.getElements().getExits().isEmpty()){
            errors.add("DiagramNotHaveExit");
        }
        StartElem startElem = scheme.getElements().getStartElem();
        if (startElem == null){
            errors.add("DiagramNotHaveStart");
        }
        if (scheme.getElements().getConnectors().isEmpty()){
            errors.add("DiagramNotHaveConnectors");
        }
        scheme.getElements().getTasks().entrySet().stream()
                .filter(rec->rec.getValue().getTask().getPlanExecDate() == null)
                .findFirst()
                .map(rec->errors.add("TasksNoHaveDeadline"));
        //ToDo! другие проверки!
    }

    /**
     * Удаление соединений у элемента модели процесса
     * @param element
     * @param scheme
     */
    private void removeConnectors(WFConnectedElem element, Scheme scheme){
        for (AnchorElem anchor : element.getAnchors()){ //сначала нужно удалить все соединения связанные с этим элементом!
            List<ConnectorElem> connectors = scheme.getElements().getConnectors().stream()
                    .filter(c -> c.getFrom().equals(anchor) || c.getTo().equals(anchor))
                    .collect(Collectors.toList());
            scheme.getElements().getConnectors().removeAll(connectors);
        }
    }

    /**
     * Выполнение задачи
     * @param task
     * @param result 
     * @param errors 
     */
    @Override
    public void executeTask(Task task, Result result, Set<String> errors){
        Scheme scheme = task.getScheme();
        unpackScheme(scheme, errors);
        if (errors.isEmpty()){
            WFConnectedElem startElement = scheme.getElements().getTasks().get(task.getTaskLinkUID());
            run(scheme, startElement, errors);
            if (errors.isEmpty()){
                task.setResult(result.getName());
                task.setIconName(result.getIconName());
                task.setFactExecDate(new Date());
                task.getState().setCurrentState(stateFacade.getCompletedState());
                taskFacade.edit(task);
            }
        }
    }
    
    @Override
    public void start(Process process, Set<String> errors) {        
        Scheme scheme = process.getScheme();
        validateScheme(scheme, errors);
        if (!errors.isEmpty()) return;
        
        scheme.getElements().getConnectors().forEach(c->c.setDone(false)); //сброс признака выполнения у всех коннекторов                
        run(scheme, scheme.getElements().getStartElem(), errors);
        if (errors.isEmpty()){                        
            State state = stateFacade.getRunningState();
            process.getState().setCurrentState(state);
            processFacade.edit(process);
        }
    }

    /**
     * Прерывание выполнения процесса
     * @param process
     * @param errors 
     */
    @Override
    public void stop(Process process, Set<String> errors) {
        State stateRun = stateFacade.getRunningState();
        State stateCancel = stateFacade.getCanceledState();
        //отмена всех запущенных задач
        process.getScheme().getTasks().stream()
                .filter(task->task.getState().getCurrentState().equals(stateRun))
                .forEach(task->task.getState().setCurrentState(stateCancel));        
        process.getState().setCurrentState(stateCancel);
        processFacade.edit(process);
    }
    
    private void finish(Process process) {
        State state = stateFacade.getCompletedState();
        process.getState().setCurrentState(state);
        processFacade.edit(process);
    }

    /**
     * Выполняет движение процесса по маршруту от указанного начального элемента
     * по всем исходящим из него переходам (connectors)
     * и останавливается после запуска всех актуальных задач и/или достижения конца процесса
     * @param scheme
     * @param startElement
     * @param errors 
     */
    @Override
    public void run(Scheme scheme, WFConnectedElem startElement, Set<String> errors) {   
        Set<Task> exeTasks = new HashSet<>();
        doRun(startElement.getAnchors(), scheme, exeTasks, errors);
        if (errors.isEmpty()){
            startTasks(exeTasks, scheme);            
            packScheme(scheme, errors);
        }
    }
    
    /**
     * Запуск задач на выполнение
     * @param tasks
     * @param scheme 
     */
    private void startTasks(Set<Task> tasks, Scheme scheme){
        if (!tasks.isEmpty()){
                tasks.stream()
                    .filter(task->!task.getState().getCurrentState().equals(stateFacade.getRunningState()))
                    .forEach(task->{
                        task.setBeginDate(new Date());
                        task.getState().setCurrentState(stateFacade.getRunningState());
                    });
                scheme.getTasks().removeAll(tasks);
                scheme.getTasks().addAll(tasks);
            }
    }
    
    /**
     * Обработка движения по исходящим соединениям 
     * Если процесс входит в задание, то оно запускается если оно не запущено, а если задание выполнено, то оно будет запущено повторно.
     * Если процесс входит в состояние, то выполняется код изменения состояния. всякий рах при входе
     * @param anchors
     * @param scheme
     * @param tasks
     * @param errors 
     */
    private void doRun(Set<AnchorElem> anchors, Scheme scheme, Set<Task> exeTasks, Set<String> errors){
        anchors.stream()
                .filter(anchor->anchor.isSource())
                .forEach(anchor->{
                    //получаем список коннекторов, исходящих из якоря
                    List<ConnectorElem> connectors = 
                            scheme.getElements().getConnectors().stream()
                            .filter(connector->connector.getFrom().equals(anchor) && !connector.isDone())                            
                            .map(connector->{
                                connector.setDone(true);
                                return connector;
                            })
                            .collect(Collectors.toList());
                    
                    //запуск задач
                    connectors.stream()
                        .map(connector->scheme.getElements().getTasks().get(connector.getTo().getOwnerUID()))
                        .filter(element->Objects.nonNull(element))
                        .forEach(taskElem-> exeTasks.add(taskElem.getTask()));
                    
                    //обрабатываем условия
                    Set<ConditionElem> targetConditions = findTargetConditions(connectors, scheme.getElements().getConditions());
                    //Todo
                    
                    //изменяем изменения состояния документа
                    Set<StateElem> targetStates = findTargetStates(connectors, scheme.getElements().getStates());
                    //Todo
                    
                    //обрабатываем логические элементы
                    Set<LogicElem> targetLogics = findTargetLogics(connectors, scheme.getElements().getLogics());
                    targetLogics.forEach(logic-> doRun(logic.getAnchors(), scheme, exeTasks, errors));
                    
                    //обработка выходов из процесса -> переход в связанный(е) процесс(ы)
                    Set<ExitElem> targetExits = findTargetExits(connectors, scheme.getElements().getExits());
                    //Todo!
                });
    }    
    
    /**
     * Находит элементы логики модели процесса к которым идут коннекторы
     * @return 
     */    
    private Set<LogicElem> findTargetLogics(List<ConnectorElem> connectors, Map<String, LogicElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }
    
    /**
     * Находит задачи модели процесса к которым идут коннекторы
     * @return 
     */    
    private Set<TaskElem> findTargetTasks(List<ConnectorElem> connectors, Map<String, TaskElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }
    
    /**
     * Находит выходы процесса к которым идут коннекторы
     * @return 
     */    
    private Set<ExitElem> findTargetExits(List<ConnectorElem> connectors, Map<String, ExitElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }
    
    /**
     * Находит условия процесса к которым идут коннекторы
     * @return 
     */    
    private Set<ConditionElem> findTargetConditions(List<ConnectorElem> connectors, Map<String, ConditionElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }
    
    /**
     * Находит состояния процесса к которым идут коннекторы
     * @return 
     */    
    private Set<StateElem> findTargetStates(List<ConnectorElem> connectors, Map<String, StateElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }    
    
}
