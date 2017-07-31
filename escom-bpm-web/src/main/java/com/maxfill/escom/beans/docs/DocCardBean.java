package com.maxfill.escom.beans.docs;

import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.partners.Partner;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.facade.DocStatusFacade;
import com.maxfill.services.numerator.DocNumerator;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
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
import javax.inject.Inject;

/* Карточка документа */

@Named
@ViewScoped
public class DocCardBean extends BaseCardBean<Doc>{
    private static final long serialVersionUID = -8151932533993171177L;

    private final List<DocStatuses> forDelDocStatus = new ArrayList<>();
    private final List<Attaches> forDelAttaches = new ArrayList<>();
    
    private String docURL;  
    private Attaches selectedAttache;
    
    @Inject
    private DocBean docsBean;
            
    @EJB
    private DocNumerator docNumeratorService;
    @EJB
    private DocFacade itemFacade;
    @EJB
    private DocStatusFacade docStatusFacade;
    @EJB
    private AttacheFacade attacheFacade;
       
    @Override
    public String doFinalCancelSave() {      
        List<Attaches> notSaveAttaches = getEditedItem().getAttachesList().stream()
                .filter(attache -> attache.getId() == null).collect(Collectors.toList());
        attacheService.deleteAttaches(notSaveAttaches); //удаление файлов вложений не сохранённых версий
        return super.doFinalCancelSave();
    }    
    
    @Override
    protected void doPrepareOpen(Doc doc) {
        if (getTypeEdit().equals(DictEditMode.INSERT_MODE) && getEditedItem().getAttachesList().size() >0){
            onItemChange(); //установка признака что документ изменён
        }
    }
     
    @Override
    protected void checkItemBeforeSave(Doc doc, Set<String> errors){        
        checkRegNumber(doc, errors);
        super.checkItemBeforeSave(doc, errors);
    }        
    
    @Override
    public DocFacade getItemFacade() {
        return itemFacade;
    }         
        
    @Override
    public void onAfterSaveItem(Doc doc){        
        onSaveChangeDocStatus();
        onSaveChangeAttaches();
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
                        getItemFacade().addLogEvent(getEditedItem(), EscomBeanUtils.getBandleLabel("DeletedDocStatus"), docsStatus.toString(), currentUser);
                    });
        }        
    }     
        
    /* Запрос на формирование ссылки URL для просмотра документа  */
    public void onGetDocViewURL(Doc doc){
        docURL = EscomBeanUtils.doGetItemURL(doc, "docs/document");
    }
    
    /* Запрос на формирование ссылки URL на открытие карточки документа */
    public void onGetDocOpenURL(Doc doc){
        docURL = EscomBeanUtils.doGetItemURL(doc, "folders/folder-explorer");
    }   
            
    /* Сброс регистрационного номера */
    public void onClearRegNumber(Doc doc){
        if (doc.getDocType() != null){
            NumeratorPattern numerator = doc.getDocType().getNumerator();
            if (numerator != null && DictNumerator.TYPE_AUTO.equals(numerator.getTypeCode())){
                docNumeratorService.doRollBackRegistred(doc, "");
            }
        }
        doc.setRegNumber(null);
        onItemChange();
        EscomBeanUtils.WarnMsgAdd("DocRegCanceled", "DocIsEnableEditing");
    }

    /* Формирование регистрационного номера документа. Вызов с экранной формы */
    public void onGenerateRegNumber(Doc doc){
        docNumeratorService.doRegistDoc(doc);
        onItemChange();
    }       
    
    /* Подготовка к отправке текущего документа на e-mail  */
    public void prepareSendMailDoc(String mode){
        List<BaseDict> docs = new ArrayList<>();
        docs.add(getEditedItem());
        sessionBean.openMailMsgForm(mode, docs);
    }
    
    /* ВЛОЖЕНИЯ */
    
    public void onSetSelectedAttache(Attaches attache){
       selectedAttache = attache;
    }
    
    /* Добавление версии к документу   */
    public void addAttacheFromFile(FileUploadEvent event) throws IOException{
        onItemChange();        
        docsBean.addAttacheFromFile(getEditedItem(), event);
    }
    
    public void addAttacheFromScan(SelectEvent event){
        onItemChange();
        docsBean.addAttacheFromScan(getEditedItem(), event);
    }
    
    /* Удаление текущей версии документа  */
    public void removeCurrentVersion(){
        Attaches version =  null;
        Doc doc = getEditedItem();
  
        for (Integer number = doc.getAttache().getNumber(); number >= 1; --number){
            for(Attaches attache : doc.getAttachesList()){
                if (Objects.equals(number, attache.getNumber())){
                    version = attache;
                    break;
                }
            }
        }
        makeCurrentVersion(doc, version);                
        onItemChange(); 
    }
    
    /* Установка текущей версии в редактируемом документе  */
    public void makeCurrentVersion(Attaches attache){
        makeCurrentVersion(getEditedItem(), attache);
        onItemChange();
    }
    
    /* Установка текущей версии в документе  */
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
    
    /* Удаление версии документа и её файла */
    public void deleteAttache(Attaches attache) {
        try {
            Doc doc = getEditedItem();
            forDelAttaches.add(attache);
            doc.getAttachesList().remove(attache);
            if (attache.equals(doc.getAttache())){
                removeCurrentVersion();
            }
            onItemChange();
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, null, exception);
            EscomBeanUtils.ErrorMessage(exception.getMessage());
        }
    }
    
    /* Скачивание текущей версии документа */
    public void downloadCurrentVersion(){
        if (getEditedItem() == null) return;        
        Doc doc = getEditedItem();        
        Attaches attache = attacheService.findAttacheByDoc(doc);                               
        if (attache != null){
            attacheDownLoad(attache);               
        } else {
            EscomBeanUtils.WarnMsgAdd("Error", "FileNotFound");
        }
    }        
    
    public void onLockSelectedAttache(){
        if (selectedAttache == null) return;
        docsBean.onOpenFormLockAttache(selectedAttache);
    }
    
    public void onOpenFormLockAttache(Attaches attache){
        docsBean.onOpenFormLockAttache(attache);
    }
    
    /* Событие изменения контрагента на форме документа */
    public void onPartnerSelected(SelectEvent event){
        List<Partner> items = (List<Partner>) event.getObject();
        if (items.isEmpty()){return;}
        Partner item = items.get(0);
        onItemChange();
        getEditedItem().setPartner(item);
    } 
    public void onPartnerSelected(ValueChangeEvent event){
        Partner partner = (Partner) event.getNewValue();
        getEditedItem().setPartner(partner);
    }     
    
    /* Событие изменения типа документа на форме документа  */
    public void onDocTypeSelected(SelectEvent event){
        List<DocType> items = (List<DocType>) event.getObject();
        if (items.isEmpty()){return;}
        DocType item = items.get(0);
        getEditedItem().setDocType(item);
        onItemChange();
        docsBean.addDocStatusFromDocType(getEditedItem(),item);
    }
    public void onDocTypeSelected(ValueChangeEvent event){        
        DocType docType = (DocType) event.getNewValue();
        getEditedItem().setDocType(docType);
        docsBean.addDocStatusFromDocType(getEditedItem(), docType);
    }      
  
    /* Возвращает true если у документа есть заблокированные вложения */
    public boolean getDocIsLock(){
        return docsBean.docIsLock(getEditedItem());
    }
    
    @Override
    public String prepSaveItemAndPublic(){
        getItemFacade().doSetStateById(getEditedItem(), DictStates.STATE_VALID);
        return super.prepSaveItemAndClose(); 
    }
    
    /* СТАТУСЫ ДОКУМЕНТА */
              
    /* Изменение значения статуса документа в таблице статусов на карточке документа */   
    public void onChangeDocStatus(DocStatuses docsStatus){
        docsStatus.setAuthor(currentUser);
        getItemFacade().addLogEvent(getEditedItem(), EscomBeanUtils.getBandleLabel("ChangeDocStatus"), docsStatus.toString(), currentUser);
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
        int loadCounter = docsBean.addDocStatusFromDocType(getEditedItem(), getEditedItem().getDocType());
        if (loadCounter > 0){
            onItemChange();
            Object[] params = new Object[]{loadCounter};
            EscomBeanUtils.SuccesFormatMessage("Successfully", "StatusesLoadComplete", params);
        } else {
            EscomBeanUtils.WarnMsgAdd("Warning", "StatusesNotFound");
        }    
    }                 
    
    /* Добавление в документ статусов из селектора */
    public void onAddStatusesFromSelector(SelectEvent event){
        if (event.getObject() != null){
            List<StatusesDoc> statuses = (List<StatusesDoc>) event.getObject();
            int loadCounter = docsBean.addStatusesInDoc(getEditedItem(), statuses);
            Object[] params = new Object[]{loadCounter};
            EscomBeanUtils.SuccesFormatMessage("Successfully", "StatusesLoadComplete", params);
        }    
    }
    
    /* Проверка регистрационного номера   */
    public void checkRegNumber(Doc doc, Set<String> errors) {
        String regNumber = doc.getRegNumber();
        if (StringUtils.isNotBlank(regNumber)){
            if (!getItemFacade().checkRegNumber(regNumber, doc)) {
                errors.add("REGNUMBER_IS_DUBLICATE");              
            }
        }
    }

    public void onUpdateSelectedAttache(){
        List<Attaches> attaches = getEditedItem().getAttachesList();
        attaches.remove(selectedAttache);
        selectedAttache = attacheFacade.find(selectedAttache.getId());
        attaches.add(selectedAttache);
    }
    
    /* GETS & SETS */
    
    public String getDocURL() {
        return docURL;
    }
    public void setDocURL(String docURL) {
        this.docURL = docURL;
    }
    
    @Override
    public Class<Doc> getItemClass() {
        return Doc.class;
    }
}