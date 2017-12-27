package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.services.files.FileService;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/* Версии вложений, работа с прикреплёнными файлами  */
@Named
@SessionScoped
public class AttacheBean extends BaseDialogBean{
    private static final long serialVersionUID = -5107683464380454618L;
    
    @EJB
    protected FileService fileService;
    @EJB
    private DocFacade docFacade;
    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private AttacheService attacheService;
    @Inject
    private DocBean docBean;
            
    private StreamedContent content;     
       
    /* Копирует вложение */
    public Attaches copyAttache(Attaches sourceAttache){
        Attaches newAttache = attacheFacade.copyAttache(sourceAttache);        
        newAttache.setAuthor(sessionBean.getCurrentUser());                
        fileService.doCopy(sourceAttache, newAttache);
        
        return newAttache;
    }
    
    /* Загрузка файла вложения */ 
    public Attaches uploadAtache(UploadedFile uploadFile) throws IOException{
        if (uploadFile == null) return null;                

        String fileName = uploadFile.getFileName();

        Map<String, Object> params = new HashMap<>();
        params.put("contentType", uploadFile.getContentType());
        params.put("fileName", fileName);
        params.put("size", uploadFile.getSize());
        params.put("author", sessionBean.getCurrentUser());
        return attacheService.uploadAtache(params, uploadFile.getInputstream());
    }        
    
    @Override
    public void onOpenCard(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String path = null;
        if (params.containsKey("itemId")){
            Integer docId = Integer.valueOf(params.get("itemId"));
            Doc doc = docFacade.find(docId);
            if (doc == null) return;
            docBean.getItemFacade().actualizeRightItem(doc, getCurrentUser());
            if (docBean.getItemFacade().isHaveRightView(doc)) {
                Attaches attache = doc.getMainAttache();
                if (attache == null) return;
                path = conf.getUploadPath() + attache.getFullNamePDF();
            } else {
                EscomMsgUtils.warnMsgAdd("AccessDenied", "RightViewNo");
            }    
        } else {
            path = params.get("path");
        }
        
        if (path == null) {
            LOGGER.log(Level.SEVERE, null, "ESCOM_BPM ERROR: file path is null!");
            return;            
        }
        
        try {
            //!!! InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demo/images/optimus.jpg");
            content = new DefaultStreamedContent(new FileInputStream(path), "application/pdf");                
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public StreamedContent getContent() {
        return content;
    }
    public void setContent(StreamedContent content) {
        this.content = content;
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_DOC_VIEWER;
    }

    @Override
    protected void initBean() {       
    }

}