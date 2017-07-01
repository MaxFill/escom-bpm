package com.maxfill.escom.beans.docs;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.folders.FoldersBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.FileUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import java.io.IOException;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named(value = "docsBean")
@SessionScoped
public class DocBean extends BaseExplBeanGroups<Doc, Folder>{
    private static final long serialVersionUID = 923378036800543406L;         
    
    @Inject
    private FoldersBean ownerBean;
    
    @EJB
    private DocFacade docsFacade;    
    
    @Override
    public void preparePasteItem(Doc pasteItem, BaseDict target){        
        pasteItem.setOwner((Folder)target);
    }

    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item);
        } 
        if (item.getOwner() != null) {
            Rights childRight = ownerBean.getRightForChild(item.getOwner()); //РїРѕР»СѓС‡Р°РµРј РїСЂР°РІР° РёР· СЃРїРµС†.РїСЂР°РІ 
            if (childRight != null){
                return childRight;
            }
        }
        return getDefaultRights(item);  
    }        
    
    @Override
    public void setSpecAtrForNewItem(Doc doc, Map<String, Object> params){
        Folder folder = doc.getOwner();
        if (folder != null){
            DocType docType = folder.getDocTypeDefault();
            doc.setDocType(docType);
        }
        doc.setDateDoc(new Date());
        if (doc.getOwner().getId() == null){ //СЃР±СЂРѕСЃ owner РµСЃР»Рё РґРѕРєСѓРјРµРЅС‚ СЃРѕР·РґР°С‘С‚СЃСЏ РІ РєРѕСЂРЅРµ Р°СЂС…РёР°!
            doc.setOwner(null);
        }
        if (params != null && !params.isEmpty()){
            Attaches attache = (Attaches)params.get("attache");
            if (attache != null){
                Integer version = doc.getNextVersionNumber();            
                attache.setNumber(version);
                attache.setDoc(doc);
                String fileName = attache.getName();
                doc.setName(fileName);
                doc.getAttachesList().add(attache);
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
 
    public void openDocCountTypesReport(){
        sessionBean.openDialogFrm(DictDlgFrmName.REP_DOC_COUNT_TYPES, new HashMap<>());
    }
    
    public void addAttache(FileUploadEvent event) throws IOException{     
        UploadedFile uploadedFile = FileUtils.handleUploadFile(event);
        User author = sessionBean.getCurrentUser();        
        Attaches attache = FileUtils.doUploadAtache(uploadedFile, author, sessionBean.getConfiguration());
        Doc doc = (Doc) event.getComponent().getAttributes().get("item");
        Integer version = doc.getNextVersionNumber();            
        attache.setNumber(version);
        attache.setDoc(doc);
        attache.setCurrent(Boolean.TRUE);
        List<Attaches> attaches = doc.getAttachesList();
        attaches.stream()
                .filter(attacheVersion -> attacheVersion.getCurrent())
                .forEach(attacheVersion -> attacheVersion.setCurrent(false));
        doc.getAttachesList().add(attache);
        getItemFacade().edit(doc);
        EscomBeanUtils.SuccesMsgAdd("Successfully", "VersionAdded");
    }
    
    public void onOpenFormLockMainAttache(Doc doc){
        Attaches attache = doc.getAttache();
        if (attache != null){
            onOpenFormLockAttache(attache);
        } else {
            EscomBeanUtils.WarnMsgAdd("Attention", "DocumentDoNotContainMajorVersion");
        }
    }
    
    public void onOpenFormLockAttache(Attaches attache){
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> paramList = new ArrayList<>();
        paramList.add(attache.getId().toString());
        paramMap.put("attache", paramList);
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_DOC_LOCK, paramMap);    
    }
    
    @FacesConverter("docsConvertor")
    public static class docsConvertors implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {       
                 DocBean bean = EscomBeanUtils.findBean("docsBean", fc);
                 return bean.getItemFacade().find(Integer.parseInt(value));
             } catch(NumberFormatException e) {
                 throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
             }
         }
         else {
             return null;
         }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((User)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}