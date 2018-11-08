package com.maxfill.services.workflow;

import com.maxfill.dictionary.DictDocStatus;
import com.maxfill.model.basedict.process.schemes.elements.*;
import com.maxfill.dictionary.DictLogEvents;
import com.maxfill.dictionary.DictReportStatuses;
import com.maxfill.dictionary.DictResults;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.ProcessParams;
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
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.conditions.Condition;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.schemes.elements.SubProcessElem;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.process.timers.ProcTimerFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.core.states.State;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.task.TaskReport;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.model.basedict.userGroups.UserGroupsFacade;
import com.maxfill.services.notification.NotificationService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Comparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
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
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import com.maxfill.model.basedict.doc.numerator.DocNumerator;
import com.maxfill.model.basedict.process.numerator.ProcessNumerator;

/**
 * Сервис реализует методы управления бизнес-процессами
 */
@Stateless
public class WorkflowImpl implements Workflow {
    protected static final Logger LOGGER = Logger.getLogger(WorkflowImpl.class.getName());
    private final String EXECUTED_TASKS = "executedTasks";
    private final String LAST_TASK = "lastTask";
    
    @Resource
    private SessionContext ctx;
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private ProcessTypesFacade processTypeFacade;
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
    private DocNumerator docNumeratorService;
    @EJB
    private ProcessNumerator processNumerator;    
    @EJB
    private ProcTimerFacade procTimerFacade;
    @EJB
    private UserFacade userFacade;
    @EJB
    private UserGroupsFacade userGroupsFacade;
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
    public void addSubProcess(SubProcessElem elem, Scheme scheme, Set<String> errors) {
        if (elem == null){
            errors.add("WorkflowIncorrectData");
        }
        if (!errors.isEmpty()) return;        
        //ToDo проверки!        
        scheme.getElements().getSubprocesses().put(elem.getUid(), elem);
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
        } else if (element instanceof SubProcessElem){
            scheme.getElements().getSubprocesses().remove(element.getUid());
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
    
    /* *** СХЕМА *** */
    
    @Override
    public Scheme initScheme(Process process, ProcTempl defaultTempl, User currentUser, Set<Tuple> errors){
        Scheme scheme = process.getScheme();
        if (scheme == null){
            scheme = new Scheme(process);
            if (defaultTempl != null){
                //defaultTempl = processTypeFacade.getDefaultTempl(process.getOwner(), currentUser);            
                scheme.setPackElements(defaultTempl.getElements());
                scheme.setName(defaultTempl.getName());
            }
            process.setScheme(scheme);
        }
        unpackScheme(scheme, currentUser);
        return scheme;
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
    public void unpackScheme(Scheme scheme, User currentUser) {
        if (scheme == null || scheme.getPackElements() == null ) return;
        try {
            String xml = EscomUtils.decompress(scheme.getPackElements());
            StringReader reader = new StringReader(xml);
            WorkflowElements elements = JAXB.unmarshal(reader, WorkflowElements.class);
            
            //линковка элементов модели c подпроцессами
            elements.getSubprocesses().forEach((key, subProcEl)-> {
                Process subProcess = scheme.getProcess().getChildItems().stream()
                        .filter(sp-> sp.getLinkUID().equals(key))
                        .findFirst()
                        .orElse(null);
                subProcEl.setSubProcess(subProcess);
            });
            
            //линковка элементов модели c задачами
            elements.getTasks().forEach((key, taskEl)-> {
                Task task = scheme.getTasks().stream().filter(t-> t.getTaskLinkUID().equals(key)).findFirst().orElse(null);                
                if (task == null){ 
                    UserGroups role = null;
                    if (taskEl.getRoleInProc() != null){
                        role = userGroupsFacade.find(taskEl.getRoleInProc());
                    }
                    Staff staff = null;
                    if (taskEl.getStaffId() != null){
                        staff = staffFacade.find(taskEl.getStaffId());
                    } 
                    task = taskFacade.createTaskInProc(staff, currentUser, scheme.getProcess(), taskEl.getUid());                
                    task.setConsidInProcReport(taskEl.getConsidInProc());
                    task.setRoleInProc(role);
                    task.setDeadLineType(taskEl.getDeadLineType());
                    task.setName(taskEl.getName());
                    task.setDeltaDeadLine(taskEl.getDeltaDeadLine());
                    task.setReminderType(taskEl.getReminderType());
                    task.setReminderRepeatType(taskEl.getReminderType());
                    task.setDeltaReminder(taskEl.getDeltaReminder());
                    task.setReminderTime(taskEl.getReminderTime());
                    task.setReminderDays(taskEl.getReminderDays());
                    task.setAvaibleResultsJSON(taskEl.getAvaibleResultsJSON());                    
                    scheme.getTasks().add(task);
                }
                taskEl.setTask(task);
            });
            
            //линковка элементов модели c таймерами
            elements.getTimers().forEach((key, timerEl)-> {
                ProcTimer procTimer = scheme.getTimers().stream().filter(timer-> timer.getTimerLinkUID().equals(key)).findFirst().orElse(null);                
                if (procTimer == null){
                    procTimer = procTimerFacade.createTimer(scheme.getProcess(), scheme, timerEl.getUid());                    
                    procTimer.setRepeatType(timerEl.getRepeatType());
                    procTimer.setStartType(timerEl.getStartType());
                    scheme.getTimers().add(procTimer);
                }
                timerEl.setProcTimer(procTimer);                
               });
            scheme.setElements(elements);
        } catch (IOException ex) {
            //errors.add("ErrorUnpackingProcessDiagram");
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void validateScheme(Scheme scheme, Boolean checkTasks, Set<Tuple> errors) {       
        StartElem startElem = scheme.getElements().getStartElem();        
        if (startElem == null && scheme.getElements().getEnters().isEmpty()){
            errors.add(new Tuple("DiagramNotHaveStart", new Object[]{}));
        }
        if (scheme.getElements().getExits().isEmpty()){
            errors.add(new Tuple("DiagramNotHaveExit", new Object[]{}));
        }
        if (scheme.getElements().getConnectors().isEmpty()){
            errors.add(new Tuple("DiagramNotHaveConnectors", new Object[]{}));
        }
        
        if (scheme.getElements().getTasks().isEmpty() && MapUtils.isEmpty(scheme.getElements().getSubprocesses())){
            errors.add(new Tuple("DiagramNotHaveTasks", null));
        }
        if (scheme.getProcess().getCurator() == null || scheme.getProcess().getCurator().getEmployee() == null){
            errors.add(new Tuple("CuratorNotSet", null));
        }
        Date planEndDate = scheme.getProcess().getPlanExecDate();
        if (checkTasks){            
            for(Task task : scheme.getTasks()){ 
                if (task.getOwner()==null && task.getRoleInProc()==null){
                    errors.add(new Tuple("TaskNoHaveExecutor", new Object[]{task.getId(), task.getName()}));
                }
                if (StringUtils.isBlank(task.getName())){
                    errors.add(new Tuple("TaskNoHaveName", new Object[]{task.getId(), task.getName()}));
                }
                switch (task.getDeadLineType()){
                    case "data":{
                        if (task.getPlanExecDate() == null){
                            errors.add(new Tuple("TasksNoHaveDeadline", new Object[]{task.getId(), task.getName()}));
                        } else 
                            if (task.getPlanExecDate().before(new Date())){
                                errors.add(new Tuple("DeadlineTaskInPastTime", new Object[]{task.getId(), task.getName()}));
                            } else 
                                if (task.getPlanExecDate().after(planEndDate)) { 
                                    errors.add(new Tuple("TaskExecTimeLongerThanProcessDeadLine", new Object[]{task.getId(), task.getName()}));
                                }
                        break;
                    }
                    case "delta":{
                        if (task.getDeltaDeadLine() == null || task.getDeltaDeadLine() == 0){
                            errors.add(new Tuple("TasksNoHaveDeadline", new Object[]{task.getId(), task.getName()}));
                        }
                    }        
                }           
            }
        }

        scheme.getElements().getSubprocesses().entrySet().stream()
                .filter(rec->rec.getValue().getProctypeId() == null)
                .findFirst()
                .map(rec->errors.add(new Tuple("IncorrectSubProcessType", new Object[]{rec.getValue().getCaption()})));
        scheme.getElements().getConditions().entrySet().stream()
                .filter(rec->rec.getValue().getConditonId() == null)
                .findFirst()
                .map(rec->errors.add(new Tuple("IncorrectConditionRouteProcess", new Object[]{rec.getValue().getCaption()})));
        scheme.getElements().getProcedures().entrySet().stream()
                .filter(rec->rec.getValue().getProcedureId() == null)
                .findFirst()
                .map(rec->errors.add(new Tuple("IncorrectProcedureRouteProcess", new Object[]{rec.getValue().getCaption()})));
        scheme.getElements().getStates().entrySet().stream()
                .filter(rec->rec.getValue().getDocStatusId() == null)
                .findFirst()
                .map(rec->errors.add(new Tuple("ProcedureSettingStateContainsIncorrectValue", new Object[]{rec.getValue().getCaption()})));
        //ToDo! другие проверки!
    }

    /**
     * Сброс настроек схемы
     * @param process 
     * @param currentUser 
     * @param params 
     * @param errors 
     */
    @Override
    public void clearScheme(Process process, User currentUser, Map<String, Object> params, Set<Tuple> errors){
        Scheme scheme = process.getScheme();
        if (scheme == null) return;
        clearElements(scheme);
        boolean sendNotAgree = params.containsKey(ProcessParams.PARAM_SEND_NOTAGREE);
        State draftState = stateFacade.getDraftState();
        scheme.getTasks().stream()
                .filter(task-> !sendNotAgree || Objects.equals(DictResults.RESULT_REFUSED, task.getResult()))
                .forEach(task->{
                    task.setFactExecDate(null);
                    task.setBeginDate(null);
                    task.setResult(null);
                    task.getState().setCurrentState(draftState);
            });
        
        process.setResult(null);
        process.setFactExecDate(null);
        process.getState().setCurrentState(draftState);
        
        process.getChildItems().stream()
                .filter(subProc-> !(sendNotAgree == true && Objects.equals(DictStates.STATE_COMPLETED, subProc.getState().getCurrentState().getId())))
                .forEach(subproc->clearScheme(subproc, currentUser, params, errors));
    }    
    
    @Override
    public void clearElements(Scheme scheme){
        scheme.getElements().getConnectors().forEach(c->c.setDone(false));  
        scheme.getElements().getLogics().forEach((k, logicEl)->logicEl.getTasksExec().clear());
        scheme.getElements().getTasks().forEach((k, taskEl)->taskEl.getTasksExec().clear());
    }
    
    /* *** ВЫПОЛНЕНИЕ ПРОЦЕССА *** */
    
    /**
     * Выполнение задачи
     * @param process
     * @param task
     * @param result 
     * @param currentUser 
     * @param params 
     * @param errors 
     * @return  
     */
    @Override
    public Set<BaseDict> executeTask(Process process, Task task, Result result, User currentUser, Map<String, Object> params, Set<Tuple> errors){        
        Set<BaseDict> forShow = new HashSet<>(); 
        if (DictStates.STATE_RUNNING != process.getState().getCurrentState().getId()){
            errors.add(new Tuple("TaskCannotCompletedBecauseProcessStopped", new Object[]{}));
            return forShow;
        }        
        
        if (!errors.isEmpty()) return forShow;        
        
        Scheme scheme = process.getScheme();
        unpackScheme(scheme, currentUser);
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
            ProcReport procReport = updateReportStatus(process, task.getOwner(), task.getRoleInProc(), task.getResult(), currentUser);
            procReport.setTask(task);
        }

        List<Integer> executedTasks = startElement.getTasksExec();            
        executedTasks.add(task.getId());

        params.put(EXECUTED_TASKS, executedTasks);
        params.put(LAST_TASK, task);
                
        forShow = run(process, startElement, new HashSet<>(), currentUser, params, errors);

        if (!errors.isEmpty()){                            
            ctx.setRollbackOnly();                      
        } else {
            processFacade.addLogEvent(process, DictLogEvents.TASK_FINISHED, currentUser);
        }
        
        return forShow;
    }
    
    @Override
    public void executeTimer(ProcTimer procTimer, Set<Tuple> errors){
        User admin = userFacade.getAdmin();
        Process process = processFacade.find(procTimer.getProcess().getId());
        if (process == null){            
            errors.add(new Tuple("ProcessNotFound", new Object[]{procTimer.getProcess().getId()}));
            return;
        }
        Scheme scheme = process.getScheme();
        unpackScheme(scheme, userFacade.getAdmin());
        TimerElem startElement = scheme.getElements().getTimers().get(procTimer.getTimerLinkUID());
        run(process, startElement, new HashSet<>(), admin, new HashMap<>(), errors);
    }
    
    /**
     * Получить отчёт процесса, относящийся к пользователю и обновить его значения
     * @param process
     * @param staff - согласующее лицо
     * @param status
     * @param currentUser 
     */
    private ProcReport updateReportStatus(Process process, Staff staff, UserGroups role, String status, User currentUser){        
        ProcReport procReport = process.getReports().stream()
                    .filter(report-> Objects.equals(staff, report.getExecutor()))
                    .findFirst()
                    .orElse(new ProcReport(currentUser, staff, process));
        procReport.setStatus(status);
        procReport.setDateCreate(new Date()); 
        Doc doc = process.getDocument(); //запись в отчёт версии, за которую проголосовал 
        procReport.setDoc(doc); 
        if (doc != null){
            procReport.setVersion(doc.getMainAttache());
        }
        if (role != null){
            procReport.setRoleName(role.getRoleFieldName());
        }
        if (!process.getReports().contains(procReport)){
            process.getReports().add(procReport);
        }
        return procReport;
    }
    
    /**
     * Запуск задач на выполнение
     * @param tasks
     * @param scheme 
     */
    private void startTasks(Process process, Set<Task> tasks, User currentUser, Map<String, Object> params, Set<Tuple> errors){
        if (tasks.isEmpty()) return;
        boolean sendNotAgree = params.containsKey(ProcessParams.PARAM_SEND_NOTAGREE);
        tasks.stream()
            .filter(task-> !task.getState().getCurrentState().equals(stateFacade.getRunningState()))                    
            .forEach(task->{
                //если нужно отправить только не согласовавшим и задача согласована, то обходим задачу
                if (sendNotAgree && Objects.equals(DictResults.RESULT_AGREED, task.getResult())){
                    TaskElem taskEl = process.getScheme().getElements().getTasks().get(task.getTaskLinkUID());
                    run(process, taskEl, new HashSet<>(), currentUser, params, errors);
                } else {
                    task.setBeginDate(new Date());
                    task.setFactExecDate(null);
                    task.setResult(null);
                    task.setComment(null);
                    if (task.getOwner() == null && task.getRoleInProc() != null){
                        User actor = processFacade.getActor(process, task.getRoleInProc().getRoleFieldName());
                        if (actor != null){
                            task.setOwner(actor.getStaff());
                        }
                    }
                    if (task.getOwner() != null){
                        if ("delta".equals(task.getDeadLineType())){
                            taskFacade.makeDatePlan(task);                    
                        }                

                        notificationService.makeNotification(task, "YouReceivedNewTask"); //уведомление о назначении задачи

                        taskFacade.makeReminder(task); 
                        taskFacade.inicializeExecutor(task, task.getOwner().getEmployee());
                        taskFacade.addLogEvent(task, DictLogEvents.TASK_ASSIGNED, task.getAuthor());
                        task.getState().setCurrentState(stateFacade.getRunningState());
                    } else {
                        errors.add(new Tuple("OneTasksFailedSetRole", new Object[]{task.getId(), task.getName()}));
                    }
                }
            });
    }
    
    /**
     * Запуск подпроцессов на выполнение
     * @param exeSubProc 
     */
    private Set<Process> initSubProcesses(Process mainProcess, Set<SubProcessElem> exeSubProc, Map<String, Object> params, User curUser, Set<Tuple> errors){        
        if (exeSubProc.isEmpty()) return new HashSet<>();
        Set<Process> processesForShow = new HashSet<>();
        exeSubProc.forEach(subProcElem->{
            Process subProcess = subProcElem.getSubProcess();
            if (subProcess == null){
                ProcessType owner = processTypeFacade.find(subProcElem.getProctypeId());
                if (owner == null){
                    errors.add(new Tuple("ObjectWithIDNotFound", new Object[]{"ProcessType", subProcElem.getProctypeId()})); 
                    return;
                }
                subProcess = processFacade.createSubProcess(owner, mainProcess, curUser, subProcElem);                
            } 
            
            boolean sendNotAgree = params.containsKey(ProcessParams.PARAM_SEND_NOTAGREE);
            
            //если отправить только не согласовавшим и подпроцесс согласован, то обходим подпроцесс
            if (sendNotAgree && Objects.equals(DictStates.STATE_COMPLETED, subProcess.getState().getCurrentState().getId()) ){                
                run(subProcess, subProcElem, new HashSet<>(), curUser, params, errors);
            } else {
                subProcess.setResult(null);
                subProcess.setFactExecDate(null);
                processFacade.setRoleOwner(subProcess, curUser);
                processFacade.edit(subProcess);
                if (subProcElem.isShowCard()){
                    processesForShow.add(subProcess);                    
                } else {                
                    start(subProcess, curUser, params,  errors);
                }
            }
        });
        return processesForShow;
    }
    
    /**
     * Запуск процесса на выполнение
     * @param process
     * @param currentUser
     * @param params
     * @param errors 
     * @return  
     */
    @Override
    public Set<BaseDict> start(Process process, User currentUser, Map<String, Object> params, Set<Tuple> errors) {  
        Scheme scheme = process.getScheme();
        if (scheme == null){            
            scheme = initScheme(process, null, currentUser, errors);            
        } else {
            unpackScheme(scheme, currentUser);
            clearScheme(process, currentUser, params, errors);
        }      
        validateScheme(scheme, true, errors);
        if (!errors.isEmpty()) return new HashSet<>();
                
        processFacade.actualizeProcessRoles(process);
        processFacade.edit(process);    //сохраняем изменения
        
        /* Перевод документа в статус Действующий */
        Doc doc = process.getDocument();
        if (doc != null){
            doc = docFacade.find(doc.getId());
            doc.addUserInRole(DictRoles.ROLE_EDITOR, process.getCurator().getEmployee());
            docFacade.changeState(doc, DictStates.STATE_VALID);
        }
        
        process.getState().setCurrentState(stateFacade.getRunningState());
        process.setBeginDate(new Date());
        
        WFConnectedElem startElement = scheme.getElements().getStartElem();
        startElement.setDone(true);
        
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_START, currentUser);
        return run(process, startElement, new HashSet<>(), currentUser, params, errors);
    }

    /**
     * Прерывание выполнения процесса
     * @param process
     * @param currentUser
     */
    @Override
    public void stop(Process process, User currentUser) {
        State stateCancel = stateFacade.getCanceledState();
        
        Doc doc = process.getDocument();
        if (doc != null){
            doc = docFacade.find(doc.getId());            
            docFacade.returnToPrevState(doc);
        }
        
        cancelSubProc(process, currentUser);        //отмена всех запущенных подпроцессов        
        cancelTasks(process, currentUser);          //отмена всех запущенных задач
        stopTimers(process);                       //остановка таймеров        

        process.getState().setCurrentState(stateCancel);
        process.setResult("ProcessСanceled");
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_CANCELED, currentUser);
        processFacade.edit(process);
    }
    
    /**
     * Остановка всех запущенных подпроцессов
     * @param process          
     * @param currentUser 
     */
    private void cancelSubProc(Process process, User currentUser){         
        process.getChildItems().stream()
                .filter(subProc->DictStates.STATE_DRAFT != subProc.getState().getCurrentState().getId()) //отмена всех, кроме черновиков
                .forEach(subProc->stop(subProc, currentUser));
    }
    
    /**
     * Отмена выполненных и остановка всех запущенных задач с установкой признака причины остановки
     * @param process          
     * @param currentUser 
     */
    private void cancelTasks(Process process, User currentUser){ 
        State stateCancel = stateFacade.getCanceledState();
        process.getScheme().getTasks().stream()
                .filter(task->DictStates.STATE_RUNNING == task.getState().getCurrentState().getId()) //отмена всех запущенных задач
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
    private void finish(Process process, ExitElem exitElem, Set<SubProcessElem> exeSubProc, User currentUser, Set<Tuple> errors) {
        if (exitElem.getFinalize()){
            State state = stateFacade.find(exitElem.getFinishStateId());
            process.getState().setCurrentState(state);            
            stopTasks(process, currentUser); //отмена всех запущенных и не завершённых задач
            stopTimers(process);
        }        
        process.setFactExecDate(new Date());
        processFacade.addLogEvent(process, DictLogEvents.PROCESS_FINISHED, currentUser);
        //если есть главный процесс, то текущий является подпроцессом и выполняем переход в главный процесс
        if (process.getParent() != null){ 
            Process mainProcess = processFacade.find(process.getParent().getId());
            unpackScheme(mainProcess.getScheme(), currentUser);
            
            Map<String, Object> params = new HashMap<>();
            params.put("subprocess", process);
            
            SubProcessElem subProcessEl = findSubProcEl(mainProcess.getScheme(), process);
            subProcessEl.setShowCard(false); //сброс признака открытия формы, т.к. подпроцесс выполнен
            run(mainProcess, subProcessEl, exeSubProc, currentUser, params, errors);
        }
    }

    /**
     * Выполняет движение процесса по маршруту от указанного начального элемента
     * по всем исходящим из него переходам (connectors)
     * и останавливается после запуска всех актуальных задач и/или достижения конца процесса
     * @param process
     * @param startElement
     * @param exeSubProc
     * @param errors 
     * @param params 
     * @param currentUser 
     * @return - возвращает список объектов, которые должны быть показаны пользователю
     */
    @Override
    public Set<BaseDict> run(Process process, WFConnectedElem startElement, Set<SubProcessElem> exeSubProc, User currentUser, Map<String, Object> params, Set<Tuple> errors) {   
        Set<BaseDict> forShow = new HashSet<>();
        Set<Task> exeTasks = new HashSet<>();        
        doRun(startElement.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
        if (!errors.isEmpty()){
            ctx.setRollbackOnly();
        } else {
            startTasks(process, exeTasks, currentUser, params, errors);
            forShow.addAll(initSubProcesses(process, exeSubProc, params, currentUser, errors));
            if (!errors.isEmpty()){
                ctx.setRollbackOnly();
            } else {
                packScheme(process.getScheme());
                processFacade.edit(process);
            }
        }
        return forShow;
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
    private void doRun(Set<AnchorElem> anchors, Process process, Set<Task> exeTasks, Set<SubProcessElem> exeSubProc, User currentUser, Map<String, Object> params, Set<Tuple> errors){
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
                        findProcedures(connectors, scheme.getElements().getProcedures())
                            .forEach(procedureElem->{
                                executeProcedure(procedureElem, scheme, errors);
                                procedureElem.setEnter(true);
                                doRun(procedureElem.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                            });

                        //формирование списка задач, которые должны быть активированы
                        connectors.stream()
                            .map(connector->scheme.getElements().getTasks().get(connector.getTo().getOwnerUID()))
                            .filter(element->Objects.nonNull(element))
                            .forEach(taskElem-> {
                                taskElem.setEnter(true);
                                if (params.containsKey(EXECUTED_TASKS)){
                                    taskElem.getTasksExec().addAll((List<Integer>)params.get(EXECUTED_TASKS));
                                }
                                scheme.getTasks().stream()
                                        .filter(task->Objects.equals(taskElem.getUid(), task.getTaskLinkUID()))
                                        .forEach(task->{
                                            taskElem.setTask(task);
                                            exeTasks.add(task);
                                        });
                            });

                        //обрабатываем условия
                        findTargetConditions(connectors, scheme.getElements().getConditions())
                            .stream()
                                .forEach(condition->{
                                    condition.setEnter(true);
                                    condition.setDone(true);
                                    if (checkCondition(condition, scheme, params, errors)){
                                        doRun(condition.getSecussAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                                    } else {
                                        doRun(condition.getFailAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                                    }
                                });

                        //изменяем изменения статусы документа
                        findTargetStates(connectors, scheme.getElements().getStates())
                            .forEach(stateElem->{
                                    stateElem.setEnter(true);
                                    stateElem.setDone(true);
                                    StatusesDoc status = changingDoc(stateElem, scheme, errors);
                                    if (status != null && stateElem.getIsSaveInProc()){
                                        process.setResult(status.getBundleName());
                                    }
                                    doRun(stateElem.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                                });

                        //обрабатываем логические элементы
                        findTargetLogics(connectors, scheme.getElements().getLogics())
                            .stream()
                                .filter(logic-> canExeLogic(logic, scheme, params))
                                .forEach(logic-> {
                                    logic.setEnter(true);
                                    logic.setDone(true);
                                    params.put(EXECUTED_TASKS, logic.getTasksExec());
                                    doRun(logic.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                                });

                        //обрабатываем подпроцессы 
                        findSubProcElems(connectors, scheme.getElements().getSubprocesses())
                                .stream()
                                .forEach(subprocEl-> {
                                    subprocEl.setEnter(true);
                                    exeSubProc.add(subprocEl);
                                });
                        
                        //обрабатываем таймеры
                        findTimers(connectors, scheme.getElements().getTimers())
                            .stream()
                                .forEach(timerEl-> {
                                    timerEl.setEnter(true);
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
                                        doRun(timerEl.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                                    }
                                });
                        
                        //обрабатываем сообщения
                        findMessages(connectors, scheme.getElements().getMessages())
                            .stream()
                            .forEach(messageEl->{
                                messageEl.setEnter(true);
                                messageEl.setDone(true);
                                sendMessage(process, messageEl, currentUser);
                                doRun(messageEl.getAnchors(), process, exeTasks, exeSubProc, currentUser, params, errors);
                            });
                                
                        //обработка выходов из процесса -> переход в связанный(е) процесс(ы)
                        findTargetExits(connectors, scheme.getElements().getExits())
                            .forEach(exitElem->{
                                exitElem.setEnter(true);
                                exitElem.setDone(true);
                                final StatusesDoc status = getStatus(exitElem.getStatusId(), errors);                                
                                process.setResult(status != null ? status.getBundleName() : null);
                                finish(process, exitElem, exeSubProc, currentUser, errors);
                            });
                    }
                });
    }        
    
    /* *** ПОИСКИ ЭЛЕМЕНТОВ В МОДЕЛИ *** */
    
    /**
     * Находит элемент подпроцесса для заданного процесса в указанной модели 
     * @return 
     */
    private SubProcessElem findSubProcEl(Scheme scheme, Process process){
        return scheme.getElements().getSubprocesses().values().stream()
                .filter(subProcEl->Objects.equals(process.getLinkUID(), subProcEl.getUid())).findFirst().orElse(null);
    }
    
    /**
     * Находит элементы подпроцессов в модели процесса к которым идут коннекторы
     * @return 
     */    
    private Set<SubProcessElem> findSubProcElems(List<ConnectorElem> connectors, Map<String, SubProcessElem> elements){
        return connectors.stream()
                .map(connector->elements.get(connector.getTo().getOwnerUID()))
                .filter(element->Objects.nonNull(element))
                .collect(Collectors.toSet());
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
    private StatusesDoc changingDoc(StatusElem stateElem, Scheme scheme, Set<Tuple> errors){
        final StatusesDoc status = getStatus(stateElem.getDocStatusId(), errors);
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
    
    private State getNewDocState(StatusElem stateElem, Set<Tuple> errors){
        if (stateElem.getDocStateId() == null) return null;
        State state = stateFacade.find(stateElem.getDocStateId());
        if (state == null){
            errors.add(new Tuple("StateProcessRouteNotFound", new Object[]{stateElem.getDocStateId()}));
        }
        return state;
    }
    
    private StatusesDoc getStatus(Integer docStatusId, Set<Tuple> errors ){        
        if (docStatusId == null) return null;
        StatusesDoc status = statusesFacade.find(docStatusId);
        if (status == null){
            errors.add(new Tuple("StateProcessRouteNotFound", new Object[]{docStatusId}));            
        }
        return status;
    }

    /* *** УВЕДОМЛЕНИЯ  *** */
    
    private void sendMessage(Process process, MessageElem message, User user){        
        StringBuilder sb = new StringBuilder();
        sb.append(message.getContent());        
        processFacade.sendRoleMessage(process, message.getRecipientsJSON(), "NotifyFromProcess", sb, user);
    }
    
    /* *** ПРОЦЕДУРЫ *** */

    /**
     * Выполнение процедуры
     * @param procedureElem
     * @param scheme
     * @param errors 
     */
    private void executeProcedure(ProcedureElem procedureElem, Scheme scheme, Set<Tuple> errors){
        Procedure procedure = procedureFacade.find(procedureElem.getProcedureId());
        if (procedure == null) return;
        switch (procedure .getMethod()) {
            case "regProcess": {
                Process process = scheme.getProcess();
                if (StringUtils.isBlank(process.getRegNumber())){
                    processNumerator.registrate(process, errors);
                }
                break;
            }
            case "regDoc": {
                Doc doc = scheme.getProcess().getDocument();
                if (doc != null && StringUtils.isBlank(doc.getRegNumber())){
                    docNumeratorService.registratedDoc(doc, errors);
                }
                break;
            }
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
    private boolean checkCondition(ConditionElem conditionEl, Scheme scheme, Map<String, Object> params, Set<Tuple> errors){
        Boolean result = false;
        Condition condition = (Condition)conditionFacade.find(conditionEl.getConditonId());
        if (condition == null){
            errors.add(new Tuple("IncorrectConditionRouteProcess", new Object[]{conditionEl.getConditonId(), scheme.getProcess().getName()}));
            return result;
        }
        switch (condition.getMethod()){
            case "everyoneApproved":{
                result = everyoneApproved(scheme, params);
                break;
            }
            case "lastApproved":{
                result = lastApproved(scheme, params);
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
            case "docIsConcorded":{
                result = docIsConcorded(scheme, params);
                break;
            }
            case "agreedUponEmployee":{                
                result = agreedUponEmployee(scheme, conditionEl.getParams());
                break;
            }
            case "subProcessInState":{
                if (!params.containsKey("subprocess")) return false;
                result = processInState((Process) params.get("subprocess"), conditionEl.getParams());
                break;
            }
            case "processInState":{                
                result = processInState(scheme.getProcess(), conditionEl.getParams());
                break;
            }
        }
        return result;
    }
    
    /**
     * Условие "Все одобрили?". Проверяет, есть ли кто-то отклонивший документ в цепочке согласований
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
     * Условие "Последний согласовал?". Проверяет, согласовал ли документ, последний согласующий в цепочке согласований
     * @param scheme
     * @return 
     */
    private boolean lastApproved(Scheme scheme, Map<String, Object> params){
        Task lastTask = (Task) params.get(LAST_TASK);
        List<Integer> taskIds = (List<Integer>) params.get(EXECUTED_TASKS);                
        taskIds.clear();
        taskIds.add(lastTask.getId());
        params.put(EXECUTED_TASKS, taskIds); //оставляем только последнюю задачу, так как её результат важнее
        LOGGER.log(Level.INFO, null, "lastApproved: "+lastTask.getId());
        LOGGER.log(Level.INFO, null, "lastApproved: "+lastTask.getResult());
        LOGGER.log(Level.INFO, null, "lastApproved: "+DateUtils.dateToString(lastTask.getFactExecDate(), DateFormat.SHORT, DateFormat.MEDIUM, userFacade.getUserLocale(userFacade.getAdmin())));
        LOGGER.log(Level.INFO, null, "lastApproved: "+taskIds.size());        
        return !Objects.equals(DictResults.RESULT_REFUSED, lastTask.getResult());        
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
    private boolean agreedUponEmployee(Scheme scheme, Map<String, Object> params){                
        if (!params.containsKey("staff")) return false;
        Integer staffId = (Integer)params.get("staff");
        if (staffId == null) return false;
        Staff staff = staffFacade.find(staffId);
        return scheme.getTasks().stream()
                .filter(task->Objects.equals(staff, task.getOwner()) 
                        && Objects.equals(DictStates.STATE_COMPLETED, task.getState().getCurrentState().getId())
                        && StringUtils.isNotBlank(task.getResult())                        
                        && !Objects.equals(DictResults.RESULT_REFUSED, task.getResult()))
                .findFirst().orElse(null) != null;
    }
    
    /**
     * Условие: документ согласован? Проверяет последний статус документа
     * @param scheme
     * @param params
     * @return 
     */
    private boolean docIsConcorded(Scheme scheme, Map<String, Object> params){
        Process process = processFacade.find(scheme.getProcess().getId());
        Doc doc = process.getDocument();
        if (doc == null) return false;
        return doc.getDocsStatusList().stream()
                .filter(st->DictDocStatus.CONCORDED == st.getStatus().getId() && st.getValue())
                .sorted(Comparator.comparing(DocStatuses::getDateStatus, nullsFirst(naturalOrder())).reversed())
                .findFirst()
                .orElse(null) != null;
    }
    
    /**
     * Условие: процесс находится в заданном состоянии
     * @param process
     * @param params
     * @return 
     */
    private boolean processInState(Process process, Map<String, Object> params){
        if (!params.containsKey("stateId")) return false;
        Integer stateId = (Integer)params.get("stateId");
        return Objects.equals(stateId, process.getState().getCurrentState().getId());
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
                        
        //добавляем в лист согласования записи, если таких там нет
        newReports.removeAll(procReports);        
        procReports.addAll(newReports);
        
        processFacade.edit(process);
    }
        
}