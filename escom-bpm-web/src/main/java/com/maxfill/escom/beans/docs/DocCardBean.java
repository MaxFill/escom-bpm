package com.maxfill.escom.beans.docs;

import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocNumerator;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.statuses.DocsStatus;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 *  Бизнес-логика для работы с документами
 * @author mfilatov
 */

@Named
@ViewScoped
public class DocCardBean extends BaseCardBean<Doc>{
    private static final long serialVersionUID = -8151932533993171177L;

    private final List<DocsStatus> forDelDocStatus = new ArrayList<>();
    private final List<DocsStatus> forAddDocStatus = new ArrayList<>();
    
    private String docURL;
    private Integer documentId; //используется при открытии файла на просмотр через прямую гиперсылку  
    private List<DocType> docTypes;
    private List<Staff> staffs;
    private List<Partner> partners;

    @EJB
    private DocNumerator docNumeratorService;

    /**
     * Специфические действия при отмене сохранения изменённого документа
     * @return 
     */
    @Override
    public String doFinalCancelSave() {      
        List<Attaches> notSaveAttaches = getEditedItem().getAttachesList().stream()
                .filter(attache -> attache.getId() == null).collect(Collectors.toList());
        attacheService.deleteAttaches(notSaveAttaches); //удаление файлов вложений не сохранённых версий
        return super.doFinalCancelSave();
    }
    
    /**
     * Специфические действия при открытии карточки документа
     * @param doc 
     */
    @Override
    protected void doPrepareOpen(Doc doc) {
        if (getTypeEdit().equals(DictEditMode.INSERT_MODE) && getEditedItem().getAttachesList().size() >0){
            onItemChange(); //установка признака что документ изменён
        }
    }
     
    /**
     * Проверка корректности полей документа
     * @param doc
     */
    @Override
    protected void checkItemBeforeSave(Doc doc, Set<String> errors){        
        checkRegNumber(doc, errors);
        super.checkItemBeforeSave(doc, errors);
    }        
    
    @Override
    public DocFacade getItemFacade() {
        return sessionBean.getDocsFacade();
    }         
    
    @Override
    public void onAfterSaveItem(Doc doc){        
        onSaveChangeDocStatus();
    }
    
    @Override
    protected void onAfterCreateItem(Doc doc) {
        addDocStatusFromType(doc, doc.getDocType());
    }
     
    /**
     * Запрос на формирование ссылки URL для просмотра документа
     * @param doc
     * @return 
     */
    public String onGetDocViewURL(Doc doc){
        return EscomBeanUtils.doGetItemURL(doc, "docs/document", "0");
    }
    
    /**
     * Запрос на формирование ссылки URL на открытие карточки документа 
     * @param doc
     * @return 
     */
    public String onGetDocOpenURL(Doc doc){
        return EscomBeanUtils.doGetItemURL(doc, "folders/folder-explorer", "0");
    }
        
    /**
     * Сброс регистрационного номера
     * @param doc
     */
    public void onClearRegNumber(Doc doc){
        if (doc.getDocType() != null){
            NumeratorPattern numerator = doc.getDocType().getNumerator();
            if (numerator != null && DictNumerator.TYPE_AUTO.equals(numerator.getTypeCode())){
                docNumeratorService.doRollBackRegistred(doc, "");
            }
        }
        doc.setRegNumber(null);
        setIsItemChange(Boolean.TRUE);
        EscomBeanUtils.WarnMsgAdd("DocRegCanceled", "DocIsEnableEditing");
    }

    /**
     * Формирование регистрационного номера документа. Вызов с экранной формы
     * @param doc
     */
    public void onGenerateRegNumber(Doc doc){
        docNumeratorService.doRegistDoc(doc);
        setIsItemChange(Boolean.TRUE);
    }       
    
    /**
     * Подготовка к отправке текущего документа на e-mail
     * @param mode
     */
    public void prepareSendMailDoc(String mode){
        List<BaseDict> docs = new ArrayList<>();
        docs.add(getEditedItem());
        EscomBeanUtils.openMailMsgForm(mode, docs);
    }
    
    /* *** ВЛОЖЕНИЯ *** */
    
    /**
     * Добавление версии к документу
     * @param event
     * @return 
     * @throws IOException 
     */
    public Attaches addAttache(FileUploadEvent event) throws IOException{
        setIsItemChange(Boolean.TRUE);
        Doc doc = getEditedItem();
        UploadedFile file = FileUtils.handleUploadFile(event);
        Attaches attache = uploadAtache(file);
        Short version = doc.getNextVersionNumber();            
        attache.setNumber(version);
        attache.setDoc(doc);
        getEditedItem().getAttachesList().add(attache);
        return attache;
    }
    
    /**
     * Удаление текущей версии документа
     */
    public void removeCurrentVersion(){
        Attaches version =  null;
        Doc doc = getEditedItem();
  
        for (short number = doc.getAttache().getNumber(); number >= 1; --number){
            for(Attaches attache : doc.getAttachesList()){
                if (number == attache.getNumber()){
                    version = attache;
                    break;
                }
            }
        }
        makeCurrentVersion(doc, version);                
        setIsItemChange(Boolean.TRUE); 
    }
    
    /**
     * Установка текущей версии в редактируемом документе
     * @param attache 
     */
    public void makeCurrentVersion(Attaches attache){
        makeCurrentVersion(getEditedItem(), attache);
        setIsItemChange(Boolean.TRUE);
    }
    
    /**
     * Установка текущей версии в документе
     * @param doc
     * @param version
     */
    public void makeCurrentVersion(Doc doc, Attaches version){
        for(Attaches attache : doc.getAttachesList()){
            if (Boolean.TRUE.equals(attache.getCurrent())){
                attache.setCurrent(Boolean.FALSE);
            }
            if(version.equals(attache)){
                attache.setCurrent(Boolean.TRUE);
            }
        }
    }
    
    /**
     * Удаление версии документа и её файла
     *
     * @param attache
     */
    public void deleteAttache(Attaches attache) {
        try {
            attacheService.deleteAttache(attache);            
            Doc doc = getEditedItem();
            doc.getAttachesList().remove(attache);
            if (attache.equals(doc.getAttache())){
                removeCurrentVersion();
            }            
            setIsItemChange(Boolean.TRUE);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            EscomBeanUtils.ErrorMessage(exception.getMessage());
        }
    }            
    
    /**
     * Скачивание текущей версии документа
     */
    public void downloadCurrentVersion(){
        if (getEditedItem() == null && documentId != null){
            setEditedItem(getItemFacade().find(documentId));
        }        
        Doc doc = getEditedItem();        
        if (doc != null){
            actualizeRightItem(doc);
            if (getItemFacade().isHaveRightView(doc)) {
                Attaches attache = attacheService.findAttacheByDoc(doc);                               
                if (attache != null){
                    attacheDownLoad(attache);                    
                } else {
                    EscomBeanUtils.WarnMsgAdd("Error", "FileNotFound");
                }
            } else {                
                EscomBeanUtils.WarnMsgAdd("AccessDenied", "RightViewNo");
            }
        } else {
            EscomBeanUtils.WarnMsgAdd("Error", "DocumentNotFound");
        }
    }
    
    /* *** СОБЫТИЯ ИЗМЕНЕНИЯ ПОЛЕЙ НА ФОРМЕ*** */
    
    /**
     * Событие изменения контрагента на форме документа
     * @param event 
     */
    public void onPartnerSelected(SelectEvent event){
        List<Partner> items = (List<Partner>) event.getObject();
        if (items.isEmpty()){return;}
        Partner item = items.get(0);
        onItemChange();
        getEditedItem().setPartner(item);
        if (!partners.contains(item)){
            partners.add(item);
        }
    } 
    public void onPartnerSelected(ValueChangeEvent event){
        Partner partner = (Partner) event.getNewValue();
        getEditedItem().setPartner(partner);
    } 
    
    /**
     * Выбор штатной единицы     
     * @param event
     */
    public void onManagerSelected(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()){return;}
        Staff item = items.get(0);
        onItemChange();
        getEditedItem().setManager(item);
        if (!staffs.contains(item)){
            staffs.add(item);
        }
    }
    
    /**
     * Событие изменения типа документа на форме документа
     * @param event 
     */
    public void onDocTypeSelected(SelectEvent event){
        List<DocType> items = (List<DocType>) event.getObject();
        if (items.isEmpty()){return;}
        DocType item = items.get(0);
        getEditedItem().setDocType(item);
        onItemChange();
        addDocStatusFromType(getEditedItem(),item);
        if (!docTypes.contains(item)){
            docTypes.add(item);
        }
    }
    public void onDocTypeSelected(ValueChangeEvent event){        
        DocType docType = (DocType) event.getNewValue();
        getEditedItem().setDocType(docType);
        addDocStatusFromType(getEditedItem(), docType);
    }      
  
    /** 
     * СТАТУСЫ ДОКУМЕНТА: Добавление статусов документа из шаблона типа документа
     * @param doc
     * @param docType
     */
    private int addDocStatusFromType(Doc doc, DocType docType){
        int loadCounter = 0;
        if (docType != null && !docType.getStatusDocList().isEmpty()){               
            List<StatusesDoc> statuses = docType.getStatusDocList();           
            loadCounter = addStatusesInDoc(doc, statuses);            
        }
        return loadCounter;
    } 
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: добавление статусов в документ
     */
    private int addStatusesInDoc(Doc doc, List<StatusesDoc> statuses){
        List<DocsStatus> docsStatuses = doc.getDocsStatusList();
        List<StatusesDoc> statusesFromDoc = docsStatuses.stream().map(docsStatus -> docsStatus.getStatus()).collect(Collectors.toList());
            
        statuses.removeAll(statusesFromDoc);            
        statuses.stream()
                .map(statusDoc -> new DocsStatus(doc, statusDoc))
                .map(newStatus -> {
                    docsStatuses.add(newStatus);
                    return newStatus;})
                .forEach(newStatus -> getForAddDocStatus().add(newStatus));
        onItemChange();
        return statuses.size();
    }
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: Сохранение изменений в статусах документа
     */
    private void onSaveChangeDocStatus() {
        List<DocsStatus> forDelStatus = getForDelDocStatus();
        List<DocsStatus> forAddStatus = getForAddDocStatus();
        if (!forDelStatus.isEmpty()){
            forDelStatus.stream()
                    .filter(docsStatus -> docsStatus.getId() != null)
                    .forEach(docsStatus -> {
                        sessionBean.getDocStatusFacade().remove(docsStatus);
                        getItemFacade().addLogEvent(getEditedItem(), EscomBeanUtils.getBandleLabel("DeletedDocStatus"), docsStatus.toString(), currentUser);
                    });
        }        
        if (!forAddStatus.isEmpty()){            
            forAddStatus.stream().forEach(docsStatus -> {
                docsStatus.setId(null);
                sessionBean.getDocStatusFacade().create(docsStatus);
            });
        }
    }
    
    /* *** СТАТУСЫ ДОКУМЕНТА *** */
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: Изменение значения статуса документа в таблице статусов на карточке документа
     * @param docsStatus
     */   
    public void onChangeDocStatus(DocsStatus docsStatus){
        docsStatus.setAuthor(currentUser);
        getItemFacade().addLogEvent(getEditedItem(), EscomBeanUtils.getBandleLabel("ChangeDocStatus"), docsStatus.toString(), currentUser);
        onItemChange();
    }
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: Удаление статуса документа из редактируемого документа
     * @param docsStatus
     */
    public void onDeleteStatus(DocsStatus docsStatus){
        List<DocsStatus> docsStatuses = getEditedItem().getDocsStatusList();
        for (DocsStatus ds : docsStatuses){
            if (Objects.equals(ds, docsStatus)){
                docsStatuses.remove(ds);
                getForDelDocStatus().add(docsStatus);
                onItemChange();
                break;
            }
        }
    }
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: Удаление всех статусов документа из редактируемого документа
     */
    public void onDeleteAllStatus(){
        getForDelDocStatus().addAll(getEditedItem().getDocsStatusList());
        getEditedItem().getDocsStatusList().clear();
        onItemChange();
    }
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: Загрузка статусов в редактируемый документ из шаблона документа - вызов с формы
     */
    public void onLoadDocStatusFromDocType(){
        int loadCounter = addDocStatusFromType(getEditedItem(), getEditedItem().getDocType());
        Object[] params = new Object[]{loadCounter};
        EscomBeanUtils.SuccesFormatMessage("Successfully", "StatusesLoadComplete", params);
    }                 
    
    /**
     * СТАТУСЫ ДОКУМЕНТА: добавление в документ статусов из селектора
     * @param event
     */
    public void onAddStatusesFromSelector(SelectEvent event){
        if (event.getObject() != null){
            List<StatusesDoc> statuses = (List<StatusesDoc>) event.getObject();
            int loadCounter = addStatusesInDoc(getEditedItem(), statuses);
            Object[] params = new Object[]{loadCounter};
            EscomBeanUtils.SuccesFormatMessage("Successfully", "StatusesLoadComplete", params);
        }    
    }
    
    /**
     * Проверка регистрационного номера
     * @param doc
     * @param errors
     */
    public void checkRegNumber(Doc doc, Set<String> errors) {
        String regNumber = doc.getRegNumber();
        if (StringUtils.isNotBlank(regNumber)){
            if (!getItemFacade().checkRegNumber(regNumber, doc)) {
                errors.add("REGNUMBER_IS_DUBLICATE");              
            }
        }
    }

    public List<DocType> getDocTypes() {
        if (docTypes == null){
            docTypes = sessionBean.getDocTypeFacade().findAll().stream()
                    .filter(item -> sessionBean.preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return docTypes;
    }

    public List<Staff> getStaffs() {
        if (staffs == null) {
            staffs = sessionBean.getStaffFacade().findAll().stream()
                    .filter(item -> sessionBean.preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return staffs;
    }

    public List<Partner> getPartners() {
        if (partners == null){
            partners = sessionBean.getPartnersFacade().findAll().stream()
                    .filter(item -> sessionBean.preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return partners;
    }

    public List<DocsStatus> getForDelDocStatus() {
        return forDelDocStatus;
    }
    public List<DocsStatus> getForAddDocStatus() {
        return forAddDocStatus;
    }
    
    public String getDocURL() {
        return docURL;
    }
    public void setDocURL(String docURL) {
        this.docURL = docURL;
    }

    public Integer getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }
}