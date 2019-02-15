package com.maxfill.escom.beans.docs.files;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.docs.attaches.AttacheBean;
import com.maxfill.escom.beans.explorer.ExplorerTreeBean;
import com.maxfill.escom.utils.EscomFileUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.folder.Folder;
import com.maxfill.model.basedict.folder.FoldersFacade;
import com.maxfill.model.basedict.user.User;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named
@ViewScoped
public class UploadFilesBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 1614637915713000615L;
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private FoldersFacade foldersFacade;
    @EJB
    private AttacheFacade attacheFacade;    
     
    @Inject
    private AttacheBean attacheBean;
    
    private Folder folder;
    private boolean canCreateDocs = false;
    private final Set<FacesMessage> messages = new HashSet<>();
    
    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (sourceBean != null){
            folder = (Folder) ((ExplorerTreeBean)sourceBean).getCurrentItem();
            if (foldersFacade.isHaveRightAddDetail(folder)){
                canCreateDocs = true;
            }
        }
    }    
    
    @Override
    public void onAfterFormLoad(){  
        if (!canCreateDocs()){
            MsgUtils.errorFormatMsg("RightAddDocsInFolderNo", new Object[]{folder.getName()});
        }
    }
    
    @Override
    public String onCloseCard(Object param){    
        if (isItemChange){
            param = SysParams.EXIT_ONLY_REFRESH;
        } else {
            param = SysParams.EXIT_NOTHING_TODO;
        }
        return super.onCloseCard(param);
    }
    
    public void uploadFile(FileUploadEvent event) throws IOException{
        UploadedFile uploadFile = event.getFile(); 
        if (uploadFile == null){
            messages.add(MsgUtils.prepFormatErrorMsg("ErrorFileOperation", new Object[]{}));
            return;
        }
        String fileName = uploadFile.getFileName();
        User author = getCurrentUser();
        Doc doc = docFacade.createDocInUserFolder(fileName, author, folder);        
        if (doc == null){ 
            messages.add(MsgUtils.prepFormatErrorMsg("FailedCreateDocFromFile", new Object[]{fileName}));
            return ;
        }        
        Attaches attache = attacheBean.uploadAtache(uploadFile);
        attache.setCurrent(Boolean.TRUE);
        attache.setDoc(doc);
        attacheFacade.create(attache);        
        messages.add(MsgUtils.prepFormatSuccesMsg("DocumentCreated", new Object[]{fileName}));        
    }
    
    public void onFinishUpload(){
        onItemChange();
        MsgUtils.showFacesMessages(messages);
        messages.clear();        
    }
    
    public boolean canCreateDocs(){
        return canCreateDocs;
    }

    public Set<FacesMessage> getMessages() {
        return messages;
    }
        
    @Override
    public String getFormName(){
        return DictFrmName.FRM_UPLOAD_FILES;
    }
      
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("UploadFilesCreateDocs");
    }    
}