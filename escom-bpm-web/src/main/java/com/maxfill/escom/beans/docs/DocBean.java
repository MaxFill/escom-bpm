package com.maxfill.escom.beans.docs;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictStates;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.folders.FoldersBean;

import static com.maxfill.escom.utils.EscomMsgUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomMsgUtils.getMessageLabel;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.statuses.StatusesDoc;

import java.io.IOException;
import java.text.MessageFormat;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

@Named(value = "docsBean")
@SessionScoped
public class DocBean extends BaseExplBeanGroups<Doc, Folder> {

    private static final long serialVersionUID = 923378036800543406L;
    private Integer documentId; //используется при открытии файла на просмотр через прямую гиперсылку 

    @Inject
    private FoldersBean ownerBean;
    @Inject
    private AttacheBean attacheBean; 
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private DocFacade docsFacade;
    
    /* перед вставкой скопированного документа */
    @Override
    public void preparePasteItem(Doc pasteItem, Doc sourceItem, BaseDict owner) {
        super.preparePasteItem(pasteItem, sourceItem, owner);
        pasteItem.setAttachesList(new ArrayList<>());
        pasteItem.setDocsStatusList(new ArrayList<>());
        pasteItem.setDocsLinks(new ArrayList<>());
        pasteItem.setRegNumber(null);        
        if (owner == null){
            owner = sourceItem.getOwner();
        }
        pasteItem.setOwner((Folder) owner);        
    }    
    
    @Override
    protected void doPasteMakeSpecActions(Doc sourceItem, Doc pasteItem){
        super.doPasteMakeSpecActions(sourceItem, pasteItem);
        copyMainAttacheFromDoc(pasteItem, sourceItem);
        copyDocStatuses(pasteItem, sourceItem);
        getItemFacade().edit(pasteItem);
    }

    private void copyMainAttacheFromDoc(Doc pasteItem, Doc sourceItem){
        Attaches sourceAttache = sourceItem.getMainAttache();
        if (sourceAttache != null){
            Attaches attache = attacheBean.copyAttache(sourceAttache);
            attacheFacade.addAttacheInDoc(pasteItem, attache);
        }
    }
    
    private void copyDocStatuses(Doc pasteItem, Doc sourceItem){
        List<DocStatuses> sourceStatuses = sourceItem.getDocsStatusList();
        List<DocStatuses> pasteStatuses = pasteItem.getDocsStatusList();
        for (DocStatuses docStatus : sourceStatuses){            
            pasteStatuses.add(new DocStatuses(pasteItem, docStatus.getStatus()));
        }
    }
    
    /* Возвращает полное регистрационное имя документа */
    public String getFullRegistrName(Doc doc){
        if (StringUtils.isNotBlank(doc.getRegNumber())){
            StringBuilder builder = new StringBuilder();
            if (doc.getDocType() != null && StringUtils.isNotBlank(doc.getDocType().getName())){
                builder.append(doc.getDocType().getName()).append(" ");
            }
            builder.append(doc.getRegNumber()).append(" ");
            builder.append(doc.getNameEndElipse());
            return builder.toString();
        } else {
            return EscomMsgUtils.getBandleLabel("DocIsNotRegistred");
        }
    }
    
    /* СТАТУСЫ ДОКУМЕНТА: Добавление статусов документа из шаблона типа документа   */
    public int addDocStatusFromDocType(Doc doc, DocType docType){        
        if (docType == null || docType.getStatusDocList().isEmpty()) return 0;        
        List<StatusesDoc> statuses = docType.getStatusDocList();           
        return addStatusesInDoc(doc, statuses);               
    } 
    
    /* СТАТУСЫ ДОКУМЕНТА: добавление статусов в документ */
    public int addStatusesInDoc(Doc doc, List<StatusesDoc> statusesForAdd){
        List<DocStatuses> docsStatuses = doc.getDocsStatusList();
        List<StatusesDoc> statusesFromDoc = docsStatuses.stream().map(docsStatus -> docsStatus.getStatus()).collect(Collectors.toList());
            
        statusesForAdd.removeAll(statusesFromDoc);      //из списка добавляемых удалить уже имеющиеся в документе статусы
        statusesForAdd.stream().map(statusDoc -> new DocStatuses(doc, statusDoc))
                .forEach(docsStatus -> docsStatuses.add(docsStatus));
        return statusesForAdd.size();
    }
    
    @Override
    public SearcheModel initSearcheModel() {
        return new DocsSearche();
    }

    @Override
    public List<Folder> getGroups(Doc item) {
        List<Folder> groups = new ArrayList<>();
        groups.add(item.getOwner());
        return groups;
    }

    @Override
    public DocFacade getItemFacade() {
        return docsFacade;
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public BaseExplBean getGroupBean() {
        return ownerBean;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    @Override
    public Class<Folder> getOwnerClass() {
        return Folder.class;
    }

    @Override
    protected void checkLockItem(Doc item, Set<String> errors){
        if (item.getState().getId().equals(DictStates.STATE_EDITED)){
            Object[] messageParameters = new Object[]{item.getName(), getItemFacade().getActorName(item, "editor")};
            String error = MessageFormat.format(getMessageLabel("ObjectIsLockUser"), messageParameters);
            errors.add(error);
            return;
        }
        super.checkLockItem(item, errors);
    }
    
    /* ПЕЧАТЬ: Открытие формы предввода отчёта по видам документов */
    public void openDocCountTypesReport() {
        sessionBean.openDialogFrm(DictDlgFrmName.REP_DOC_COUNT_TYPES, new HashMap<>());
    }    
        
    public boolean docIsLock(Doc doc) {
        return DictStates.STATE_EDITED.equals(doc.getState().getCurrentState().getId());
    }      
             
    /* ВЛОЖЕНИЯ */
    
    /* Возвращает максимальный размер загружаемого файла */    
    public Integer getMaxFileSize(){
        return conf.getMaxFileSize();
    }
    
    public Attaches addAttacheFromScan(Doc doc, SelectEvent event){
        if (event.getObject() == null) return null;
        Attaches attache = (Attaches) event.getObject();
        attacheFacade.addAttacheInDoc(doc, attache);
        return attache;
    }
    
    public Attaches addAttacheFromFile(Doc doc, FileUploadEvent event) throws IOException {
        UploadedFile uploadedFile = EscomFileUtils.handleUploadFile(event);
        Attaches attache = attacheBean.uploadAtache(uploadedFile);
        //Doc doc = (Doc) event.getComponent().getAttributes().get("item");
        attacheFacade.addAttacheInDoc(doc, attache);
        EscomMsgUtils.succesMsg("VersionAdded");
        return attache;
    }            
    
    public void onOpenFormLockMainAttache(Doc doc) {
        Attaches attache = doc.getMainAttache();
        if (attache != null) {
            Set<String> errors = new HashSet<>();
            getItemFacade().makeRightItem(doc, currentUser);
            if (!getItemFacade().isHaveRightEdit(doc)){
                String objName = getBandleLabel(getMetadatesObj().getBundleName()) + ": " + doc.getName();
                String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
                errors.add(error);
            }
            if (!errors.isEmpty()){
                EscomMsgUtils.showErrorsMsg(errors);
                return;
            }
            onOpenFormLockAttache(attache);
        } else {
            EscomMsgUtils.warnMsg("DocumentDoNotContainMajorVersion");
        }        
    }

    /* Открытие формы добавления версии */
    public void openAttacheAddForm(Doc doc){
        if (doc == null) return;
        Set<String> errors = new HashSet<>();
        getItemFacade().makeRightItem(doc, currentUser);
        if (!getItemFacade().isHaveRightEdit(doc)){
            String objName = getBandleLabel(getMetadatesObj().getBundleName()) + ": " + doc.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
        if (!errors.isEmpty()){
            EscomMsgUtils.showErrorsMsg(errors);
            return;
        }
        sessionBean.openAttacheAddForm(doc);
    }
    
    public void onOpenFormLockAttache(Attaches attache) {
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> paramList = new ArrayList<>();
        paramList.add(attache.getId().toString());
        paramMap.put("attache", paramList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_LOCK, paramMap);
    }

    public void onDownLoadAttachePDFByExtLink() {
        if (documentId == null) return;            
        Doc doc = docsFacade.find(documentId);
        if (doc == null) return;
        getItemFacade().actualizeRightItem(doc, currentUser);
        if (getItemFacade().isHaveRightView(doc)) {
            Attaches attache = doc.getMainAttache();
            if (attache != null) {
                attacheDownLoadPDF(attache);
            } else {
                EscomMsgUtils.warnMsg("DocumentDoNotContainMajorVersion");
            }
        } else {
            EscomMsgUtils.warnMsg("RightViewNo");
        }
    }

    /* Просмотр файла вложения основной версии документа как PDF */
    public void onViewMainAttache(Doc doc) {
        if (doc == null) return;
        getItemFacade().actualizeRightItem(doc, currentUser);
        if (getItemFacade().isHaveRightView(doc)) {
            Attaches attache = doc.getMainAttache();
            if (attache != null) {
                onViewAttache(attache);
            } else {
                EscomMsgUtils.warnMsg("DocumentDoNotContainMajorVersion");
            }
        } else {
            EscomMsgUtils.warnMsg("RightViewNo");
        }
    }

    /* Скачивание файла вложения основной версии документа как PDF */
    public void attacheDownLoadPDF(Attaches attache){
        if (attache == null) return;
        String path = conf.getUploadPath() + attache.getFullNamePDF();         
        EscomFileUtils.attacheDownLoad(path, attache.getNamePDF());
    }
    
    public Integer getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

}
