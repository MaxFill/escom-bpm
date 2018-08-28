package com.maxfill.services.workflow;

import com.maxfill.Configuration;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictReportStatuses;
import com.maxfill.dictionary.DictResults;
import com.maxfill.model.process.conditions.ConditionFacade;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.remarks.Remark;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.process.timers.ProcTimerFacade;
import com.maxfill.model.states.State;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.users.User;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.ItemUtils;

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
import org.apache.commons.collections.CollectionUtils;

/**
 * Сервис реализует методы управления бизнес-процессами
 */
@Stateless
public class WorkflowImpl implements Workflow {
    protected static final Logger LOGGER = Logger.getLogger(WorkflowImpl.class.getName());

    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private StateFacade stateFacade;
    @EJB
    private ConditionFacade conditionFacade;
    @EJB
    private StatusesDocFacade statusesFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private NotificationService notificationService;
    @EJB
    private Configuration config;
    @EJB
    private ProcTimerFacade procTimerFacade;
    
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
            return;
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
    public void addTimer(TimerElem timer, Scheme scheme, Set<String> errors) {
        if (timer == null){
            errors.add("WorkflowIncorrectData");
        }
        if (!errors.isEmpty()) return;        
        //ToDo проверки!        
        scheme.getElements().getTimers().put(timer.getUid(), timer);
        if (timer.getProcTimer() != null){
            scheme.getTimers().add(timer.getProcTimer());
        }       
    }
    
    @Override
    public void addMessage(MessageElem elem, Scheme scheme, Set<String> errors) {
        if (elem == null){
            errors.add("WorkflowIncorrectData");
        }
        if (!errors.isEmpty()) return;        
        //ToDo проверки!        
        scheme.getElements().getMessages().put(elem.getUid(), elem);      
    }
    
    @Override
    public void addProcedure(ProcedureElem elem, Scheme scheme, Set<String> errors) {
        if (elem == null){
            errors.add("WorkflowIncorrectData");
        }
        if (!errors.isEmpty()) return;        
        //ToDo проверки!        
        scheme.getElements().getProcedures().put(elem.getUid(), elem);      
    }
    
    @Override
    public void addState(StatusElem state, Scheme scheme, Set<String> errors) {
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
        } else if (element instanceof StatusElem){
            scheme.getElements().getStates().remove(element.getUid());
        } else if (element instanceof ConditionElem){
            scheme.getElements().getConditions().remove(element.getUid());
        } else if (element instanceof LogicElem){
            scheme.getElements().getLogics().remove(element.getUid());
        } else if (element instanceof TimerElem){
            scheme.getElements().getTimers().remove(element.getUid());
        } else if (element instanceof ProcedureElem){
            scheme.getElements().getProcedures().remove(element.getUid());
        } else if (element instanceof MessageElem){
            scheme.getElements().getMessages().remove(element.getUid());
        }
        removeConnectors(element, scheme);        
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
    public void packScheme(Scheme scheme) {
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
    public void unpackScheme(Scheme scheme) {
        try {
            String xml = EscomUtils.decompress(scheme.getPackElements());
            StringReader reader = new StringReader(xml);
            WorkflowElements elements = JAXB.unmarshal(reader, WorkflowElements.class);
            //перелинковка задач с объектами
            elements.getTasks().forEach((key, taskEl)-> {
                for(Task task : scheme.getTasks()){
                    if (task.getTaskLinkUID().equals(key)){
                        taskEl.setTask(task);
                        break;
                    }
                }
               });
            //перелинковка timers с объектами
            elements.getTimers().forEach((key, timerEl)-> {
                for(ProcTimer procTimer : scheme.getTimers()){
                    if (procTimer.getTimerLinkUID().equals(key)){
                        timerEl.setProcTimer(procTimer);
                        break;
                    }
                }
               });
            scheme.setElements(elements);
        } catch (IOException ex) {
            //errors.add("ErrorUnpackingProcessDiagram");
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void validateScheme(Scheme scheme, Boolean checkTasks, Set<String> errors) {       
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
        
        Date planEndDate = scheme.getProcess().getPlanExecDate();
        if (checkTasks){
            for(Task task : scheme.getTasks()){ 
                switch (task.getDeadLineType()){
                    case "data":{
                        if (task.getPlanExecDate() == null){
                            errors.add("TasksNoHaveDeadline");
                        } else 
                            if (task.getPlanExecDate().before(new Date())){
                                errors.add("DeadlineSpecifiedInPastTime");
                            } else 
                                if (task.getPlanExecDate().after(planEndDate)) { 
                                    errors.add("TaskExecTimeLongerThanProcessDeadLine");
                                }
                        break;
                    }
                    case "delta":{
                        if (task.getDeltaDeadLine() == null || task.getDeltaDeadLine() == 0){
                            errors.add("TasksNoHaveDeadline");
                        }
                    }        
                }           
            }
        }

        scheme.getElements().getConditions().entrySet().stream()
                .filter(rec->rec.getValue().getConditonId() == null)
                .findFirst()
                .map(rec->errors.add("IncorrectConditionRouteProcess"));
        scheme.getElements().getStates().entrySet().stream()
                .filter(rec->rec.getValue().getDocStatusId() == null)
                .findFirst()
                .map(rec->errors.add("ProcedureSettingStateContainsIncorrectValue"));
        //ToDo! другие проверки!
    }

    /**
     * Выполнение задачи
     * @param process
     * @param task
     * @param result 
     * @param user 
     * @param errors 
     */
    @Override
    public void executeTask(Process process, Task task, Result result, User user, Set<String> errors){
        Scheme scheme = process.getScheme();
        unpackScheme(scheme);
        if (errors.isEmpty()){
            TaskElem startElement = scheme.getElements().getTasks().get(task.getTaskLinkUID());
            startElement.getTask().setResult(result.getName());
            taskFacade.taskDone(task, result, user);            
            scheme.getTasks().remove(task);
            scheme.getTasks().add(task); //TODO это лишнее ?  
            ProcReport report = new ProcReport(task.getComment(), DictReportStatuses.REPORT_ACTUAL, user, process, task);
            process.getReports().add(report);
            task.getReports().add(report);
            run(process, startElement, errors);
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
                        task.setFactExecDate(null);
                        task.setResult(null);
                        task.setComment(null);
                        if ("delta".equals(task.getDeadLineType())){
                            task.setPlanExecDate(DateUtils.calculateDate(task.getBeginDate(), task.getDeltaDeadLine()));
                        }
                        StringBuilder msg = new StringBuilder();
                        msg.append(ItemUtils.getMessageLabel("YouReceivedNewTask", config.getServerLocale()));
                        msg.append(" <").append(task.getName()).append(">!");
                        notificationService.makeNotification(task, msg.toString()); //уведомление о назначении задачи
                        taskFacade.makeReminder(task); 
                        taskFacade.inicializeExecutor(task, task.getOwner().getEmployee());
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_ASSIGNED, task.getAuthor());
                        task.getState().setCurrentState(stateFacade.getRunningState());
                    });
            }
    }
    
    /**
     * Запуск процесса на выполнение
     * @param process
     * @param user
     * @param errors 
     */
    @Override
    public void start(Process process, User user, Set<String> errors) {        
        Scheme scheme = process.getScheme();
        unpackScheme(scheme);
        validateScheme(scheme, true, errors);
        if (!errors.isEmpty()) return;
        
        scheme.getElements().getConnectors().forEach(c->c.setDone(false));  //сброс признака выполнения у всех коннекторов                
        scheme.getTasks().forEach(task->{
                task.setFactExecDate(null); //сброс даты выполнения
                task.setResult(null);       //сброс предудущего результата                
            });        
        process.getState().setCurrentState(stateFacade.getRunningState());
        process.setBeginDate(new Date());
        WFConnectedElem startElement = scheme.getElements().getStartElem();
        startElement.setDone(true);        
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_START, user);
        run(process, startElement, errors); 
    }

    /**
     * Прерывание выполнения процесса
     * @param process
     * @param user
     * @param errors 
     */
    @Override
    public void stop(Process process, User user, Set<String> errors) {
        State stateCancel = stateFacade.getCanceledState();
        //отмена всех запущенных задач
        String msg = ItemUtils.getMessageLabel("TaskCancelled", config.getServerLocale());
        process.getScheme().getTasks().stream()                
                .forEach(task->{
                        task.getState().setCurrentState(stateCancel);
                        notificationService.makeNotification(task, msg); //уведомление об аннулировании задачи
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_CANCELLED, user);
                    });        
        process.getState().setCurrentState(stateCancel);        
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_CANCELED, user);
        processFacade.edit(process);        
    }
    
    /**
     * Обработка выхода из процесса
     * @param scheme
     * @param exitElem 
     */
    private void finish(Process process, ExitElem exitElem, Set<String> errors) {
        if (exitElem.getFinalize()){
            process.getState().setCurrentState(stateFacade.getCompletedState());          
        }
        process.setFactExecDate(new Date());
    }

    /**
     * Выполняет движение процесса по маршруту от указанного начального элемента
     * по всем исходящим из него переходам (connectors)
     * и останавливается после запуска всех актуальных задач и/или достижения конца процесса
     * @param process
     * @param startElement
     * @param errors 
     */
    @Override
    public void run(Process process, WFConnectedElem startElement, Set<String> errors) {   
        Set<Task> exeTasks = new HashSet<>();
        doRun(startElement.getAnchors(), process, exeTasks, errors);
        if (errors.isEmpty()){
            startTasks(exeTasks, process.getScheme());            
            packScheme(process.getScheme());
            processFacade.edit(process);
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
    private void doRun(Set<AnchorElem> anchors, Process process, Set<Task> exeTasks, Set<String> errors){
        Scheme scheme = process.getScheme();
        anchors.stream()
                .filter(anchor->anchor.isSource())
                .forEach(anchor->{
                    //получаем список коннекторов, исходящих из якоря
                    List<ConnectorElem> connectors = 
                            scheme.getElements().getConnectors().stream()
                            .filter(connector->connector.getFrom().equals(anchor))                            
                            .map(connector->{
                                connector.setDone(true);
                                return connector;
                            })
                            .collect(Collectors.toList());
                    
                    if (!connectors.isEmpty()){
                        //запуск задач
                        connectors.stream()
                            .map(connector->scheme.getElements().getTasks().get(connector.getTo().getOwnerUID()))
                            .filter(element->Objects.nonNull(element))
                            .forEach(taskElem-> exeTasks.add(taskElem.getTask()));

                        //обрабатываем условия
                        Set<ConditionElem> targetConditions = findTargetConditions(connectors, scheme.getElements().getConditions());
                        targetConditions.stream()
                                .forEach(condition->{
                                    condition.setDone(true);
                                    if (checkCondition(condition, scheme, errors)){
                                        doRun(condition.getSecussAnchors(), process, exeTasks, errors);
                                    } else {
                                        doRun(condition.getFailAnchors(), process, exeTasks, errors);
                                    }
                                });

                        //изменяем изменения статусы документа
                        Set<StatusElem> targetStates = findTargetStates(connectors, scheme.getElements().getStates());
                        targetStates.forEach(stateElem->{
                                    stateElem.setDone(true);
                                    changingDoc(stateElem, scheme, errors);
                                    doRun(stateElem.getAnchors(), process, exeTasks, errors);
                                });

                        //обрабатываем логические элементы
                        Set<LogicElem> targetLogics = findTargetLogics(connectors, scheme.getElements().getLogics());
                        targetLogics.stream()
                                .filter(logic-> canExeLogic(logic, scheme))
                                .forEach(logic-> {
                                    logic.setDone(true);
                                    doRun(logic.getAnchors(), process, exeTasks, errors);
                                });

                        //обрабатываем таймеры
                        Set<TimerElem> timers = findTimers(connectors, scheme.getElements().getTimers());
                        timers.stream()
                                .forEach(timerEl-> {
                                    ProcTimer procTimer = timerEl.getProcTimer();
                                    switch (procTimer.getStartType()){
                                        case "on_init":{                                    
                                            procTimer.setStartDate(DateUtils.addMinute(new Date(), -1));
                                            break;
                                        }
                                        case "on_plan":{
                                            procTimer.setStartDate(process.getPlanExecDate());
                                            break;
                                        }
                                        case "on_date":{
                                            if (procTimer.getStartDate()== null){
                                                procTimer.setStartDate(DateUtils.addMinute(new Date(), -1));
                                            }
                                            break;
                                        }
                                    }
                                    if (procTimer.getStartDate().before(new Date())){
                                        procTimerFacade.updateNextStart(procTimer);
                                        doRun(timerEl.getAnchors(), process, exeTasks, errors);
                                    }
                                });
                        
                        //обрабатываем сообщения
                        Set<MessageElem> messages = findMessages(connectors, scheme.getElements().getMessages());
                        messages.stream().forEach(msgEl->{
                            processFacade.sendRoleMessage(process, msgEl.getRecipientsJSON(), msgEl.getContent(), "");
                            doRun(msgEl.getAnchors(), process, exeTasks, errors);
                        });
                                
                        //обработка выходов из процесса -> переход в связанный(е) процесс(ы)
                        Set<ExitElem> targetExits = findTargetExits(connectors, scheme.getElements().getExits());
                        targetExits.forEach(exitElem->{
                                exitElem.setDone(true);
                                finish(process, exitElem, errors);
                            });
                    }
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
     * Находит таймеры в модели процесса к которым идут коннекторы
     * @return 
     */    
    private Set<TimerElem> findTimers(List<ConnectorElem> connectors, Map<String, TimerElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }
    
    /**
     * Находит элементы 'Сообщение' в модели процесса к которым идут коннекторы
     * @return 
     */    
    private Set<MessageElem> findMessages(List<ConnectorElem> connectors, Map<String, MessageElem> elements){
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
    private Set<StatusElem> findTargetStates(List<ConnectorElem> connectors, Map<String, StatusElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
    }    
    
    /**
     * Выполнение изменений в документе
     * @param stateElem
     * @param scheme
     * @param errors 
     */
    private void changingDoc(StatusElem stateElem, Scheme scheme, Set<String> errors){
        final StatusesDoc status = getNewDocStatus(stateElem, errors);
        final State state = getNewDocState(stateElem, errors);
        if (!errors.isEmpty()) return;
        List<Doc> docs = scheme.getProcess().getDocs();
        if (CollectionUtils.isNotEmpty(docs)){
            docs.forEach(d -> {
                Doc doc = docFacade.find(d.getId());
                if (doc != null){
                    boolean docChanging = false;
                    if (status != null){
                        DocStatuses docStatus = new DocStatuses(doc, status);
                        docStatus.setValue(Boolean.TRUE);
                        docStatus.setDateStatus(new Date());                    
                        doc.getDocsStatusList().add(docStatus); 
                        docChanging = true;
                    }
                    if (state != null){
                        doc.getState().setCurrentState(state);
                        docChanging = true;
                    }
                    if (docChanging){
                        docFacade.edit(doc);
                    }
                }
            });
        }
    }              
    
    private State getNewDocState(StatusElem stateElem, Set<String> errors){
        if (stateElem.getDocStateId() == null) return null;
        State state = stateFacade.find(stateElem.getDocStateId());
        if (state == null){
            errors.add("StateProcessRouteNotFound");
        }
        return state;
    }
    
    private StatusesDoc getNewDocStatus(StatusElem stateElem, Set<String> errors ){        
        if (stateElem.getDocStatusId() == null) return null;
        StatusesDoc status = statusesFacade.find(stateElem.getDocStatusId());
        if (status == null){
            errors.add("StateProcessRouteNotFound");            
        }
        return status;
    }
    
    /* *** ВЕТВЛЕНИЯ *** */
    
    /**
     * Проверяет возможность выполнения условного ветвления
     * @param logic
     * @return 
     */
    private boolean canExeLogic(LogicElem logic, Scheme scheme){
        if ("OR".equals(logic.getCaption().toUpperCase())) return true;
        
        List<AnchorElem> anchors = logic.getAnchors().stream()
                .filter(anchor->anchor.isTarget())
                .collect(Collectors.toList());
        Boolean result = true;
        for (AnchorElem anchorElem : anchors){            
            for (ConnectorElem connector : scheme.getElements().getConnectors()){
                if (connector.getTo().equals(anchorElem) && !connector.isDone()){
                    result = false;
                    break;
                }
            }            
        }
        return result;
    }
            
    /* *** УСЛОВИЯ *** */
    
    /**
     * Проверка условия
     * @param condition
     * @return 
     */
    private boolean checkCondition(ConditionElem conditionEl, Scheme scheme, Set<String> errors){
        Boolean result = false;
        Condition condition = (Condition)conditionFacade.find(conditionEl.getConditonId());
        if (condition == null){
            errors.add("IncorrectConditionRouteProcess");
            return result;
        }
        switch (condition.getMethod()){
            case "everyoneApproved":{
                result = everyoneApproved(scheme);
                break;
            }
            case "allFinished":{
                result = allFinished(scheme);
                break;
            }
            case "allRemarksChecked":{
                result = allRemarksChecked(scheme);
                break;
            }
        }
        return result;
    }
    
    /**
     * Условие "Все одобрили?". Проверяет, есть ли кто-то отклонивший документ
     * @param scheme
     * @return 
     */
    private boolean everyoneApproved(Scheme scheme){
        Boolean result = true;
        for (Task task : scheme.getTasks()){
            String taskResult = task.getResult();
            if (DictResults.RESULT_REFUSED.equals(taskResult)){
                result = false;
                break;
            }
        }
        return result;
    }
    
    /**
     * Условие "Все поручения выполнены?". Проверяет, есть ли не выполненные задачи в данном процессе
     * @param scheme
     * @return 
     */
    private boolean allFinished(Scheme scheme){
        boolean result = true;
        for(Task task : scheme.getTasks()){
            if (task.getFactExecDate() == null){
                result = false;
                break;
            }
        }
        return result;
    }
    
    /**
     * Условие, все замечания учтены?
     * @param scheme
     * @return 
     */
    private boolean allRemarksChecked(Scheme scheme){
        boolean result = true;
        Process process = processFacade.find(scheme.getProcess());
        for(Remark remark : process.getDetailItems()){
            if (!remark.isChecked()){
                result = false;
                break;
            }
        }
        return result;
    }
}