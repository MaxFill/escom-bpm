package com.maxfill.services.workflow;

import com.google.gson.Gson;
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
import com.maxfill.model.basedict.process.schemes.elements.WorkflowElements;
import com.maxfill.model.basedict.process.schemes.elements.MessageElem;
import com.maxfill.model.basedict.process.schemes.elements.EnterElem;
import com.maxfill.model.basedict.process.schemes.elements.StartElem;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictReportStatuses;
import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.ProcessParams;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.conditions.ConditionFacade;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.process.procedures.Procedure;
import com.maxfill.model.basedict.process.procedures.ProcedureFacade;
import com.maxfill.model.core.states.StateFacade;
import com.maxfill.model.basedict.docStatuses.StatusesDocFacade;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.docStatuses.DocStatuses;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.conditions.Condition;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.process.timers.ProcTimerFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskReport;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.services.numerators.NumeratorService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Сервис реализует методы управления бизнес-процессами
 */
@Stateless
public class WorkflowImpl implements Workflow {
    protected static final Logger LOGGER = Logger.getLogger(WorkflowImpl.class.getName());
    private final String EXECUTED_TASKS = "executedTasks";
    
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
    private ProcTimerFacade procTimerFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private StaffFacade staffFacade;
    
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
        if (scheme == null || scheme.getPackElements() == null ) return;
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
                if (StringUtils.isBlank(task.getName())){
                    errors.add("TaskNoHaveName");
                }
                switch (task.getDeadLineType()){
                    case "data":{
                        if (task.getPlanExecDate() == null){
                            errors.add("TasksNoHaveDeadline");
                        } else 
                            if (task.getPlanExecDate().before(new Date())){
                                errors.add("DeadlineTaskInPastTime");
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
     * Сброс настроек схемы
     * @param scheme 
     */
    @Override
    public void clearScheme(Scheme scheme){
        scheme.getElements().getConnectors().forEach(c->c.setDone(false));  
        scheme.getElements().getLogics().forEach((k, logicEl)->logicEl.getTasksExec().clear());
        scheme.getElements().getTasks().forEach((k, taskEl)->taskEl.getTasksExec().clear());
        scheme.getTasks().forEach(task->{
                task.setFactExecDate(null); //сброс даты выполнения
                task.setResult(null);       //сброс предудущего результата
                task.getState().setCurrentState(stateFacade.getDraftState());
            });
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
        if (DictStates.STATE_RUNNING != process.getState().getCurrentState().getId()){
            errors.add("TaskCannotCompletedBecauseProcessStopped");
            return;
        }
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
            
            //Внесение инф. в отчёт по процессу
            if (task.getConsidInProcReport()){
                ProcReport procReport = updateReportStatus(process, task.getOwner(), task.getResult(), currentUser);
                procReport.setTask(task);
            }
            
            List<Integer> executedTasks = startElement.getTasksExec();            
            executedTasks.add(task.getId());
            
            params.put(EXECUTED_TASKS, executedTasks);
            
            run(process, startElement, currentUser, params, errors);
            processFacade.addLogEvent(process, DictLogEvents.TASK_FINISHED, currentUser);
        }
    }
    
    @Override
    public void executeTimer(ProcTimer procTimer, Set<String> errors){
        User admin = userFacade.getAdmin();
        Process process = processFacade.find(procTimer.getProcess().getId());
        if (process == null){            
            errors.add("WorkflowIncorrectData");
            return;
        }
        Scheme scheme = process.getScheme();
        unpackScheme(scheme);
        TimerElem startElement = scheme.getElements().getTimers().get(procTimer.getTimerLinkUID());
        run(process, startElement, admin, new HashMap<>(), errors);
    }
    
    /**
     * Получить отчёт процесса, относящийся к согласующему и обновить его значения
     * @param process
     * @param staff - согласующее лицо
     * @param status
     * @param currentUser 
     */
    private ProcReport updateReportStatus(Process process, Staff staff, String status, User currentUser){        
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
        return procReport;
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
                
                notificationService.makeNotification(task, "YouReceivedNewTask"); //уведомление о назначении задачи
                
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
        
        //очистки если запуск повторный
        clearScheme(scheme);                        
        
        Doc doc = process.getDocument();
        if (doc != null){
            doc = docFacade.find(doc.getId());            
            docFacade.changeState(doc, DictStates.STATE_VALID);
        }
        
        process.getState().setCurrentState(stateFacade.getRunningState());
        process.setBeginDate(new Date());
        process.setResult(null);
        
        WFConnectedElem startElement = scheme.getElements().getStartElem();
        startElement.setDone(true);
        
        //если действие выполняется куратором, то внести инф в лист согласования 
        /*
        if (Objects.equals(process.getCurator().getEmployee(), currentUser)){
            updateReportStatus(process, process.getCurator(), DictResults.RESULT_AGREED, currentUser);
        }
        */
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
        
        Doc doc = process.getDocument();
        if (doc != null){
            doc = docFacade.find(doc.getId());            
            docFacade.returnToPrevState(doc);
        }
        
        //отмена всех запущенных задач
        cancelTasks(process, currentUser);
        stopTimers(process);
        
        process.getState().setCurrentState(stateCancel);
        process.setResult("ProcessСanceled");
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_CANCELED, currentUser);
        processFacade.edit(process);
    }
    
    /**
     * Отмена выполненных и остановка всех запущенных задач с установкой признака причины остановки
     * @param process          
     * @param currentUser 
     */
    private void cancelTasks(Process process, User currentUser){ 
        State stateCancel = stateFacade.getCanceledState();
        process.getScheme().getTasks().stream()
                .filter(task->DictStates.STATE_DRAFT != task.getState().getCurrentState().getId()) //отмна всех задач, кроме черновиков
                .forEach(task->{
                        task.getState().setCurrentState(stateCancel);
                        notificationService.makeNotification(task, "TaskCancelled"); //уведомление об аннулировании задачи
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_CANCELLED, currentUser);
                    });
    }
    
    /**
     * Остановка всех запущенных задач с установкой признака причины остановки
     * @param process
     * @param currentUser 
     */
    private void stopTasks(Process process, User currentUser){ 
        State stateFinish = stateFacade.getCompletedState();
        process.getScheme().getTasks().stream()
                .filter(task->Objects.equals(task.getState().getCurrentState().getId(), DictStates.STATE_RUNNING))
                .forEach(task->{
                        task.getState().setCurrentState(stateFinish);
                        notificationService.makeNotification(task, "TaskIsFinishedBecauseProcessComplete"); //уведомление об аннулировании задачи
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_FINISHED, currentUser);
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
            State state = stateFacade.find(exitElem.getFinishStateId());
            process.getState().setCurrentState(state);            
            stopTasks(process, currentUser); //отмена всех запущенных и не завершённых задач
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
                            .forEach(taskElem-> {
                                if (params.containsKey(EXECUTED_TASKS)){
                                    taskElem.getTasksExec().addAll((List<Integer>)params.get(EXECUTED_TASKS));
                                }
                                exeTasks.add(taskElem.getTask());
                            });

                        //обрабатываем условия
                        Set<ConditionElem> targetConditions = findTargetConditions(connectors, scheme.getElements().getConditions());
                        targetConditions.stream()
                                .forEach(condition->{
                                    condition.setDone(true);
                                    if (checkCondition(condition, scheme, params, errors)){
                                        doRun(condition.getSecussAnchors(), process, exeTasks, currentUser, params, errors);
                                    } else {
                                        doRun(condition.getFailAnchors(), process, exeTasks, currentUser, params, errors);
                                    }
                                });

                        //изменяем изменения статусы документа
                        Set<StatusElem> targetStates = findTargetStates(connectors, scheme.getElements().getStates());
                        targetStates.forEach(stateElem->{
                                    stateElem.setDone(true);
                                    StatusesDoc status = changingDoc(stateElem, scheme, errors);
                                    if (status != null && stateElem.getIsSaveInProc()){
                                        process.setResult(status.getBundleName());
                                    }
                                    doRun(stateElem.getAnchors(), process, exeTasks, currentUser, params, errors);
                                });

                        //обрабатываем логические элементы
                        Set<LogicElem> targetLogics = findTargetLogics(connectors, scheme.getElements().getLogics());
                        targetLogics.stream()
                                .filter(logic-> canExeLogic(logic, scheme, params))
                                .forEach(logic-> {
                                    logic.setDone(true);
                                    params.put(EXECUTED_TASKS, logic.getTasksExec());
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
    
    /* *** СТАТУСЫ *** */
    
    /**
     * Выполнение изменений в документе
     * @param stateElem
     * @param scheme
     * @param errors 
     */    
    private StatusesDoc changingDoc(StatusElem stateElem, Scheme scheme, Set<String> errors){
        final StatusesDoc status = getNewDocStatus(stateElem, errors);
        final State state = getNewDocState(stateElem, errors);
        if (!errors.isEmpty()) return null;
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
        return status;
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
        sb.append(message.getContent());        
        String subject = "NotifyFromProcess";
        processFacade.sendRoleMessage(process, message.getRecipientsJSON(), subject, sb, user);
    }
    
    /* *** ПРОЦЕДУРЫ *** */

    private void executeProcedure(ProcedureElem procedureElem, Scheme scheme, Set<String> errors){
        Procedure procedure = procedureFacade.find(procedureElem.getProcedureId());
        if (procedure == null) return;
        switch (procedure .getMethod()) {
            case "regProcess": {
                Process process = scheme.getProcess();
                callNumerator(process, processFacade);
                break;
            }
            case "regDoc": {
                Doc doc = scheme.getProcess().getDocument();
                if (doc != null){
                    callNumerator(doc, docFacade);                    
                }
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
            Date regDate = new Date();
            String number = numeratorService.doRegistrNumber(item, numeratorPattern, null, regDate);
            item.setRegNumber(number);
            item.setItemDate(regDate);
        }
    }

    /* *** ВЕТВЛЕНИЯ *** */
    
    /**
     * Проверяет возможность выполнения условного ветвления
     * @param logic
     * @return 
     */
    private boolean canExeLogic(LogicElem logic, Scheme scheme, Map<String, Object> params){
        if (params.containsKey(EXECUTED_TASKS)){
            logic.getTasksExec().addAll((List<Integer>)params.get(EXECUTED_TASKS));
        }
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
    private boolean checkCondition(ConditionElem conditionEl, Scheme scheme, Map<String, Object> params, Set<String> errors){
        Boolean result = false;
        Condition condition = (Condition)conditionFacade.find(conditionEl.getConditonId());
        if (condition == null){
            errors.add("IncorrectConditionRouteProcess");
            return result;
        }
        switch (condition.getMethod()){
            case "everyoneApproved":{
                result = everyoneApproved(scheme, params);
                break;
            }
            case "allFinished":{
                result = allFinished(scheme, params);
                break;
            }
            case "allRemarksChecked":{
                result = allRemarksChecked(scheme, params);
                break;
            }
            case "agreedUponEmployee":{
                Map<String, Object> paramMap = conditionEl.getParams();
                if (!paramMap.containsKey("staff")) return false;
                Integer staffId = (Integer)paramMap.get("staff");
                if (staffId == null) return false;
                Staff staff = staffFacade.find(staffId);
                result = agreedUponEmployee(scheme, staff);
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
    private boolean everyoneApproved(Scheme scheme, Map<String, Object> params){        
        if (!params.containsKey(EXECUTED_TASKS)) return false;
        List<Integer> taskIds = (List<Integer>) params.get(EXECUTED_TASKS);
        return scheme.getTasks().stream()
                .filter(task->taskIds.contains(task.getId()) 
                        && Objects.equals(DictResults.RESULT_REFUSED, task.getResult()))
                .findFirst().orElse(null) == null;
    }
    
    /**
     * Условие "Все поручения выполнены?". Проверяет, есть ли не выполненные задачи в данном процессе
     * @param scheme
     * @return 
     */
    private boolean allFinished(Scheme scheme, Map<String, Object> params){
        if (!params.containsKey(EXECUTED_TASKS)) return false;
        List<Integer> taskIds = (List<Integer>) params.get(EXECUTED_TASKS);
        return scheme.getTasks().stream()
                .filter(task->taskIds.contains(task.getId()) 
                        && task.getFactExecDate() == null)
                .findFirst().orElse(null) == null;         
    }
    
    /**
     * Условие, все замечания учтены?
     * @param scheme
     * @return 
     */
    private boolean allRemarksChecked(Scheme scheme, Map<String, Object> params){
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
    
    /**
     * Условие: указанный сотрудник согласовал?
     * @param scheme
     * @param staff
     * @return 
     */
    private boolean agreedUponEmployee(Scheme scheme, Staff staff){        
        return scheme.getTasks().stream()
                .filter(task->Objects.equals(staff, task.getOwner()) 
                        && Objects.equals(DictStates.STATE_COMPLETED, task.getState().getCurrentState().getId())
                        && StringUtils.isNotBlank(task.getResult())                        
                        && !Objects.equals(DictResults.RESULT_REFUSED, task.getResult()))
                .findFirst().orElse(null) != null;
    }
    
    /* *** ПРОЧЕЕ *** */
    
    /**
     * Формирование листа согласования
     * @param p
     * @param user - автор изменений
     */
    @Override 
    public void makeProcessReport(Process p, User user){
        final Process process = processFacade.find(p.getId());
        if (process == null) return;
        
        Set<ProcReport> procReports = process.getReports();
        
        //удаляем из листа все записи, кроме тех, которые уже согласовали
        List<ProcReport> removeRepors = procReports.stream()            
                .filter(report-> report.getDateCreate() == null )
                .collect(Collectors.toList());
        procReports.removeAll(removeRepors); 
                
        //формируем список из задач процесса, кроме тех, которые не должны там быть
        Set<ProcReport> newReports = process.getScheme().getTasks().stream()
                .filter(task->task.getOwner() != null && task.getConsidInProcReport())
                .map(task -> new ProcReport(user, task.getOwner(), process))
                .collect(Collectors.toSet()); 
        //добавляем отчёт для куратора
        newReports.add(new ProcReport(user, process.getCurator(), process));
                        
        //добавляем в лист согласования записи, если таких там нет
        newReports.removeAll(procReports);        
        procReports.addAll(newReports);
        
        processFacade.edit(process);
    }
        
}