package com.maxfill.model.process;

import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.process.types.ProcessTypesFacade;
import com.maxfill.facade.BaseDictWithRolesFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.companies.Company;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.messages.UserMessagesFacade;
import com.maxfill.model.process.remarks.RemarkFacade;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.types.ProcessType;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
import com.maxfill.model.task.TaskFacade;
import com.maxfill.model.users.User;
import com.maxfill.utils.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Query;
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
    private DocFacade docFacade;
    @EJB
    private UserMessagesFacade messagesFacade;
    @EJB
    private RemarkFacade remarkFacade;
    @EJB
    private TaskFacade taskFacade;
    
    public ProcessFacade() {
        super(Process.class, ProcessLog.class, ProcessStates.class);
    }

    @Override
    public int replaceItem(Process oldItem, Process newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return 20;
    }

    @Override
    public void setSpecAtrForNewItem(Process process, Map<String, Object> params) {       
        if (params.containsKey("documents")){
            List<Doc> docs = (List<Doc>)params.get("documents");
            if (!docs.isEmpty()){
                process.setDocs(docs);
                process.setDocument(docs.get(0));
            }
            makeProcName(process);
        }
        if (params.containsKey("author")) {
            User user = (User)params.get("author");
            if (user.getStaff() != null){
                Staff curator = user.getStaff();
                if (curator != null){
                    process.setCurator(curator);                    
                }
            }
        }
        /*
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(process, numeratorPattern, null, new Date());
        process.setRegNumber(number);
        */
        addRole(process, DictRoles.ROLE_CONCORDER);
        addRole(process, DictRoles.ROLE_CURATOR);
                
    }

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
    public void remove(Process process){
        messagesFacade.removeMessageByProcess(process);
        process.getScheme().getTasks().forEach(task->{
                taskFacade.removeItemLogs(task);
                messagesFacade.removeMessageByTask(task);
            });
        remarkFacade.removeRemarksByProcess(process);        
        super.remove(process);
    } 

    @Override
    public void edit(Process process) {
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
        super.edit(process); //To change body of generated methods, choose Tools | Templates.
    }        
            
    @Override
    protected String getItemFormPath(){
        return "/processes/process-explorer.xhtml";
    }  
    
    public Long findCountDocLinks(Doc doc){
        getEntityManager().getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.document), doc));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    public Long findCountStaffLinks(Staff staff){
        getEntityManager().getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.curator), staff));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    public Long findCountCompanyLinks(Company company){
        getEntityManager().getEntityManagerFactory().getCache().evict(Process.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Process> root = cq.from(Process.class);
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get(Process_.company), company));                
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        cq.select(builder.count(root)).where(builder.and(predicates));
        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
    }
    
    /**
     * Формирует список результатов процессов
     * @return 
     */
    public List<String> findProcessResults(){        
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq= builder.createQuery();
        Root root = cq.from(itemClass);
        Expression<String> resultName = root.get(Process_.result);
        cq.multiselect(resultName);
        cq.groupBy(root.get(Process_.result));
        Predicate crit1 = builder.equal(root.get("deleted"), false);
        Predicate crit2 = builder.isNotNull(root.get(Process_.result));
        cq.where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(root.get(Process_.result)));
        Query query = getEntityManager().createQuery(cq);
        return query.getResultList();        
    }
}