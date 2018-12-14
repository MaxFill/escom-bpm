package com.maxfill.escom.beans.docs;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.docStatuses.DocStatuses;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.statusesDoc.StatusesDoc;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.interfaces.WithDetails;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.remarks.RemarkBean;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.basedict.docStatuses.DocStatusFacade;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.utils.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import com.maxfill.model.basedict.doc.numerator.DocNumerator;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.reports.ProcReportFacade;
import com.maxfill.services.numerators.NumeratorService;
import org.primefaces.PrimeFaces;

/**
 * Контролер формы "Карточка документа"
 * @author maksim
 */
@Named
@ViewScoped
public class DocCardBean extends BaseCardBean<Doc> implements WithDetails<Remark>{
    private static final long serialVersionUID = -8151932533993171177L;
        
    @Inject
    private DocBean docBean;
    @Inject
    private RemarkBean remarkBean;
    @Inject
    private ProcessBean processBean;
    
    @EJB
    private DocNumerator docNumeratorService;
    @EJB
    private DocFacade itemFacade;
    @EJB
    private DocStatusFacade docStatusFacade;
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private RemarkFacade remarkFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private ProcReportFacade procReportFacade;
    
    private final List<DocStatuses> forDelDocStatus = new ArrayList<>();
    private final List<Attaches> forDelAttaches = new ArrayList<>();
    private List<ProcReport> concorders = new ArrayList<>();
    
    private String docURL;  
    private Attaches selectedAttache;
    private Doc linkedDoc;
    private Process selectedProcess;
    private List<Remark> checkedDetails;
    private Remark selectedDetail;
    private ProcTempl selectedProcTempl;
    
    @Override
    public String doFinalCancelSave() {      
        List<Attaches> notSaveAttaches = getEditedItem().getAttachesList().stream()
                .filter(attache -> attache.getId() == null).collect(Collectors.toList());
        attacheService.deleteAttaches(notSaveAttaches); //удаление файлов вложений не сохранённых версий
        return super.doFinalCancelSave();
    }    
    
    @Override
    protected void doPrepareOpen(Doc doc) {
        if (getTypeEdit().equals(DictEditMode.INSERT_MODE)){
            if (getEditedItem().getAttachesList().size() >0){                
                onItemChange(); //установка признака что документ изменён
            }
            if (getEditedItem().getRegNumber() != null){
                onItemChange(); //установка признака что документ изменён, потому что зарегистрирован автоматически при создании
                isItemRegisted = true;
            }
        } 
        
        if (StringUtils.isNoneBlank(getEditedItem().getRegNumber())){        
            isItemRegisted = true;
        }
        initConcorders();
    }
         
    @Override
    protected void checkItemBeforeSave(Doc doc, FacesContext context, Set<String> errors){        
        checkRegNumber(doc, errors);
        super.checkItemBeforeSave(doc, context, errors);
    }
        
    @Override
    public DocFacade getFacade() {
        return itemFacade;
    }         
        
    @Override
    public void onAfterSaveItem(Doc doc){        
        onSaveChangeDocStatus();
        onSaveChangeAttaches();
    }

    public void onRowDblClckOpen(SelectEvent event){
        linkedDoc = (Doc) event.getObject();
    }
        
    public String getNumberMask() {
        DocType docType = getEditedItem().getDocType();
        if (docType == null) return null;
        
        NumeratorPattern numerator = docType.getNumerator();
        if (numerator == null) return null;
        
        String pattern = numerator.getPattern();
        if (StringUtils.isBlank(pattern)) {
            MsgUtils.errorMsg("PatternIsEmpty");
            return null;
        }
        
        Integer lenNumber = numerator.getLeadingZeros();
        StringBuilder mask = new StringBuilder();
        int index = 0;
        int l = pattern.length();
        while (index < l){
            String part = "";            
            switch (pattern.charAt(index)){
                case 'T':{              
                    part = docType.getCode();                    
                    break;
                }
                case 'N':{                    
                    part = StringUtils.repeat("9", lenNumber);                    
                    break;
                }
                case 'O':{
                    Company company = getEditedItem().getCompany();
                    if (company != null){
                        part = company.getCode();
                    }
                    break;
                }
                case 'Y':{
                    part = StringUtils.repeat("9", 2);
                    break;
                }
                case 'y':{
                    part = StringUtils.repeat("9", 4);
                    break;
                }
                default:{
                    part = StringUtils.repeat(pattern.charAt(index), 1);
                }
            }
            if (part != null){
                mask.append(part);
            }
            index++;
        }
        return mask.toString();
    }
     
    private void onSaveChangeAttaches(){
        if (!forDelAttaches.isEmpty()){
            forDelAttaches.stream()
                    .forEach(attache -> {
                        attacheService.deleteAttache(attache);
                        if (attache.getId() != null){
                            attacheFacade.remove(attache);
                        }
                    });
        }
    }
    
    private void onSaveChangeDocStatus() {
        if (!forDelDocStatus.isEmpty()){
            forDelDocStatus.stream()
                    .filter(docsStatus -> docsStatus.getId() != null)
                    .forEach(docsStatus -> {
                        docStatusFacade.remove(docsStatus);
                        getFacade().addLogEvent(getEditedItem(), "DeletedDocStatus", new String[]{docsStatus.toString()}, getCurrentUser());
                    });
        }        
    }     
        
    /* Запрос на формирование ссылки URL для просмотра документа  */
    public void onGetDocViewURL(Doc doc){
        docURL = sessionBean.doGetItemURL(doc, "/docs/document.xhtml");
    }
    
    /* Запрос на формирование ссылки URL на открытие карточки документа */
    public void onGetDocOpenURL(Doc doc){
        docURL = sessionBean.doGetItemURL(doc, "/docs/doc-card.xhtml");
    }   
    
    /* *** СООБЩЕНИЯ *** */
    
    /**
     * Открытие формы просмотра сообщений
     */
    public void onShowMessages(){
        Map<String, List<String>> paramMap = getParamsMap();
        paramMap.put("typeMsg", Collections.singletonList("allMsg"));
        sessionBean.openDialogFrm(DictFrmName.FRM_USER_MESSAGES, paramMap); 
    }
    
    /**
     * Создание сообщения
     */
    public void onCreateMessage(){
        Map<String, List<String>> params = getParamsMap();       
        if (getEditedItem().getId() != null){
            sessionBean.openDialogFrm(DictFrmName.FRM_NOTIFY, params);
            return;
        } 
        onItemChange();
        if (doSaveItem()){
            sessionBean.openDialogFrm(DictFrmName.FRM_NOTIFY, params);
        }        
    }
    
    /* *** ПРОЦЕССЫ *** */
    
    public List<Process> getProcesses(){
        return processFacade.findProcessesByDoc(getEditedItem(), getCurrentUser());
    }
    
    public void onAfterSelectProcTempl(SelectEvent event){
        if (event.getObject() == null) return;
        List<ProcTempl> procTempls = (List<ProcTempl>) event.getObject();
        if (!procTempls.isEmpty()){
            selectedProcTempl = procTempls.get(0);
        }
    }
    
    public void onCreateProc(){
        if (selectedProcTempl == null) return;
        onItemChange();
        if (doSaveItem()){
            Doc doc = getEditedItem();
            Map<String, Object> createParams = new HashMap<>();
            createParams.put("documents", Collections.singletonList(doc));
            createParams.put("template", selectedProcTempl);
            createParams.put("company", doc.getCompany());        
            processBean.createItemAndOpenCard(null, selectedProcTempl.getOwner(), createParams, getParamsMap());
        }
    }
    
    public void onAfterCloseProcess(){
        
    }
    
    public void onUpdateProcesses(){
        
    }
    
    /* *** РЕГИСТРАЦИЯ *** */
    
    /* Определяет доступность кнопки регистрации документа */
    public boolean isCanRegistred(){
        if (isReadOnly()) return false;
        Doc doc = getEditedItem();      
        return StringUtils.isBlank(doc.getRegNumber()) && doc.getCompany() != null && doc.getDocType() != null && doc.getDocType().getNumerator() != null;
    }  
    
    /* Определяет доступность кнопки отмены регистрации документа */
    public boolean isCanUnregistred(){
        if (isReadOnly()) return false;
        Doc doc = getEditedItem();      
        return StringUtils.isNotBlank(doc.getRegNumber()) && doc.getCompany() != null && doc.getDocType() != null && doc.getDocType().getNumerator() != null;
    }
    
    /* Сброс регистрационного номера */
    public void onClearRegNumber(){
        Doc doc = getEditedItem();
        if (doc.getDocType() != null){
            NumeratorPattern numerator = doc.getDocType().getNumerator();
            if (numerator != null && DictNumerator.TYPE_AUTO.equals(numerator.getTypeCode())){
                docNumeratorService.doRollBackRegistred(doc);
            }
        }
        doc.setRegNumber(null);
        onItemChange();
        isItemRegisted = false;
        MsgUtils.warnMsg("DocRegCanceled");
    }

    /* Формирование регистрационного номера документа. Вызов с экранной формы */
    public void onGenerateRegNumber(Doc doc){
        Set<Tuple> errors = new HashSet<>();
        docNumeratorService.registratedDoc(doc, errors);
        if (errors.isEmpty()){
            onItemChange();
            isItemRegisted = true;
            MsgUtils.succesMsg("DocIsRegistred");
        } else {
            MsgUtils.showTupleErrsMsg(errors);
        }
    }
    
    /* Проверка регистрационного номера   */
    public void checkRegNumber(Doc doc, Set<String> errors) {
        String regNumber = doc.getRegNumber();
        if (StringUtils.isNotBlank(regNumber)){            
            if (!getFacade().checkRegNumber(regNumber, doc)) {                
                errors.add(MessageFormat.format(MsgUtils.getMessageLabel("REGNUMBER_IS_DUBLICATE"), new Object[]{doc.getTypeName(), regNumber, doc.getCompanyName()}));                
            }
        }
    }
    
    @Override
    public NumeratorService getNumerator(){
        return docNumeratorService;
    }
        
    /* *** ВЛОЖЕНИЯ *** */
    
    /* Подготовка к отправке текущего документа на e-mail  */
    public void prepareSendMailDoc(String mode){
        List<BaseDict> docs = new ArrayList<>();
        docs.add(getEditedItem());
        sessionBean.openMailMsgForm(mode, docs);
    }    
    
    public void onSetSelectedAttache(Attaches attache){
       selectedAttache = attache;
    }
    
    /* Добавление версии к документу   */
    public void addAttacheFromFile(FileUploadEvent event) throws IOException{
        onItemChange();        
        docBean.addAttacheFromFile(getEditedItem(), event);        
    }
    
    public void addAttacheFromScan(SelectEvent event){
        if (event.getObject() instanceof String) return;
        onItemChange();
        docBean.addAttacheFromScan(getEditedItem(), event);
    }
    
    /* Удаление текущей версии документа  */
    public void removeCurrentVersion(){
        Attaches version =  null;
        Doc doc = getEditedItem();
  
        for (Integer number = doc.getMainAttache().getNumber(); number >= 1; --number){
            for(Attaches attache : doc.getAttachesList()){
                if (Objects.equals(number, attache.getNumber())){
                    version = attache;
                    break;
                }
            }
        }
        attacheFacade.setMainAttache(doc, version);                
        onItemChange(); 
    }
    
    /* Установка текущей версии в редактируемом документе  */
    public void makeCurrentVersion(Attaches attache){
        attacheFacade.setMainAttache(getEditedItem(), attache);        
        onItemChange();
    }    
    
    /* Удаление версии документа и её файла */
    public void deleteAttache(Attaches attache) {   
        if (procReportFacade.findReportByAttache(attache).isEmpty()){
            MsgUtils.errorMsg("CannotDelVersionBecauseConcorder");
            return;
        }
        Doc doc = getEditedItem();
        forDelAttaches.add(attache);
        doc.getAttachesList().remove(attache);
        if (attache.equals(doc.getMainAttache())){
            removeCurrentVersion();
        }
        onItemChange();        
    }
    
    /* Скачивание текущей версии документа */
    public void downloadCurrentVersion(){
        if (getEditedItem() == null) return;        
        Doc doc = getEditedItem();        
        Attaches attache = attacheService.findAttacheByDoc(doc);                               
        if (attache != null){
            sessionBean.attacheDownLoad(attache);
        } else {
            MsgUtils.warnMsg("FileNotFound");
        }
    }        
    
    public void onLockSelectedAttache(){
        if (selectedAttache == null) return;
        docBean.onOpenFormLockAttache(selectedAttache);
    }
    
    public void onOpenFormLockAttache(Attaches attache){
        docBean.onOpenFormLockAttache(attache);
    }
    
    /* Событие изменения контрагента на карточке */
    public void onPartnerSelected(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Partner> items = (List<Partner>) event.getObject();
        if (items.isEmpty()) return;
        Partner item = items.get(0);
        onItemChange();
        getEditedItem().setPartner(item);
    } 
    public void onPartnerSelected(ValueChangeEvent event){
        Partner partner = (Partner) event.getNewValue();
        getEditedItem().setPartner(partner);
    }     
    
    /* Событие изменения типа документа на карточке  */
    public void onDocTypeSelected(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<DocType> items = (List<DocType>) event.getObject();
        if (items.isEmpty()){return;}
        DocType item = items.get(0);
        getEditedItem().setDocType(item);
        onItemChange();
        docBean.addDocStatusFromDocType(getEditedItem(),item);
    }
    public void onDocTypeSelected(ValueChangeEvent event){        
        DocType docType = (DocType) event.getNewValue();
        getEditedItem().setDocType(docType);
        docBean.addDocStatusFromDocType(getEditedItem(), docType);
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:regNumberPanel");
    }      
  
    /* Событие обработки выбора главного документа на карточке */
    public void onMainDocSelected(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<Doc> items = (List<Doc>) event.getObject();
        if (items.isEmpty()) return;
        Doc item = items.get(0);
        onItemChange();
        getEditedItem().setMainDoc(item);
    }
    
    /* Возвращает true если у документа есть заблокированные вложения */
    public boolean getDocIsLock(){
        return docBean.docIsLock(getEditedItem());
    }          
    
    /* СТАТУСЫ ДОКУМЕНТА */
              
    /* Изменение значения статуса документа в таблице статусов на карточке документа */   
    public void onChangeDocStatus(DocStatuses docsStatus){
        if (docsStatus.getDateStatus() == null){
            docsStatus.setDateStatus(new Date());
        }
        docsStatus.setAuthor(getCurrentUser());
        //String statusName = docsStatus.getStatus().getName() + " = " + EscomBeanUtils.getBandleLabel(docsStatus.getValueBundleKey());
        //getFacade().addLogEvent(getEditedItem(), "ChangeDocStatus", statusName, getCurrentUser());
        onItemChange();
    }
    
    /* Удаление статуса документа из редактируемого документа  */
    public void onDeleteStatus(DocStatuses docsStatus){
        List<DocStatuses> docsStatuses = getEditedItem().getDocsStatusList();
        for (DocStatuses ds : docsStatuses){
            if (Objects.equals(ds, docsStatus)){
                docsStatuses.remove(ds);
                forDelDocStatus.add(docsStatus);
                onItemChange();
                break;
            }
        }
    }
    
    /* Удаление всех статусов документа из редактируемого документа */
    public void onDeleteAllStatus(){
        forDelDocStatus.addAll(getEditedItem().getDocsStatusList());
        getEditedItem().getDocsStatusList().clear();
        onItemChange();
    }
    
    /* Загрузка статусов в редактируемый документ из шаблона документа - вызов с формы  */
    public void onLoadDocStatusFromDocType(){
        int loadCounter = docBean.addDocStatusFromDocType(getEditedItem(), getEditedItem().getDocType());
        if (loadCounter > 0){
            onItemChange();
            Object[] params = new Object[]{loadCounter};
            MsgUtils.succesFormatMsg("StatusesLoadComplete", params);
        } else {
            MsgUtils.warnMsg("StatusesNotFound");
        }    
    }                 
    
    /* Добавление в документ статусов из селектора */
    public void onAddStatusesFromSelector(SelectEvent event){
        if (event.getObject() instanceof String) return;
        if (event.getObject() != null){
            List<StatusesDoc> statuses = (List<StatusesDoc>) event.getObject();
            int loadCounter = docBean.addStatusesInDoc(getEditedItem(), statuses);
            Object[] params = new Object[]{loadCounter};
            MsgUtils.succesFormatMsg("StatusesLoadComplete", params);
        }    
    }    

    /* Обновляет текущую запись в таблице версий на карточке документа */
    public void onUpdateSelectedAttache(){
        List<Attaches> attaches = getEditedItem().getAttachesList();
        attaches.remove(selectedAttache);
        selectedAttache = attacheFacade.find(selectedAttache.getId());
        attaches.add(selectedAttache);
    }
    
    public void onUpdateLinkedDocs(){
        List<Doc> linkedDocs = getFacade().findLinkedDocs(getEditedItem());
        getEditedItem().setDocsLinks(linkedDocs);
    }
    
    /* ПРИЗНАКИ */      
 
    public boolean isCanCreateProcess(){
        return isHaveRightExec();
    }  
    
    /* ЗАМЕЧАНИЯ */
    
    @Override
    public List<Remark> getDetails() {
        return getEditedItem().getDetailItems();
    }

    @Override
    public List<Remark> getCheckedDetails() {
        return checkedDetails;
    }

    @Override
    public void setCheckedDetails(List<Remark> checkedDetails) {
        this.checkedDetails = checkedDetails;
    }

    @Override
    public void onDeleteCheckedDetails() {
        getDetails().removeAll(checkedDetails);
        onItemChange();
    }

    @Override
    public void onCreateDetail() {
        selectedDetail = remarkFacade.createItem(getCurrentUser(), null, getEditedItem(), new HashMap<>());
        onOpenDetail(selectedDetail);
    }

    @Override
    public void afterCloseDetailItem(SelectEvent event) {
        if (event.getObject() == null) return;        
        switch ((String) event.getObject()){
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
            case SysParams.EXIT_NEED_UPDATE:{                
                if (selectedDetail.getId() == null){
                    getDetails().add(selectedDetail);
                }
                onItemChange();
                break;
            }
        }  
    }

    @Override
    public void onDeleteDetail(Remark item) {
        getDetails().remove(item);
        onItemChange();
    }

    @Override
    public void onOpenDetail(Remark item) {
        setSourceItem(item);
        Map<String, List<String>> paramsMap = getParamsMap();
        paramsMap.put("remarkId", Collections.singletonList(item.getId().toString()));
        remarkBean.prepEditChildItem((Remark)item, paramsMap);
    }
    
    @Override
    public Remark getSelectedDetail() {
        return selectedDetail;
    }

    @Override
    public void setSelectedDetail(Remark selectedDetail) {
       this.selectedDetail = selectedDetail;
    }
    
    /* *** ПЕЧАТЬ *** */
    
    /* Печать карточки документа */
    @Override
    protected void doPreViewItemCard(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        super.doPreViewItemCard(dataReport, parameters, DictPrintTempl.REPORT_DOC_CARD);
    }
    
    /**
     * Распечатка списка замечаний
     */
    public void onPreViewRemarks(){        
        remarkBean.onPreViewRemarks(getEditedItem());        
    }    
    
    /* ЛИСТ СОГЛАСОВАНИЯ */
    
    private void initConcorders(){
        concorders = procReportFacade.findReportByDoc(getEditedItem(), DictRoles.ROLE_CONCORDER);    
    }
    
    /* GETS & SETS */

    public List<ProcReport> getConcorders() {
        return concorders;
    }        
    
    @Override
    public Doc getEditedItem() {
        return super.getEditedItem(); 
    }    
    
    public Doc getLinkedDoc() {
        return linkedDoc;
    }
    public void setLinkedDoc(Doc selectedDoc) {
        this.linkedDoc = selectedDoc;
    }
    
    public String getDocURL() {
        return docURL;
    }
    public void setDocURL(String docURL) {
        this.docURL = docURL;
    }

    public Process getSelectedProcess() {
        return selectedProcess;
    }
    public void setSelectedProcess(Process selectedProcess) {
        this.selectedProcess = selectedProcess;
    }
    
}