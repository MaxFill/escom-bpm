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
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.process.options.RunOptions;
import com.maxfill.model.basedict.process.options.RunOptionsFacade;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.reports.ProcReportFacade;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.process.timers.ProcTimer;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.services.workflow.Workflow;
import com.maxfill.services.worktime.WorkTimeService;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
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
    @EJB
    private ProcReportFacade procReportFacade;
    @EJB
    private WorkTimeService workTimeService;
    
    private String exitParam = SysParams.EXIT_NOTHING_TODO;
    private ProcReport currentReport;  
    private Doc selectedDoc;    
    private String accordDocsTab = "0";
    private final DefaultMenuModel runMenuModel = new DefaultMenuModel();
    private List<RunOptions> runOptions = new ArrayList<>();    
    private List<BaseDict> forShow;
    
    private List<Staff> inspectors;
    private List<Staff> curators;
    
    private int deadLineDeltaDay = 0;
    private int deadLineDeltaHour = 0;
    
    private final String uid = EscomUtils.generateGUID();
    
    @Override
    protected void doPrepareOpen(Process process) {
        workflow.unpackScheme(process.getScheme(), getCurrentUser());
        initRunOptions();
        long hoursInMilli = 3600;
        long daysInMilli = hoursInMilli * 24;
        if (process.getDeltaDeadLine() > 0){
            long deltaSec = process.getDeltaDeadLine();

            Long elapsedDays = deltaSec / daysInMilli;
            deadLineDeltaDay = elapsedDays.intValue();
            deltaSec = deltaSec % daysInMilli;

            Long elapsedHours = deltaSec / hoursInMilli;
            deadLineDeltaHour = elapsedHours.intValue();        
        }
    }

    /**
     * Перед сохранением процесса
     * @param process
     */
    @Override
    protected void onBeforeSaveItem(Process process){
        int seconds = deadLineDeltaDay * 86400;
        seconds = seconds + deadLineDeltaHour * 3600;
        process.setDeltaDeadLine(seconds);
        processFacade.actualizeRoles(process);
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
        return closeItemForm(exitParam);  
    }
    
    @Override
    public String makeCardHeader() {
        StringBuilder sb = new StringBuilder();
        if (getEditedItem().getParent() == null){
            sb.append(MsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName()));
        } else {
            sb.append(MsgUtils.getBandleLabel("SubProcess"));
        }
        return makeHeader(sb);
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
        sessionBean.openDialogFrm(DictFrmName.FRM_DIAGRAMMA, getParamsMap());
    }
    
    public void onSchemeClose(SelectEvent event){
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
        
        PrimeFaces.current().ajax().update("mainFRM:explToolBar");
        onItemChange();
    }
    
    /* *** РОЛИ *** */
    
    public boolean isShowInspector(){
        return processFacade.isHaveRole(getEditedItem(), DictRoles.ROLE_INSPECTOR);
    }
    public boolean isShowCurator(){
        return processFacade.isHaveRole(getEditedItem(), DictRoles.ROLE_CURATOR);
    }
    
    public void onChangeInspector(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()){return;}
        Staff staff = items.get(0);
        getEditedItem().setInspector(staff);
    }
    public void onChangeInspector(ValueChangeEvent event){
        Staff staff = (Staff) event.getNewValue();
        getEditedItem().setInspector(staff);
    }    
    
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
    }    
    
    /* *** МЕТОДЫ РАБОТЫ С ПРОЦЕССОМ *** */

    /**
     * Определает отображение кнопки пуска процесса на форме карточки
     * @return 
     */
    public boolean isDisableRunBtn(){        
        return isReadOnly();
    }

    /**
     * Обработка события запуска процесса на исполнение
     */
    public void onRun(){
        onRun(runOptions.get(0).getName());
    }
    public void onRun(String option){
        Set<Tuple> errors = new HashSet<>();
        Process process = getEditedItem();
        calculateDeadline(errors);
        validatePlanDate(process, errors);
        //validateRemarks(process.getDocument(), errors);
        processFacade.validateCanRun(process, getCurrentUser(), errors);
        workflow.initScheme(process, null, getCurrentUser(), errors);
        if (!errors.isEmpty()){
            MsgUtils.showTupleErrsMsg(errors);
            return;
        }
        onItemChange();
        if (doSaveItem()){
            Map<String, Object> params = new HashMap<>();
            params.put(option, true);
            process = processFacade.find(getEditedItem().getId());
            forShow = new ArrayList<>(workflow.start(process, getCurrentUser(), params, errors));
            if (!errors.isEmpty()){
                MsgUtils.showTupleErrsMsg(errors);
                return;
            } else {
                getEditedItem().getState().setCurrentState(process.getState().getCurrentState());
                getEditedItem().setScheme(process.getScheme());
                setItemCurrentState(getEditedItem().getState().getCurrentState());
                MsgUtils.succesMsg("ProcessSuccessfullyLaunched");
            }
            exitParam = SysParams.EXIT_EXECUTE;            
            PrimeFaces.current().ajax().update("mainFRM");
            if (!forShow.isEmpty()){                
                PrimeFaces.current().ajax().update("initObjFRM");
                PrimeFaces.current().executeScript("PF('InitObjectsWV').show();");                
            }
        }
    }
    
    /**
     * Обработка события прерывания процесса
     */
    public void onStop(){
        workflow.stop(getEditedItem(), getCurrentUser());        
        exitParam = SysParams.EXIT_EXECUTE;
        MsgUtils.warnMsg("ProcessExecutionInterrupted");        
    }      
    
    /* *** ВАЛИДАЦИЯ ПРОЦЕССА *** */
    
    /**
     * Проверка срока исполнения
     * @param process 
     * @param errors 
     */
    public void validatePlanDate(Process process, Set<Tuple> errors){                
        Date planDate = process.getPlanExecDate();
        Date today = new Date();
        if (today.after(planDate)){
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:planEndDate");
            input.setValid(false);
            errors.add(new Tuple("DeadlineProcessInPastTime", new Object[]{}));            
            context.validationFailed();
        }
    }    

    /**
     * Проверка снятых замечаний
     */
    private void validateRemarks(Doc doc,  Set<Tuple> errors){
        if (doc == null) return;
        doc = docBean.getLazyFacade().find(doc.getId());        
        Remark remark = doc.getDetailItems().stream()
                .filter(r->Objects.equals(DictStates.STATE_ISSUED, r.getState().getCurrentState().getId()) && !r.isChecked())
                .findFirst()
                .orElse(null);
        if (remark != null){
            errors.add(new Tuple("CannotStartProcessUnprocessedRemarks", new Object[]{}));
        }
    }
    
    /* *** ПРОЧИЕ МЕТОДЫ *** */
    
    public void onChangeCurator(){        
        checkAvailableStaff(getEditedItem().getCurator(), "mainFRM:mainTabView:smCurator");
    }
    
    public void onChangeInspector(){                
        checkAvailableStaff(getEditedItem().getInspector(), "mainFRM:mainTabView:smInspector");
    }
    
    private void checkAvailableStaff(Staff staff, String component){  
        if (staff == null) return;
        Set<Tuple> errors = new HashSet<>();        
        Date endDate = calculateDeadline(errors);
         if (!errors.isEmpty()){
            errors.add(new Tuple("AvailabilyEmployeeWorkCalendarNotCheck", new Object[]{}));
            MsgUtils.showTupleErrsMsg(errors);
            return;
        }
        Date beginDate = DateUtils.clearDate(new Date());
        if (workTimeService.checkStaffAvailable(staff, getEditedItem().getCompany(), beginDate, endDate)){
            MsgUtils.succesFormatMsg("EmployeAvailableOnSpecifiedDates", new Object[]{staff.getEmployeeFIO()});
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(component);
            input.setValid(false);
            MsgUtils.warnFormatMsg("SpecifiedPeriodIsNonWorking", 
                    new Object[]{
                        staff.getEmployeeFIO(), 
                        DateUtils.dateToString(beginDate, DateFormat.SHORT, null, sessionBean.getLocale()), 
                        DateUtils.dateToString(endDate, DateFormat.SHORT, null, sessionBean.getLocale())
                    }); 
            context.validationFailed();
            PrimeFaces.current().ajax().update(component);
        }
    }
    
    private void checkAvailableRoles(){
        FacesContext context = FacesContext.getCurrentInstance();
        UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:smCurator");
        if (input != null){
            checkAvailableStaff(getEditedItem().getCurator(), "mainFRM:mainTabView:smCurator");
        }
        input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:smInspector");
        if (input != null){
            checkAvailableStaff(getEditedItem().getInspector(), "mainFRM:mainTabView:smInspector");
        }
    }
    
    /**
     * Обновление списка подпроцессов на форме предварительного запуска после открытия карточки процесса
     */
    public void onUpdateProcesses(){
        forShow = forShow.stream()
                .map(proc->processFacade.find(proc.getId()))
                .filter(proc->Objects.equals(proc.getState().getCurrentState().getId(), DictStates.STATE_DRAFT))
                .collect(Collectors.toList());
        if(forShow.isEmpty()){
            PrimeFaces.current().executeScript("PF('InitObjectsWV').hide();");
        } else {
            PrimeFaces.current().ajax().update("initObjFRM");
        }
    }
    
    /**
     * Формирование заголовка для страницы с листом исполнения/согласования
     * @return 
     */
    public String getHeaderTabReports(){
        ProcessType processType = processTypesFacade.getProcTypeForOpt(getEditedItem().getOwner());
        if (StringUtils.isNotBlank(processType.getNameReports())){
            return getLabelFromBundle(processType.getNameReports());
        }
        return "";
    }
    
    public boolean isShowReports(){
        ProcessType processType = processTypesFacade.getProcTypeForOpt(getEditedItem().getOwner());
        return processType.isShowReports();
    }
    
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
     * Признак доступности кнопки прерывания процесса
     * @return 
     */
    public boolean isDisableBtnStop(){        
        if (!getEditedItem().isRunning()) return true;
        if (userFacade.isAdmin(getCurrentUser())) return false;
        
        User curator = null;
        if (getEditedItem().getCurator() != null){
            curator = getEditedItem().getCurator().getEmployee();
        }
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
   
    /**
     * Вычисление планового срока исполнения
     */
    public void calculateDeadline(){        
        Set<Tuple> errors = new HashSet<>();
        Date planDate = calculateDeadline(errors);
        
        if (!errors.isEmpty()){
            MsgUtils.showTupleErrsMsg(errors);
            return;
        }
                
        getEditedItem().setPlanExecDate(planDate); 
        
        String strDate = DateUtils.dateToString(planDate,  DateFormat.SHORT, DateFormat.MEDIUM, getLocale());
        MsgUtils.succesFormatMsg("DeadlineCalcWorkingCalendar", new Object[]{strDate});
        checkAvailableRoles();
    }    
    
    public Date calculateDeadline(Set<Tuple> errors ){
        Process process = getEditedItem();
        if (process.getDeadLineType().equals("data")) return process.getPlanExecDate();
        
        if (deadLineDeltaDay == 0 && deadLineDeltaHour == 0){
            errors.add(new Tuple("DeadlineIncorrect", new Object[]{})); 
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput deltaDay = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:deltaDay");
            deltaDay.setValid(false);
            UIInput deltaHour = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:deltaHour");
            deltaHour.setValid(false);
            context.validationFailed();
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:dtPlansGrid");
        }
        if (process.getCompany() == null){
            errors.add(new Tuple("CompanyNotSet", new Object[]{}));
            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:smCompany");
            input.setValid(false);
            context.validationFailed();
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:smCompany");
        }        
        int deltasec = deadLineDeltaDay * 86400;
        deltasec = deltasec + deadLineDeltaHour * 3600;         
        return workTimeService.calcWorkDayByCompany(new Date(), deltasec, process.getCompany());
    }
    
    /* *** СООБЩЕНИЯ *** */
    
    /**
     * Создание сообщения с cсылкой на процесс
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
        List<Doc> selectedDocs = (List<Doc>) event.getObject();
        if (selectedDocs.isEmpty()) return;
        List<Doc> procDocs = new ArrayList<>(getEditedItem().getDocs());
        procDocs.addAll(selectedDocs);
        getEditedItem().setDocs(procDocs);
        if (getEditedItem().getDocument() == null){
            Doc doc = selectedDocs.get(0);
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
        List<Object> dataReport = procReportFacade.findReportByDoc(doc, DictRoles.ROLE_CONCORDER).stream()
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
    
    public int getDeadLineDeltaDay() {
        return deadLineDeltaDay;
    }
    public void setDeadLineDeltaDay(int deadLineDeltaDay) {
        this.deadLineDeltaDay = deadLineDeltaDay;
    }

    public int getDeadLineDeltaHour() {
        return deadLineDeltaHour;
    }
    public void setDeadLineDeltaHour(int deadLineDeltaHour) {
        this.deadLineDeltaHour = deadLineDeltaHour;
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

    public List<Staff> getInspectors() {
        if (inspectors == null){
            UserGroups userGroup = processFacade.getRoleDataSource(getEditedItem(), DictRoles.ROLE_INSPECTOR);
            if (userGroup != null){
                inspectors = userFacade.findUserByGroupID(userGroup.getId(), getCurrentUser()) 
                    .stream()                    
                    .filter(user->user.getStaff() != null)
                    .map(user->user.getStaff())
                    .collect(Collectors.toList());
            } else {
                inspectors = new ArrayList<>();
            }
        }
        return inspectors;
    }
    
    public List<Staff> getCurators() {
        if (curators == null){
            UserGroups userGroup = processFacade.getRoleDataSource(getEditedItem(), DictRoles.ROLE_CURATOR);
            if (userGroup != null){
                curators = userFacade.findUserByGroupID(userGroup.getId(), getCurrentUser()) 
                    .stream()                    
                    .filter(user->user.getStaff() != null)
                    .map(user->user.getStaff())
                    .collect(Collectors.toList());
            } else {
                curators = new ArrayList<>();
            }
        }
        return curators;
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

    public String getUid() {
        return uid.replaceAll("-", "").substring(0, 9);        
    }
    
    @Override
    public BaseDict getSourceItem() {
        return getEditedItem(); 
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

    @Override
    protected BaseDictFacade getFacade() {
        return processFacade;
    }
    
    public List<BaseDict> getForShow() {
        return forShow;
    }
}
