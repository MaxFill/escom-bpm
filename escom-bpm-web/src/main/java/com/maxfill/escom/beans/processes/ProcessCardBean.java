package com.maxfill.escom.beans.processes;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.DictStates;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.processes.remarks.RemarkBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.options.RunOptions;
import com.maxfill.model.basedict.process.options.RunOptionsFacade;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.user.User;
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
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;

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
    @Inject
    private RemarkBean remarkBean;
    
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private Workflow workflow;
    @EJB
    private RemarkFacade remarkFacade;
    @EJB
    private RunOptionsFacade runOptionsFacade;
    @EJB
    private ProcessTypesFacade processTypesFacade;
     
    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;  
    private Doc selectedDoc;    
    private String accordDocsTab = null;
    private final DefaultMenuModel runMenuModel = new DefaultMenuModel();
    private List<RunOptions> runOptions = new ArrayList<>();
    
    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }

    @Override
    protected void doPrepareOpen(Process process) {
        workflow.unpackScheme(process.getScheme());
        initRunOptions();
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
        if (getEditedItem().getDocument() == null) return;
        Doc doc = docBean.getLazyFacade().find(getEditedItem().getDocument().getId());
        getEditedItem().setDocument(doc);
        checkDocument(doc, new HashSet<>());
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
        setSourceItem(process);
        docBean.doViewMainAttache(doc, getParamsMap());
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
        if (processBean.getDiagramBean() != null){
            MsgUtils.errorMsg("DiagramAlreadyOpenInAnotherWindow");
            return;
        }
        processBean.setDiagramBean(diagramBean);
        processBean.getDiagramBeanMap().put(this.toString(), diagramBean);
        diagramBean.setReadOnly(isReadOnly());
        diagramBean.prepareModel(getEditedItem());        
        sessionBean.openDialogFrm(DictFrmName.FRM_DIAGRAMMA, getParamsMap());
    }
    
    public void onSchemeClose(SelectEvent event){
        processBean.setDiagramBean(null);
        if (event.getObject() == null) return;
        String result = (String) event.getObject();
        if (result.equals(SysParams.EXIT_NOTHING_TODO)) return;                
        Process process = getEditedItem();
        Scheme scheme = process.getScheme();        
        List<Task> liveTasks = getTasksFromModel();
        
        List<Task> forRemoveTasks = new ArrayList<>(scheme.getTasks()); //старые задачи?
        forRemoveTasks.removeAll(liveTasks); //в списке остались только задачи, которые нужно удалить
        scheme.getTasks().removeAll(forRemoveTasks);     
        
        //создаём записи в "листе согласования" для участников согласования из модели процесса        
        Set<ProcReport> newReports = liveTasks.stream()
                .filter(task->task.getOwner() != null && task.getConsidInProcReport())
                .map(task -> new ProcReport(getCurrentUser(), task.getOwner(), process))
                .collect(Collectors.toSet()); 

        Set<ProcReport> procReports = process.getReports();        
        List<ProcReport> removeRepors = procReports.stream()
                .filter(report-> report.getDateCreate() == null)   //кроме тех кто уже согласовал/отклонил             
                .collect(Collectors.toList());
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
        PrimeFaces.current().ajax().update("mainFRM:explToolBar");
        onItemChange();
    }
    
    /* *** КУРАТОР *** */
    
    public void onChangeCurator(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()){return;}
        Staff staff = items.get(0);
        getEditedItem().setCurator(staff);
        //doCangeCurator(item);
    }
    public void onChangeCurator(ValueChangeEvent event){
        Staff staff = (Staff) event.getNewValue();
        getEditedItem().setCurator(staff);
        //doCangeCurator(user);
    }    
    
    /* *** МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ *** */
    
    /**
     * Определает отображение кнопки пуска процесса на форме карточки
     * @return 
     */
    public boolean isDisableRunBtn(){        
        if (getEditedItem().getScheme() == null || isReadOnly()) return true;
        Scheme scheme = getEditedItem().getScheme();
        return scheme.getElements().getStartElem() == null;
    }
    
    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        onRun(runOptions.get(0).getName());
    }
    public void onRun(String option){
        Set<String> errors = new HashSet<>();
        Process process = getEditedItem();
        validatePlanDate(process.getPlanExecDate(), errors);
        validateRemarks(process.getDocument(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showErrorsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){
            Map<String, Object> params = new HashMap<>();
            params.put(option, true);
            process = processFacade.find(getEditedItem().getId());
            workflow.start(process, getCurrentUser(), params, errors);
            if (!errors.isEmpty()){
                MsgUtils.showErrorsMsg(errors);
            } else {
                getEditedItem().getState().setCurrentState(process.getState().getCurrentState());
                getEditedItem().setScheme(process.getScheme());
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
    
    private void initRunOptions(){
        String runOpt = processTypesFacade.getProcTypeForOpt(getEditedItem().getOwner()).getRunOptionsJSON();
        Gson gson = new Gson(); 
        runOptions = runOptionsFacade.findByIds(gson.fromJson(runOpt, List.class), getCurrentUser()); 
        
        runOptions.forEach(opt-> {                    
            DefaultMenuItem menuItem = new DefaultMenuItem(getLabelFromBundle(opt.getBundleName()));      
            menuItem.setIcon(opt.getIconName());
            menuItem.setCommand("#{processCardBean.onRun('" + opt.getName() + "')}");
            menuItem.setUpdate("mainFRM");
            menuItem.setOnstart("PF('statusDialog').show()");
            menuItem.setOncomplete("PF('statusDialog').hide(); return itemChange = 0;");
            menuItem.setParam("isRequired", "true");
            runMenuModel.addElement(menuItem); 
        });        
    }
    
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
    private void validateRemarks(Doc doc,  Set<String> errors){
        if (doc == null) return;
        doc = docBean.getLazyFacade().find(doc.getId());        
        Remark remark = doc.getDetailItems().stream()
                .filter(r->Objects.equals(DictStates.STATE_ISSUED, r.getState().getCurrentState().getId()) && !r.isChecked())
                .findFirst()
                .orElse(null);
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
   
    /* *** СООБЩЕНИЯ *** */
    
    /**
     * Создание сообщения с сылкой на процесс
     */
    public void onCreateMessage(){
        Map<String, List<String>> params = getParamsMap();        
        sessionBean.openDialogFrm(DictFrmName.FRM_NOTIFY, params);
    }
    
    /**
     * Открытие формы просмотра сообщений, связанных с процессом
     */
    public void onShowMessages(){
        Map<String, List<String>> paramMap = getParamsMap();
        paramMap.put("typeMsg", Collections.singletonList("allMsg"));
        sessionBean.openDialogFrm(DictFrmName.FRM_USER_MESSAGES, paramMap); 
    }
    
    /* *** РАБОТА С ДОКУМЕНТАМИ *** */
    
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
    
    /**
     * Распечатка списка замечаний
     */
    public void onPreViewRemarks(){                
        Process process = getEditedItem();
        remarkBean.onPreViewRemarks(process.getDocument());        
    }
    
    /* GETS & SETS */

    public DefaultMenuModel getRunMenuModel() {
        return runMenuModel;
    }
    
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

    public List<RunOptions> getRunOptions() {
        return runOptions;
    }
    public void setRunOptions(List<RunOptions> runOptions) {
        this.runOptions = runOptions;
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

    public DiagramBean getDiagramBean() {
        return diagramBean;
    }
        
}