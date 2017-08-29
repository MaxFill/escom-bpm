package com.maxfill.escom.beans.docs;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictStates;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.folders.FoldersBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getBandleLabel;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.model.users.User;
import java.io.IOException;
import java.text.MessageFormat;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
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
    }
        
    private void copyMainAttacheFromDoc(Doc pasteItem, Doc sourceItem){
        Attaches sourceAttache = sourceItem.getAttache();
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
            return EscomBeanUtils.getBandleLabel("DocIsNotRegistred");
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
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;        

        if (!item.isInherits()) {
            return getActualRightItem(item);
        }
        if (item.getOwner() != null) {
            Rights childRight = ownerBean.getRightForChild(item.getOwner()); 
            if (childRight != null) {
                return childRight;
            }
        }
        return getDefaultRights(item);
    }

    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params) {
        Folder folder = doc.getOwner();
        doSetDefaultDocType(doc, folder);
        doSetDefaultCompany(doc, folder);
        doSetDefaultPartner(doc, folder);           
    
        //addDocStatusFromDocType(doc, docType);
        doc.setDateDoc(new Date());
        if (doc.getOwner().getId() == null) { 
            doc.setOwner(null);
        }
        if (params != null && !params.isEmpty()) {
            Attaches attache = (Attaches) params.get("attache");
            if (attache != null) {
                Integer version = doc.getNextVersionNumber();
                attache.setNumber(version);
                attache.setDoc(doc);
                String fileName = attache.getName();
                doc.setName(fileName);
                doc.getAttachesList().add(attache);
            }
        }
    }

    private void doSetDefaultPartner(Doc doc, Folder folder){
        if (doc.getPartner() == null && folder != null){
            if (folder.isInheritPartner()){
                doSetDefaultPartner(doc, folder.getParent());
            } else {
                doc.setPartner(folder.getPartnerDefault());
            }             
        }
    }

    private void doSetDefaultCompany(Doc doc, Folder folder){
        if (doc.getCompany() == null && folder != null){
            if (folder.isInheritCompany()){
                doSetDefaultCompany(doc, folder.getParent());
            } else {
                doc.setCompany(folder.getCompanyDefault());
            }
        }
    }
    
    private void doSetDefaultDocType(Doc doc, Folder folder){
        if (doc.getDocType() == null && folder != null){
            if (folder.isInheritDocType()){
                doSetDefaultDocType(doc, folder.getParent());
            } else {
                doc.setDocType(folder.getDocTypeDefault()); 
            }
        }
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
    public BaseExplBean getDetailBean() {
        return null;
    }

    @Override
    public Class<Doc> getItemClass() {
        return Doc.class;
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
        EscomBeanUtils.SuccesMsgAdd("Successfully", "VersionAdded");
        return attache;
    }            
    
    public void onOpenFormLockMainAttache(Doc doc) {
        Attaches attache = doc.getAttache();
        if (attache != null) {
            Set<String> errors = new HashSet<>();
            makeRightItem(doc);
            if (!isHaveRightEdit(doc)){              
                String objName = getBandleLabel(getMetadatesObj().getBundleName()) + ": " + doc.getName();
                String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
                errors.add(error);
            }
            if (!errors.isEmpty()){
                EscomBeanUtils.showErrorsMsg(errors);
                return;
            }
            onOpenFormLockAttache(attache);
        } else {
            EscomBeanUtils.WarnMsgAdd("Attention", "DocumentDoNotContainMajorVersion");
        }        
    }

    /* Открытие формы добавления версии */
    public void openAttacheAddForm(Doc doc){
        if (doc == null) return;
        Set<String> errors = new HashSet<>();
        makeRightItem(doc);
        if (!isHaveRightEdit(doc)){              
            String objName = getBandleLabel(getMetadatesObj().getBundleName()) + ": " + doc.getName();
            String error = MessageFormat.format(getMessageLabel("RightEditNo"), new Object[]{objName});
            errors.add(error);
        }
        if (!errors.isEmpty()){
            EscomBeanUtils.showErrorsMsg(errors);
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
        actualizeRightItem(doc);
        if (isHaveRightView(doc)) {
            Attaches attache = doc.getAttache();
            if (attache != null) {
                attacheDownLoadPDF(attache);
            } else {
                EscomBeanUtils.WarnMsgAdd("Attention", "DocumentDoNotContainMajorVersion");
            }
        } else {
            EscomBeanUtils.WarnMsgAdd("AccessDenied", "RightViewNo");
        }
    }

    /* Просмотр файла вложения основной версии документа как PDF */
    public void onViewMainAttache(Doc doc) {
        if (doc == null) return;
        
        actualizeRightItem(doc);
        if (isHaveRightView(doc)) {
            Attaches attache = doc.getAttache();
            if (attache != null) {
                onViewAttache(attache);
            } else {
                EscomBeanUtils.WarnMsgAdd("Attention", "DocumentDoNotContainMajorVersion");
            }
        } else {
            EscomBeanUtils.WarnMsgAdd("AccessDenied", "RightViewNo");
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

    @FacesConverter("docsConvertor")
    public static class docsConvertors implements Converter {

        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
            if (value != null && value.trim().length() > 0) {
                try {
                    DocBean bean = EscomBeanUtils.findBean("docsBean", fc);
                    return bean.getItemFacade().find(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
                }
            } else {
                return null;
            }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if (object != null) {
                return String.valueOf(((User) object).getId());
            } else {
                return "";
            }
        }
    }
}
