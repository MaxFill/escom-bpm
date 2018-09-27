package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.remarks.Remark;
import com.maxfill.model.process.remarks.RemarkFacade;
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
    private DocBean docBean;
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private RemarkFacade remarkFacade;
    
    @EJB
    private StateFacade stateFacade;

    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;  
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
     * Обработка события просмотра документа
     */
    public void onViewDocument(){
        Process process = getEditedItem();                
        Doc doc = process.getDocument();
        if (doc == null){
            MsgUtils.errorFormatMsg("ProcessNotContainDoc", new Object[]{process.getName()});
            return;
        }  
        Map<String, List<String>> params = getParamsMap();
        params.put("processID", Collections.singletonList(process.getId().toString()));
        params.put("taskID", Collections.singletonList(getEditedItem().getId().toString()));
        docBean.doViewMainAttache(doc, params);
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
    
    /* *** СХЕМА ПРОЦЕССА *** */ 
    
    public void onOpenScheme(){
        processBean.setDiagramBean(diagramBean);
        diagramBean.setReadOnly(isReadOnly());
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
        
        /*
        List<ProcReport> removeRepors = forRemoveTasks.stream()
                .map(task->process.getReports().stream()
                        .filter(report-> report.getDateCreate() == null 
                                && Objects.equals(task.getOwner(), report.getExecutor()) 
                                && !Objects.equals(curator, report.getExecutor()))
                        .findFirst().orElse(null))
                .collect(Collectors.toList());
        */
        
        //создаём записи в "листе согласования" для участников согласования из модели процесса        
        Set<ProcReport> newReports = liveTasks.stream()
                .filter(task->task.getOwner() != null && task.getConsidInProcReport())
                .map(task -> new ProcReport(getCurrentUser(), task.getOwner(), process))
                .collect(Collectors.toSet()); 

        Set<ProcReport> procReports = process.getReports();        
        List<ProcReport> removeRepors = procReports.stream()            
                .filter(report-> report.getDateCreate() == null && !Objects.equals(curator, report.getExecutor()))                 
                .collect(Collectors.toList());
        procReports.removeAll(removeRepors);        
        procReports.addAll(newReports);   
        
        //в оставшихся отчётах нужно сбросить ссылку на удалённые task
        forRemoveTasks.forEach(task->
                procReports.stream()
                .filter(report->Objects.equals(task, report.getTask()))
                .forEach(report->report.setTask(null))
        );
        
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
    
    /* *** КУРАТОР *** */
    
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
        Process process = processFacade.find(getEditedItem().getId());
        validatePlanDate(process.getPlanExecDate(), errors);
        validateRemarks(process, errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){
            Map<String, Object> params = new HashMap<>();
            params.put(option, true);            
            workflow.start(process, getCurrentUser(), params, errors);
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
    
    /* *** ПРОЧИЕ МЕТОДЫ *** */
    
    /**
     * Проверка срока исполнения
     * @param planDate 
     * @param errors 
     */
    public void validatePlanDate(Date planDate, Set<String> errors){
        Date today = new Date();
        if (today.after(planDate)){
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:dtPlanExecDate");
            input.setValid(false);
            errors.add("DeadlineProcessInPastTime");            
            context.validationFailed();
        }
    }    

    /**
     * Проверка снятых замечаний
     */
    private void validateRemarks(Process process,  Set<String> errors){
        Doc doc = process.getDocument();
        if (doc == null) return;
        Remark remark = doc.getDetailItems().stream().filter(r->!r.isChecked()).findFirst().orElse(null);
        if (remark != null){
            errors.add("CannotStartProcessUnprocessedRemarks");
        }
    }
    
    /**
     * Признак доступности кнопки прерывания процесса
     * @return 
     */
    public boolean isDisableBtnStop(){        
        if (!getEditedItem().isRunning()) return true;
        if (userFacade.isAdmin(getCurrentUser())) return false;
        
        User curator = getEditedItem().getCurator().getEmployee();
        User owner = getEditedItem().getAuthor();
        
        return !(getCurrentUser().equals(owner) || getCurrentUser().equals(curator));
    }
    
    @Override
    public boolean isReadOnly(){        
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || getEditedItem().isRunning();
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
        StringBuilder docName = new StringBuilder();        
        String partnerName = "";
        Doc doc = process.getDocument();
        if (doc != null){            
            docName.append(doc.getFullName());
            if (doc.getPartner() != null){
                partnerName = doc.getPartner().getFullName();
            }
        } 
        List<Object> dataReport = process.getReports().stream()
                .map(report -> {
                    String data = DateUtils.dateToString(report.getDateCreate(),  DateFormat.SHORT, null, getLocale());
                    String dateBegin;
                    if (report.getTask() != null){
                        dateBegin = DateUtils.dateToString(report.getTask().getBeginDate(),  DateFormat.SHORT, null, getLocale());
                    } else {
                        dateBegin = data;
                    }
                    String result = getLabelFromBundle(report.getStatus());
                    String fio = report.getExecutor().getStaffFIO();
                    StringBuilder remarkBuilder = new StringBuilder();
                    remarkFacade.findActualDetailItems(doc, 0, 0, null, null, getCurrentUser())
                            .stream()
                            .filter(remark->Objects.equals(DictStates.STATE_ISSUED, remark.getState().getCurrentState().getId()) //только не снятые замечания 
                                    && remark.getAuthor().equals(report.getExecutor().getEmployee()))
                            .forEach(remark->remarkBuilder.append(remark.getContent()).append(SysParams.LINE_SEPARATOR));                            
                return new ConcordersData(fio, result, data, dateBegin, remarkBuilder.toString());
             }).collect(Collectors.toList());
        
        StringBuilder curator = new StringBuilder();
        if (process.getCurator() != null){
            curator.append(process.getCurator().getStaffFIO()).append(", ").append(process.getCurator().getPhone());
        }
        params.put("CURATOR_NAME", curator.toString());
        params.put("DOC_NAME", docName.toString());  
        params.put("PARTNER_NAME", partnerName);
        params.put("PROCESS_NUMBER", process.getRegNumber());  
        params.put("PROCESS_DATE", DateUtils.dateToString(process.getItemDate(),  DateFormat.SHORT, null, getLocale()));  
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
        private final String dateBegin;
        private final String remark;
        
        public ConcordersData(String fio, String result, String date, String dateBegin, String remark) {
            this.fio = fio;
            this.result = result;
            this.date = date;
            this.dateBegin = dateBegin;
            this.remark = remark;
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
        
        public String getDateBegin(){
            return dateBegin;
        }
        
        public String getRemark(){
            return remark;
        }
    }
}