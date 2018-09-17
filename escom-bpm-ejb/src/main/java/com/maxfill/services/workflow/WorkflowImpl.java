package com.maxfill.services.workflow;

import com.maxfill.Configuration;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictReportStatuses;
import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.ProcessParams;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.process.conditions.ConditionFacade;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.process.procedures.Procedure;
import com.maxfill.model.process.procedures.ProcedureFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.model.docs.docStatuses.StatusesDocFacade;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.conditions.Condition;
import com.maxfill.model.process.remarks.Remark;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.process.timers.ProcTimerFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.states.State;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.TaskReport;
import com.maxfill.model.task.result.Result;
import com.maxfill.model.users.User;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.services.numerators.NumeratorService;
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
import javax.ejb.Asynchronous;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
    private ProcedureFacade procedureFacade;
    @EJB
    private StatusesDocFacade statusesFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private NotificationService notificationService;
    @EJB
    private NumeratorService numeratorService;
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
        List<ConnectorElem> connectorElems = scheme.getElements().getConnectors();
        if (!connectorElems.contains(connector)){
            connectorElems.add(connector);
        }
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
        if (connector != null){            
            if (scheme.getElements().getConnectors().contains(connector)) {
                scheme.getElements().getConnectors().remove(connector);
            }
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
        if (scheme.getElements().getTasks().isEmpty()){
            errors.add("DiagramNotHaveTasks");
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
        scheme.getElements().getProcedures().entrySet().stream()
                .filter(rec->rec.getValue().getProcedureId() == null)
                .findFirst()
                .map(rec->errors.add("IncorrectProcedureRouteProcess"));
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
     * @param currentUser 
     * @param params 
     * @param errors 
     */
    @Override
    public void executeTask(Process process, Task task, Result result, User currentUser, Map<String, Object> params, Set<String> errors){
        Scheme scheme = process.getScheme();
        unpackScheme(scheme);
        if (errors.isEmpty()){
            TaskElem startElement = scheme.getElements().getTasks().get(task.getTaskLinkUID());
            startElement.getTask().setResult(result.getName());
            taskFacade.taskDone(task, result, currentUser);
            scheme.getTasks().remove(task);
            scheme.getTasks().add(task); //TODO это лишнее ?  
            
            //Запись отчёта по задаче
            TaskReport taskReport = new TaskReport(task.getComment(), DictReportStatuses.REPORT_ACTUAL, currentUser, task);
            task.getReports().add(taskReport);
                        
            updateReportStatus(process, task.getOwner(), task.getResult(), currentUser);            
                                
            run(process, startElement, currentUser, params, errors);
            processFacade.addLogEvent(process, DictLogEvents.TASK_FINISHED, currentUser);
        }
    }
    
    /**
     * Получить отчёт процесса, относящийся к согласующему и обновить его значения
     * @param process
     * @param staff - согласующее лицо
     * @param status
     * @param currentUser 
     */
    private void updateReportStatus(Process process, Staff staff, String status, User currentUser){
        if (staff == null) return;
        ProcReport procReport = process.getReports().stream()
                    .filter(report-> Objects.equals(staff, report.getExecutor()))
                    .findFirst()
                    .orElse(new ProcReport(currentUser, staff, process));
        procReport.setStatus(status);
        procReport.setDateCreate(new Date());
        Doc doc = process.getDocument(); //запись в отчёт версии, за которую проголосовал 
        if (doc != null){
            procReport.setVersion(doc.getMainAttache());
        }
    }
    
    /**
     * Запуск задач на выполнение
     * @param tasks
     * @param scheme 
     */
    private void startTasks(Set<Task> tasks, Map<String, Object> params){
        if (tasks.isEmpty()) return;
        boolean sendNotAgree = params.containsKey(ProcessParams.PARAM_SEND_NOTAGREE);
        tasks.stream()
            .filter(task-> !task.getState().getCurrentState().equals(stateFacade.getRunningState())
                    && (sendNotAgree == Boolean.FALSE || !DictResults.RESULT_AGREED.equals(task.getResult())))                    
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
    
    /**
     * Запуск процесса на выполнение
     * @param process
     * @param currentUser
     * @param params
     * @param errors 
     */
    @Override
    public void start(Process process, User currentUser, Map<String, Object> params, Set<String> errors) {        
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
        updateReportStatus(process, currentUser.getStaff(), DictResults.RESULT_AGREED, currentUser);
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_START, currentUser);
        run(process, startElement, currentUser, params, errors);
    }

    /**
     * Прерывание выполнения процесса
     * @param process
     * @param currentUser
     * @param errors 
     */
    @Override
    public void stop(Process process, User currentUser, Set<String> errors) {
        State stateCancel = stateFacade.getCanceledState();
        //отмена всех запущенных задач
        stopTasks(process, stateCancel, "TaskCancelled", currentUser);
        stopTimers(process);
        process.getState().setCurrentState(stateCancel);        
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_CANCELED, currentUser);
        processFacade.edit(process);        
    }
    
    /**
     * Остановка всех запущенных задач с установкой признака причины остановки
     * @param process
     * @param state
     * @param keyMessage
     * @param currentUser 
     */
    private void stopTasks(Process process, State state, String keyMessage, User currentUser){
        String msg = ItemUtils.getMessageLabel(keyMessage, config.getServerLocale());
        process.getScheme().getTasks().stream()                
                .filter(task->task.getState().getCurrentState().equals(stateFacade.getRunningState()))
                .forEach(task->{
                        task.getState().setCurrentState(state);
                        notificationService.makeNotification(task, msg); //уведомление об аннулировании задачи
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_CANCELLED, currentUser);
                    });
    }
    
    /**
     * Остановка всех запущенных таймеров 
     * @param process
     */
    private void stopTimers(Process process){
        process.getScheme().getTimers().stream()
                .filter(timer->timer.getStartDate() != null)
                .forEach(timer->procTimerFacade.stopTimer(timer));
    }
    
    /**
     * Обработка выхода из процесса
     * @param exitElem 
     */
    private void finish(Process process, ExitElem exitElem, User currentUser, Set<String> errors) {
        if (exitElem.getFinalize()){
            State state = stateFacade.getCompletedState();
            process.getState().setCurrentState(state);        
            //отмена всех запущенных задач
            stopTasks(process, state, "TaskIsFinishedBecauseProcessComplete", currentUser);
            stopTimers(process);
        }
        process.setFactExecDate(new Date());
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_FINISHED, currentUser);
    }

    /**
     * Выполняет движение процесса по маршруту от указанного начального элемента
     * по всем исходящим из него переходам (connectors)
     * и останавливается после запуска всех актуальных задач и/или достижения конца процесса
     * @param process
     * @param startElement
     * @param errors 
     * @param params 
     * @param currentUser 
     */
    @Override
    public void run(Process process, WFConnectedElem startElement, User currentUser, Map<String, Object> params, Set<String> errors) {   
        Set<Task> exeTasks = new HashSet<>();
        doRun(startElement.getAnchors(), process, exeTasks, currentUser, params, errors);
        if (errors.isEmpty()){
            startTasks(exeTasks, params);            
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
    private void doRun(Set<AnchorElem> anchors, Process process, Set<Task> exeTasks, User currentUser, Map<String, Object> params, Set<String> errors){
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
                        //выполнение процедур
                        Set<ProcedureElem> procedures = findProcedures(connectors, scheme.getElements().getProcedures());
                        procedures.forEach(procedureElem->{
                            executeProcedure(procedureElem, scheme, errors);
                            doRun(procedureElem.getAnchors(), process, exeTasks, currentUser, params, errors);
                        });

                        //формирование списка задач, которые должны быть активированы
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
                                        doRun(condition.getSecussAnchors(), process, exeTasks, currentUser, params, errors);
                                    } else {
                                        doRun(condition.getFailAnchors(), process, exeTasks, currentUser, params, errors);
                                    }
                                });

                        //изменяем изменения статусы документа
                        Set<StatusElem> targetStates = findTargetStates(connectors, scheme.getElements().getStates());
                        targetStates.forEach(stateElem->{
                                    stateElem.setDone(true);
                                    changingDoc(stateElem, scheme, errors);
                                    doRun(stateElem.getAnchors(), process, exeTasks, currentUser, params, errors);
                                });

                        //обрабатываем логические элементы
                        Set<LogicElem> targetLogics = findTargetLogics(connectors, scheme.getElements().getLogics());
                        targetLogics.stream()
                                .filter(logic-> canExeLogic(logic, scheme))
                                .forEach(logic-> {
                                    logic.setDone(true);
                                    doRun(logic.getAnchors(), process, exeTasks, currentUser, params, errors);
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
                                        doRun(timerEl.getAnchors(), process, exeTasks, currentUser, params, errors);
                                    }
                                });
                        
                        //обрабатываем сообщения
                        Set<MessageElem> messages = findMessages(connectors, scheme.getElements().getMessages());
                        messages.stream().forEach(message->{
                            sendMessage(process, message, currentUser);
                            doRun(message.getAnchors(), process, exeTasks, currentUser, params, errors);
                        });
                                
                        //обработка выходов из процесса -> переход в связанный(е) процесс(ы)
                        Set<ExitElem> targetExits = findTargetExits(connectors, scheme.getElements().getExits());
                        targetExits.forEach(exitElem->{
                                exitElem.setDone(true);
                                finish(process, exitElem, currentUser, errors);
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
     * Находит элементы 'Процедура' в модели процесса к которым идут коннекторы
     * @return
     */
    private Set<ProcedureElem> findProcedures(List<ConnectorElem> connectors, Map<String, ProcedureElem> elements){
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

    /* *** УВЕДОМЛЕНИЯ  *** */
    
    private void sendMessage(Process process, MessageElem message, User user){        
        StringBuilder sb = new StringBuilder();        
        String subject = message.getContent();
        processFacade.sendRoleMessage(process, message.getRecipientsJSON(), subject, sb, user);
    }
    
    /* *** ПРОЦЕДУРЫ *** */

    private void executeProcedure(ProcedureElem procedureElem, Scheme scheme, Set<String> errors){
        Procedure procedure = procedureFacade.find(procedureElem.getProcedureId());
        if (procedure == null) return;
        switch (procedure .getMethod()) {
            case "regProcess": {
                callNumerator(scheme.getProcess(), processFacade);
                break;
            }
            case "regDoc": {
                callNumerator(scheme.getProcess().getDocument(), docFacade);
                break;
            }
        }
    }

    /**
     * Формирование номера по шаблону
     * @param scheme 
     */
    private void callNumerator(BaseDict item, BaseDictFacade facade){
        if (item == null) return;
        if (StringUtils.isBlank(item.getRegNumber())){
            NumeratorPattern numeratorPattern = facade.getMetadatesObj().getNumPattern();
            String number = numeratorService.doRegistrNumber(item, numeratorPattern, null, new Date());
            item.setRegNumber(number);
        }
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
        List<Doc> docs = process.getDocs();
        for (Doc doc : docs){
            for(Remark remark : doc.getDetailItems()){
                if (!remark.isChecked()){
                    result = false;
                    break;
                }
            }
            if (!result){
                break;
            }
        }
        return result;
    }
    
    /* *** ПРОЧЕЕ *** */
    
    /**
     * Замена исполнителя в отчёте по процессу (листе согласования)
     * @param task
     * @param user 
     */
    @Asynchronous
    @Override    
    public void replaceReportExecutor(Task task, User user){
        Scheme scheme = task.getScheme();
        if (scheme == null) return;        
        
        Process process = processFacade.find(scheme.getProcess().getId());        
        Set<ProcReport> procReports = process.getReports();
        
        Staff newExecutor = task.getOwner();
        Staff oldExecutor = scheme.getTasks().stream()
                .filter(t->t.getId().equals(task.getId()))
                .findFirst()
                .map(t-> t.getOwner())
                .orElse(null);
        if (oldExecutor != null){
            List<Staff> oldExecutors = scheme.getTasks().stream() //список всех таких, т.к. их может быть в модели больше чем один!
                    .filter(t->t.getOwner().equals(oldExecutor) && !t.getId().equals(task.getId()))
                    .map(t-> t.getOwner())
                    .collect(Collectors.toList());
            if (oldExecutors.isEmpty()){
                ProcReport oldExecutorReport = procReports.stream()
                        .filter(report-> report.getDateCreate() == null && Objects.equals(report.getExecutor(), oldExecutor))
                        .findFirst().orElse(null);
                if (oldExecutorReport != null){
                    procReports.remove(oldExecutorReport);
                }
            }
        }        
        
        //добавить в лист согласования нового исполнителя, если его там нет
        ProcReport procReport = new ProcReport(user, newExecutor, process);
        procReports.add(procReport);
        processFacade.edit(process);
    }
}