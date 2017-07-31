package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.files.FileService;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
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
    @Inject
    private DocBean docBean;
            
    private StreamedContent content;     
       
    /* Копирует вложение */
    public Attaches copyAttache(Attaches sourceAttache){
        Attaches newAttache = new Attaches();        
        newAttache.setName(sourceAttache.getName());
        newAttache.setExtension(sourceAttache.getExtension());
        newAttache.setType(sourceAttache.getType());
        newAttache.setSize(sourceAttache.getSize());
        newAttache.setAuthor(sessionBean.getCurrentUser());
        newAttache.setDateCreate(new Date());
        
        fileService.doCopy(sourceAttache, newAttache);
        
        return newAttache;
    }
    
     /* Вложения */ 
    public Attaches uploadAtache(UploadedFile uploadFile) throws IOException{
        if (uploadFile == null) return null;                

        int length = uploadFile.getContents().length;

        String fileName = uploadFile.getFileName();
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

        Attaches attache = new Attaches();
        attache.setName(fileName);
        attache.setExtension(fileExt);
        attache.setType(uploadFile.getContentType());
        attache.setSize(length);
        attache.setAuthor(sessionBean.getCurrentUser());
        attache.setDateCreate(new Date());

        fileService.doUpload(attache, uploadFile.getInputstream());            

        return attache;
    }
    
    @Override
    protected void initBean(){
    }
    
    @Override
    public void onOpenCard(){
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String path = null;
        if (params.containsKey("itemId")){
            Integer docId = Integer.valueOf(params.get("itemId"));
            Doc doc = docFacade.find(docId);
            if (doc == null) return;
            docBean.actualizeRightItem(doc);
            if (docBean.isHaveRightView(doc)) {
                Attaches attache = doc.getAttache();
                if (attache == null) return;
                path = conf.getUploadPath() + attache.getFullNamePDF();
            } else {
                EscomBeanUtils.WarnMsgAdd("AccessDenied", "RightViewNo");
            }    
        } else {
            path = params.get("path");
        }
        
        if (path == null) return;
        try {
            //!!! InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demo/images/optimus.jpg");
            content = new DefaultStreamedContent(new FileInputStream(path), "application/pdf");                
        } catch (Exception e) {
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

}