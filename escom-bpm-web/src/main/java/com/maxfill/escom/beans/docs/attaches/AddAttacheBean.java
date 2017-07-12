package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@ViewScoped
public class AddAttacheBean extends BaseDialogBean{
    private static final long serialVersionUID = 1614637915713000615L;
    
    @EJB
    private DocFacade docFacade;
    @Inject
    private DocBean docBean;
                     
    private Doc doc;
    
    @Override
    protected void initBean(){
    }
    
    @Override
    public void onOpenCard(){
        if (doc == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (params.containsKey("docId")){
                Integer docId = Integer.valueOf(params.get("docId"));
                doc = docFacade.find(docId);            
            }        
        }
    }    
    
    public void addAttache(FileUploadEvent event) throws IOException{
        docBean.addAttache(doc, event);
        docFacade.edit(doc);        
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_ADD_ATTACHE;
    }

    public Doc getDoc() {
        return doc;
    }
    public void setDoc(Doc doc) {
        this.doc = doc;
    }
          
}