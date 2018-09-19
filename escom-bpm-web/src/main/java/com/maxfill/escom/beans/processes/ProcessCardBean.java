package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import java.text.DateFormat;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import javax.faces.component.UIInput;
import javax.inject.Inject;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

/**
 * Контролер формы "Карточка процесса"
 */
@Named
@ViewScoped
public class ProcessCardBean extends BaseCardBean<Process>{
    private static final long serialVersionUID = -5558740260204665618L;    
       
    @Inject
    private DiagramBean diagramBean;
    @Inject
    private ProcessBean processBean;
    @Inject
    private TaskBean taskBean;  
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private StateFacade stateFacade;

    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;  
    private Task currentTask;
    private Doc selectedDoc;    
    private String accordDocsTab = null;
    
    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }

    @Override
    protected void doPrepareOpen(Process item) {
    }    

    /**
     * Перед сохранением процесса
     * @param process
     */
    @Override
    protected void onBeforeSaveItem(Process process){
        if (process.getCurator() != null){
            User userCurator = process.getCurator().getEmployee();
            if (!processFacade.checkUserInRole(process, DictRoles.ROLE_CURATOR_ID, userCurator)){                
                process.doSetSingleRole(DictRoles.ROLE_CURATOR, userCurator.getId());
            }
        }        
        Set<Integer> usersIds = process.getScheme().getTasks().stream()
                .filter(task->task.getOwner() != null)
                .map(task->task.getOwner().getEmployee().getId())
                .collect(Collectors.toSet());
        process.doSetMultyRole(DictRoles.ROLE_CONCORDER, usersIds);
        super.onBeforeSaveItem(process);
    }

    @Override
    protected void checkItemBeforeSave(Process process, FacesContext context, Set<String> errors){
        checkDocument(getEditedItem().getDocument(), errors);
        super.checkItemBeforeSave(process, context, errors);
    }    
    
    public void checkDocument(){
        checkDocument(getEditedItem().getDocument(), new HashSet<>());
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:documentPanel");
    }
    public void checkDocument(Doc doc, Set<String> errors){
        if(doc == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getLabelFromBundle("Field")).append(" [");
            sb.append(getLabelFromBundle("Document")).append("] ");
            sb.append(getLabelFromBundle("MustBeFilled"));
            errors.add(sb.toString());            
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:documentPanel_item");
            input.setValid(false);
            //context.addMessage(input.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, sb.toString(),  MsgUtils.getValidateLabel("CHECK_ERROR")));
            context.validationFailed();
        }
    }
        
    /**
     * Переопределение метода закрытия формы. Передаём параметр закрытия, 
     * который устанавливается в зависимости от того, был ли запущен/остановлен процесс
     * @return 
     */
    @Override
    public String doFinalCancelSave() {
        return closeItemForm(exitParam);  //закрыть форму объекта
    }
    
    @Override
    protected String makeHeader(StringBuilder sb){
        if (getEditedItem() != null && StringUtils.isNotEmpty(getEditedItem().getRegNumber())){
            sb.append(" ").append(MsgUtils.getBandleLabel("NumberShort")).append(getEditedItem().getRegNumber()).append(" ");
        }
        return super.makeHeader(sb);
    }
         
    public void onOpenScheme(){
        processBean.setDiagramBean(diagramBean);
        diagramBean.prepareModel(getEditedItem());
        sessionBean.openDialogFrm(DictFrmName.FRM_DIAGRAMMA, getParamsMap());
    }
    
    public void onSchemeClose(SelectEvent event){
        if (event.getObject() == null) return;
        String result = (String) event.getObject();
        if (result.equals(SysParams.EXIT_NOTHING_TODO)) return;                
        Process process = getEditedItem();
        Scheme scheme = process.getScheme();
        Staff curator = process.getCurator();
        List<Task> liveTasks = getTasksFromModel();
        List<Task> forRemoveTasks = new ArrayList<>(scheme.getTasks());
        forRemoveTasks.removeAll(liveTasks); //в списке остались только задачи, которые нужно удалить
        scheme.getTasks().removeAll(forRemoveTasks);     
        List<ProcReport> removeRepors = forRemoveTasks.stream()
                .map(task->process.getReports().stream()
                        .filter(report-> report.getDateCreate() == null 
                                && Objects.equals(task.getOwner(), report.getExecutor()) 
                                && !Objects.equals(curator, report.getExecutor()))
                        .findFirst().orElse(null))
                .collect(Collectors.toList());
                
        //создаём записи в "листе согласования" для всех участников согласования из модели процесса        
        Set<ProcReport> newReports = liveTasks.stream()
                .filter(task->task.getOwner() != null)
                .map(task -> new ProcReport(getCurrentUser(), task.getOwner(), process))
                .collect(Collectors.toSet()); //список исполнителей из модели
        
        Set<ProcReport> procReports = process.getReports();
        procReports.removeAll(removeRepors);
        procReports.addAll(newReports);        
        
        //сохраняем только оставшиеся на схеме таймеры, а старые удаляем
        List<ProcTimer> liveTimers = getProcTimersFromModel();
        List<ProcTimer> forRemoveTimers = new ArrayList<>(getScheme().getTimers());
        forRemoveTimers.removeAll(liveTimers);
        if (!forRemoveTimers.isEmpty()){
            getScheme().getTimers().removeAll(forRemoveTimers);
        }
        
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:accord");
        onItemChange();
    }
    
    public void onChangeCurator(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()){return;}
        Staff item = items.get(0);
        doCangeCurator(item);
    }
    public void onChangeCurator(ValueChangeEvent event){
        Staff user = (Staff) event.getNewValue();        
        doCangeCurator(user);
    }

    private void doCangeCurator(Staff newCurator){
        Process process = getEditedItem();
        Set<ProcReport> procReports = process.getReports();
        
        //удалить запись о старом кураторе в листе согласования
        Staff oldCurator = process.getCurator();
        if (oldCurator != null){
            ProcReport oldCuratorRep = procReports.stream()
                    .filter(rep -> rep.getDateCreate() == null && Objects.equals(rep.getExecutor(), oldCurator))
                    .findFirst()
                    .orElse(null);
            if (oldCuratorRep != null){
                procReports.remove(oldCuratorRep);
            }
        }        
        process.setCurator(newCurator);
        onItemChange();
        //если куратор указан и его нет в листе согласования, то добавим его
        if (newCurator != null){
            ProcReport curatorReport = procReports.stream()
                    .filter(report-> Objects.equals(report.getExecutor(), newCurator))
                    .findFirst().orElse(new ProcReport(getCurrentUser(), newCurator, process));
            procReports.add(curatorReport);
        }
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:accord");
    }
    
    /* МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ */
    
    /**
     * Обработка события запуска процесса на исполнение
     * @param option
     */
    public void onRun(String option){
        Set<String> errors = new HashSet<>();
        validatePlanDate(getEditedItem().getPlanExecDate(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){
            Map<String, Object> params = new HashMap<>();
            params.put(option, true);
            workflow.start(getEditedItem(), getCurrentUser(), params, errors);
            if (!errors.isEmpty()){
                MsgUtils.showErrorsMsg(errors);
            } else {
                getEditedItem().getState().setCurrentState(stateFacade.getRunningState());                
                setItemCurrentState(getEditedItem().getState().getCurrentState());
                MsgUtils.succesMsg("ProcessSuccessfullyLaunched");
            }
            exitParam = SysParams.EXIT_EXECUTE;
            PrimeFaces.current().ajax().update("mainFRM");
        }
    }
    
    /**
     * Обработка события прерывания процесса
     */
    public void onStop(){
        Set<String> errors = new HashSet<>();
        workflow.stop(getEditedItem(), getCurrentUser(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
        } else {
            exitParam = SysParams.EXIT_EXECUTE;
            MsgUtils.warnMsg("ProcessExecutionInterrupted");
        }
    }           
    
    /* *** ЗАДАЧИ *** */
    
    public void onAfterTaskClose(SelectEvent event){
        String action = (String) event.getObject();
        switch (action){
            case SysParams.EXIT_EXECUTE:{
                doAfterTaskChange();
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{
                doAfterTaskChange();
                break;
            }
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
        }
    }
    
    private void doAfterTaskChange(){
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:accord");
        onItemChange();
    }
    
    public void onOpenTask(){
        if (currentTask == null) return;
        setSourceItem(currentTask);
        taskBean.prepEditChildItem(currentTask, getParamsMap());
    }        
    
    /* *** ПРОЧИЕ МЕТОДЫ *** */
    
    /**
     * Проверка срока исполнения
     * @param planDate 
     * @param errors 
     */
    public void validatePlanDate(Date planDate, Set<String> errors){
        Date today = new Date();
        if (today.after(planDate)){
            String errMsg = MsgUtils.getMessageLabel("DeadlineSpecifiedInPastTime");
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:dtPlanExecDate");
            input.setValid(false);
            errors.add(errMsg);            
            context.validationFailed();
        }
    }    

    public boolean isDisableBtnStop(){
        if (getEditedItem() == null) return false;
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || !getEditedItem().isRunning();
    }
    
    @Override
    public boolean isReadOnly(){
        if (getEditedItem() == null) return false;
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || getEditedItem().isRunning() ;
    }    
    
    public void onOpenExeReport(ProcReport report){
        currentReport = report;
    }     
    
    public String getCuratorImage(Staff staff){
        if (staff == null) return "";
        return staff.equals(getEditedItem().getCurator()) ? "/resources/icon/16_inspector.png" : "/resources/icon/user.png";
    }
    
    /* РАБОТА С ДОКУМЕНТАМИ */
    
    public void onDocSelected(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Doc> items = (List<Doc>) event.getObject();
        if (items.isEmpty()){
            MsgUtils.warnMsg("NothingСhosen");
            return;
        }
        Process process = getEditedItem();
        Doc doc = items.get(0);
        onItemChange();
        process.setDocument(doc);
        if (!process.getDocs().contains(doc)){
            process.getDocs().add(doc);
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:accordDocs");
        }
        if (StringUtils.isBlank(process.getName())){
            processFacade.makeProcName(process); 
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:nameItem");
        }
    }
       
    public void onAccordDocsTabChange(TabChangeEvent event){
        Tab tab = event.getTab();
        String tabId = tab.getId();
        if ("tabDocs".equals(tabId)){
            accordDocsTab = "0";
        } else {
            accordDocsTab = null;
        }                
    }
    
    /**
     * Обработка события удаления документа из списка документов процесса
     * @param doc
     */
    public void onDeleteDocFromChilds(Doc doc){
        getEditedItem().getDocs().remove(doc);
        onItemChange();
    }    
    
    /**
     * Обработка события выбора документа(ов) из селектора
     * @param event
     */
    public void onDocsSelected(SelectEvent event){
        List<Doc> docs = (List<Doc>) event.getObject();
        if (docs.isEmpty()) return;        
        getEditedItem().getDocs().addAll(docs); 
        if (getEditedItem().getDocument() == null){
            Doc doc = docs.get(0);
            getEditedItem().setDocument(doc);
        }
        onItemChange();
    }     
    
    /**
     * Обработка события закрытия карточки документа
     * @param event 
     */
    public void onUpdateAfterCloseDocForm(SelectEvent event){
        String exitResult = (String) event.getObject();
        if (!SysParams.EXIT_NOTHING_TODO.equals(exitResult)) {
           PrimeFaces.current().ajax().update("mainFRM:mainTabView:tblDocs");
        }
    }
    
    /* ПЕЧАТЬ */
    
    /**
     * Распечатка листа согласования
     */
    public void onPreViewListConcorder(){
        Map<String, Object> params = prepareReportParams();
        params.put("REPORT_TITLE", MsgUtils.getBandleLabel("ConcorderList"));                
        Process process = getEditedItem();           
        List<Object> dataReport = process.getReports().stream()
                .map(report -> {
                    String data = DateUtils.dateToString(report.getDateCreate(),  DateFormat.SHORT, null, getLocale());
                    String result = getLabelFromBundle(report.getStatus());
                    String fio = report.getExecutor().getFullName();
                return new ConcordersData(fio, result, data);
             }).collect(Collectors.toList());        
        StringBuilder docName = new StringBuilder();
        StringBuilder docNumber = new StringBuilder();
        List<Doc> docs = process.getDocs();
        if (!docs.isEmpty()){
            Doc doc = docs.get(0);            
            docName.append(doc.getFullName());
            docNumber.append(doc.getRegInfo(getLocale()));
        }
        params.put("DOC_NAME", docName.toString());
        params.put("DOC_NUMBER", docNumber.toString());   
        printService.doPrint(dataReport, params, DictPrintTempl.REPORT_CONCORDER_LIST);
        sessionBean.onViewReport(DictPrintTempl.REPORT_CONCORDER_LIST);
    }
    
    /* GETS & SETS */

    public Doc getSelectedDoc() {
        return selectedDoc;
    }
    public void setSelectedDoc(Doc selectedDoc) {
        this.selectedDoc = selectedDoc;
    }    

    public Task getCurrentTask() {
        return currentTask;
    }
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public String getAccordDocsTab() {
        return accordDocsTab;
    }
    public void setAccordDocsTab(String accordDocsTab) {
        this.accordDocsTab = accordDocsTab;
    }
           
    public ProcReport getCurrentReport() {
        return currentReport;
    }
    public void setCurrentReport(ProcReport currentReport) {
        this.currentReport = currentReport;
    }
        
    /**
     * Получение из схемы списка задач процесса
     * @return 
     */    
    public List<Task> getTasksFromModel(){
        Scheme scheme = getScheme(); 
        List<Task> result = new ArrayList<>();
        if (scheme != null){
            result = getScheme().getElements().getTasks().entrySet().stream()
                .filter(tsk->tsk.getValue().getTask() != null)
                .map(tsk->tsk.getValue().getTask())
                .collect(Collectors.toList());
        }
        return result;
    }    
    
    public List<Task> getTasks(){
        return getScheme().getTasks();
    }
    
    /**
     * Получение из модели списка таймеров
     * @return 
     */
    public List<ProcTimer> getProcTimersFromModel(){
        Scheme scheme = getScheme(); 
        List<ProcTimer> result = new ArrayList<>();
        if (scheme != null){
            result = getScheme().getElements().getTimers().entrySet().stream()                
                .map(tsk->tsk.getValue().getProcTimer())
                .collect(Collectors.toList());
        }
        return result;
    }              
    
    public Scheme getScheme(){
        return getEditedItem().getScheme();
    }
    
    public class ConcordersData{
        private final String fio;
        private final String result;
        private final String date;
        
        public ConcordersData(String fio, String result, String date) {
            this.fio = fio;
            this.result = result;
            this.date = date;
        }

        public String getFio() {
            return fio;
        }

        public String getResult() {
            return result;
        }

        public String getDate() {
            return date;
        }
    }
}