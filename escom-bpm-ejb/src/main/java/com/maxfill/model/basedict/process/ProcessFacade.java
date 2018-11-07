package com.maxfill.model.basedict.process;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.procTempl.ProcessTemplFacade;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.schemes.elements.SubProcessElem;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.services.worktime.WorkTimeService;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Фасад для сущности "Процессы"
 */
@Stateless
public class ProcessFacade extends BaseDictWithRolesFacade<Process, ProcessType, ProcessLog, ProcessStates>{

    @EJB
    private ProcessTypesFacade processTypesFacade;
    @EJB
    private ProcessTemplFacade procTemplFacade;
    @EJB
    private DocFacade docFacade;
    @EJB
    private UserMessagesFacade messagesFacade;
    @EJB
    private RemarkFacade remarkFacade;
    @EJB
    private TaskFacade taskFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private WorkTimeService workTimeService;
    @EJB
    private Workflow workflow;
            
    public ProcessFacade() {
        super(Process.class, ProcessLog.class, ProcessStates.class);
    }

    @Override
    public void remove(Process process){
        messagesFacade.removeMessageByProcess(process);
        if (process.getScheme() != null){
            process.getScheme().getTasks().forEach(task->{
                    taskFacade.removeItemLogs(task);
                    messagesFacade.removeMessageByTask(task);
                });
        }
        remarkFacade.removeRemarksByProcess(process);
        super.remove(process);
    } 

    @Override
    public void edit(Process process) { 
        /*        
        List<Task> liveTasks = process.getScheme().getTasks();
        Process oldProcess = find(process.getId());        
        List<Task> forRemoveTasks = new ArrayList<>(oldProcess.getScheme().getTasks());
        forRemoveTasks.removeAll(liveTasks);
        
        Set<ProcReport> procReports = process.getReports();
        forRemoveTasks.forEach(task->{
                taskFacade.removeItemLogs(task);
                messagesFacade.removeMessageByTask(task);
                procReports.stream()
                    .filter(report->Objects.equals(task, report.getTask()))
                    .forEach(report->report.setTask(null));
            });
        */
        super.edit(process); //To change body of generated methods, choose Tools | Templates.
    }   
 
    /**
     * Создание подпроцесса
     * @param owner
     * @param parent
     * @param author
     * @param subProcEl
     * @return 
     */
    public Process createSubProcess(ProcessType owner, Process parent, User author, SubProcessElem subProcEl){        
        StringBuilder sb = new StringBuilder();
        sb.append(ItemUtils.getBandleLabel("SubProcess", userFacade.getUserLocale(author)));
        sb.append(": ").append(subProcEl.getCaption());
        
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("documents", Collections.singletonList(parent.getDocument()));        
        createParams.put("company", parent.getCompany());
        createParams.put("curator", parent.getCurator());
        if (subProcEl.getProctemplId() != null){
            createParams.put("template", procTemplFacade.find(subProcEl.getProctemplId()));
        }
        Process subProcess = createItem(author, parent, owner, createParams);
        
        subProcess.setDocs(parent.getDocs());                    
        subProcess.setName(sb.toString());
        subProcess.setLinkUID(subProcEl.getUid());
        subProcEl.setSubProcess(subProcess);
        
        create(subProcess);
        return subProcess;
    }    
    
    @Override
    public int replaceItem(Process oldItem, Process newItem) {
        return 0;
    }

    /**
     * Установка специальных атрибутов объекта при его создании
     * @param process
     * @param createParams 
     */
    @Override
    public void setSpecAtrForNewItem(Process process, Map<String, Object> createParams){
        process.setDeltaDeadLine(0);
        process.setDeadLineType("data");              
        
        ProcessType processType = processTypesFacade.getProcTypeForOpt(process.getOwner());
        if (processType != null){
            Integer deltasec = processType.getDefaultDeltaDeadLine(); //срок исполнения в секундах
            if (deltasec != null){
                process.setDeltaDeadLine(deltasec);
                Date planDate = workTimeService.calcWorkDayByCompany(new Date(), deltasec, process.getCompany());
                process.setPlanExecDate(planDate);
            }
        }
        
        if (createParams.containsKey("company")){
            process.setCompany((Company) createParams.get("company"));
        } else {
            process.setCompany(staffFacade.findCompanyForStaff(process.getAuthor().getStaff())); 
        }
        
        if (createParams.containsKey("documents")){
            List<Doc> docs = (List<Doc>)createParams.get("documents");
            if (!docs.isEmpty()){
                process.setDocs(docs);
                process.setDocument(docs.get(0));
            }            
            makeProcName(process);
        }
        
        if (createParams.containsKey("author")) {
            User user = (User)createParams.get("author");
            if (user.getStaff() != null){
                Staff curator = user.getStaff();
                if (curator != null){
                    setRoleCurator(process, curator);            
                }
            }
        }        
        if (createParams.containsKey("curator")) {
            setRoleCurator(process, (Staff)createParams.get("curator"));
        } else { 
            if (process.getAuthor().getStaff() != null){
                setRoleCurator(process, process.getAuthor().getStaff());
            }
        }
        
        if (createParams.containsKey("template")){
            ProcTempl procTempl = (ProcTempl)createParams.get("template");
            workflow.initScheme(process, procTempl, process.getAuthor(), new HashSet<>());
        }
    }

    /**
     * Отбор процессов по документу
     * @param doc
     * @param currentUser
     * @return 
     */
    public List<Process> findProcessesByDoc(Doc doc, User currentUser){        
        em.getEntityManagerFactory().getCache().evict(itemClass);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(itemClass);
        Root c = cq.from(itemClass);
        Predicate crit1 = builder.equal(c.get(Process_.document), doc);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        Predicate crit3 = builder.isNull(c.get("parent"));
        Predicate crit4 = builder.equal(c.get("actual"), true);
        cq.select(c).where(builder.and(crit1, crit2, crit3, crit4));        
        TypedQuery<Process> query = em.createQuery(cq);         
        return query.getResultStream()
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }  
    
    /* *** РОЛИ *** */    
    
    public void setRoleCurator(Process process, Staff curator){
        process.setCurator(curator);
        process.doSetSingleRole(DictRoles.ROLE_CURATOR, curator.getEmployee());
    }
    
    /**
     * Актуализирует роли процесса
     * @param process 
     */
    public void actualizeProcessRoles(Process process){        
        clearRoles(process);
        setRoleCurator(process, process.getCurator());
        setRoleOwner(process, process.getAuthor());
        if (process.getScheme() != null){
            process.getScheme().getTasks().stream()
                    .filter(task->task.getOwner() != null)
                    .forEach(task->process.addUserInRole(task.getRoleInProc().getRoleFieldName(), task.getOwner().getEmployee()));
        }
    }
    
    /**
     * Формирует список пользователей, входящих в роль указанного процесса включая все подпроцессы
     * @param parent
     * @param roleName
     * @param currentUser
     * @return 
     */
    public List<User> getUsersProcessRole(Process parent, String roleName, User currentUser){
        Set<User> users = new HashSet<>();
        makeUsersFromProcessRole(parent, users, roleName, currentUser);
        return new ArrayList<>(users);
    }
    
    private void makeUsersFromProcessRole(Process parent, Set<User> users, String roleName, User currentUser){
        users.addAll(getUsersFromRole(parent, roleName, currentUser));
        parent.getChildItems().forEach(process->makeUsersFromProcessRole(process, users, roleName, currentUser));
    }
    
    /* *** ВАЛИДАЦИЯ *** */
    
    /**
     * Проверка возможности ручного запуска процесса
     * @param process
     * @param currentUser
     * @param errors
     * @return 
     */
    public void validateCanRun(Process process, User currentUser,  Set<Tuple> errors){
        Process parent = process.getParent();
        if (parent == null) return; //нет ограничений на запуск, т.к. это не подпроцесс
        if(!Objects.equals(DictStates.STATE_RUNNING, process.getParent().getState().getCurrentState().getId())){
            errors.add(new Tuple("SubprocessCannotBeStarted", new Object[]{}));
            return;
        }
        Scheme scheme = parent.getScheme();
        workflow.unpackScheme(scheme, currentUser);
        SubProcessElem elem = scheme.getElements().getSubprocesses().entrySet().stream()
                .filter(entry->Objects.equals(process.getLinkUID(), entry.getKey()))
                .map(entry->entry.getValue())
                .findFirst().orElse(null);
        if (elem != null && !elem.isEnter()){            
            errors.add(new Tuple("SubprocessCannotBeStarted", new Object[]{}));
        }
    }
    
    /* *** *** */
    
    /**
     * Формирование дефолтного наименования для процесса
     * @param process 
     */
    public void makeProcName(Process process){
        StringBuilder sb = new StringBuilder();
        if (process.getOwner() != null){
            sb.append(process.getOwner().getName());
        }
        if (process.getDocs() != null && !process.getDocs().isEmpty()){
            Iterator iterator = process.getDocs().iterator(); 
            Doc doc = (Doc)iterator.next();            
            if (doc != null){
                sb.append(" <").append(doc.getFullName()).append(">");
            }
        }
        process.setName(sb.toString());
    }
    
    /**
     * Получение прав доступа к процессу
     * @param item
     * @param user
     * @return 
     */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user);
        }
        if (item.getOwner() != null) {
            Rights childRight = processTypesFacade.getRightForChild(item.getOwner());
            if (childRight != null) {
                return childRight;
            }
        }
        return getDefaultRights(item);
    }
    
    @Override
    public Tuple findDublicateExcludeItem(Process item){       
        return new Tuple(false, null);        
    }
        
    /**
     * Создание процесса c созданием и прикреплением к нему документов созданных из файлов
     * @param owner
     * @param author
     * @param attaches
     * @param errors
     * @return 
     */
    public Process createProcFromFile(ProcessType owner, User author, List<Attaches> attaches, Set<String> errors){
        if (owner == null){
            errors.add("DoNotSpecifyTypeProcess");
            return null;
        }
        Folder folder = author.getInbox();
        if (folder == null){
            errors.add("NoDefaultUserFolderSpecified");
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        List<Doc> docs = new ArrayList<>();
        attaches.forEach(attach->{
            Doc doc = docFacade.createDocInUserFolder(attach.getName(), author, folder, attach);
            docs.add(doc);
        });        
        params.put("documents", docs);
        Process process = createItem(author, null, owner, params);
        makeRightItem(process, author);
        return process;
    }                
            
    @Override
    protected String getItemFormPath(){
        return "/processes/process-explorer.xhtml";
    }  
    
    public Long findCountDocLinks(Doc doc){
        em.getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.document), doc));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    public Long findCountStaffLinks(Staff staff){
        em.getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.curator), staff));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    public Long findCountCompanyLinks(Company company){
        em.getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.company), company));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = em.createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    /**
     * Формирует список результатов процессов
     * @return 
     */
    public List<String> findProcessResults(){        
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery cq= builder.createQuery();
        Root root = cq.from(itemClass);
        Expression<String> resultName = root.get(Process_.result);
        cq.multiselect(resultName);
        cq.groupBy(root.get(Process_.result));
        Predicate crit1 = builder.equal(root.get("deleted"), false);
        Predicate crit2 = builder.isNotNull(root.get(Process_.result));
        cq.where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(root.get(Process_.result)));
        Query query = em.createQuery(cq);
        return query.getResultList();        
    }
    
    /* *** СЛУЖЕБНЫЕ *** */
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_PROCESS;
    }
}