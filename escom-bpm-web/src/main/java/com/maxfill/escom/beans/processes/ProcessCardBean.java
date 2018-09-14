package com.maxfill.escom.beans.processes;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.states.StateFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.reports.ProcReport;
import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.timers.ProcTimer;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.task.Task;
import com.maxfill.model.users.User;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.utils.DateUtils;
import java.io.IOException;
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
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.inject.Inject;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.diagram.Element;

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
    private AttacheBean attacheBean;    
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;

    @EJB
    private StateFacade stateFacade;

    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;          
    
    private final List<Attaches> attaches = new ArrayList<>();
    
    private Doc selectedDoc;    
    
    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }

    @Override
    protected void doPrepareOpen(Process item) {
    }
    
    @Override
    public void onAfterFormLoad() {
        if (getEditedItem() == null) return;
        if (getEditedItem().getScheme() == null){
            Scheme scheme = new Scheme(getEditedItem());
            getEditedItem().setScheme(scheme);
        } 
    }

    /**
     * Перед сохранением процесса
     * @param process
     */
    @Override
    protected void onBeforeSaveItem(Process process){                 
        workflow.packScheme(getScheme());
        super.onBeforeSaveItem(process);
    }

    @Override
    protected void checkItemBeforeSave(Process process, Set<String> errors){
        checkDocument(getEditedItem().getDocument(), errors);
        super.checkItemBeforeSave(process, errors);
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
        List<Task> liveTasks = getTasksFromModel();
        List<Task> forRemoveTasks = new ArrayList<>(scheme.getTasks());
        forRemoveTasks.removeAll(liveTasks); //в списке остались только задачи, которые нужно удалить
        scheme.getTasks().removeAll(forRemoveTasks);     
        List<ProcReport> removeRepors = forRemoveTasks.stream()
                .map(task->process.getReports().stream()
                        .filter(report-> report.getDateCreate() == null && Objects.equals(task.getOwner(), report.getExecutor()))
                        .findFirst().orElse(null))
                .collect(Collectors.toList());
        Set<ProcReport> procReports = process.getReports();        
        //создаём записи в "листе согласования" для всех участников согласования из модели процесса        
        Set<ProcReport> newReports = liveTasks.stream()
                .filter(task->task.getOwner() != null)
                .map(task -> new ProcReport(getCurrentUser(), task.getOwner(), process))
                .collect(Collectors.toSet()); //список исполнителей из модели
        procReports.removeAll(removeRepors);
        procReports.addAll(newReports);        
        //сохраняем только оставшиеся на схеме таймеры, а старые удаляем
        List<ProcTimer> liveTimers = getProcTimersFromModel();
        List<ProcTimer> forRemoveTimers = new ArrayList<>(getScheme().getTimers());
        forRemoveTimers.removeAll(liveTimers);
        if (!forRemoveTimers.isEmpty()){
            getScheme().getTimers().removeAll(forRemoveTimers);
        }
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:concorderList");
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
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:concorderList");
    }
    
    /* МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ */
    
    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        Set<String> errors = new HashSet<>();
        validatePlanDate(getEditedItem().getPlanExecDate(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){            
            workflow.start(getEditedItem(), getCurrentUser(), errors);
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
    
    /* ПРОЧИЕ МЕТОДЫ */
    
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
        return Objects.equals(DictEditMode.VIEW_MODE, getTypeEdit()) || getEditedItem().isRunning() || getEditedItem().isCompleted() ;
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
        if (items.isEmpty()) return;
        Doc doc = items.get(0);
        onItemChange();
        getEditedItem().setDocument(doc);
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
     * Обработка события добавления файла в процесс с созданием документа
     */
    public void onAddFile(){
        User author = getCurrentUser();
        Folder folder = author.getInbox();
        if (folder == null){
            MsgUtils.errorMsg("NoDefaultUserFolderSpecified");
            return;
        }
        Attaches attache = attaches.get(0); 
        Map<String, Object> params = new HashMap<>();
        params.put("attache", attache);
        params.put("name", attache.getName());
        Doc doc = docFacade.createDocInUserFolder(attache.getName(), author, folder, attache);
        getEditedItem().getDocs().add(doc);
        if (getEditedItem().getDocument() == null){
            getEditedItem().setDocument(doc);
        }
        onItemChange();
    }
    
    /* Загрузка файла через контрол на форме карточки процеса */
    public void onUploadFile(FileUploadEvent event) throws IOException{       
        attaches.clear();
        UploadedFile uploadFile = EscomFileUtils.handleUploadFile(event);        
        attaches.add(attacheBean.uploadAtache(uploadFile));
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
        if (getEditedItem() == null) return null;
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