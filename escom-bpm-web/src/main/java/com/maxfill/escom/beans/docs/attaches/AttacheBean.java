package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import java.io.FileInputStream;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/* Версии вложений, работа с прикреплёнными файлами  */
@Named
@SessionScoped
public class AttacheBean extends BaseDialogBean{
    private static final long serialVersionUID = -5107683464380454618L;
    
    @EJB
    private DocFacade docFacade;
    @Inject
    private DocBean docBean;
            
    private StreamedContent content;     
        
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