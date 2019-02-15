package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.doc.Doc;
import java.io.IOException;
import java.util.Map;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;

@Named
@ViewScoped
public class AddAttacheBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 1614637915713000615L;
    
    @EJB
    private DocFacade docFacade;
    @EJB
    private AttacheFacade attacheFacade;
    @Inject
    private DocBean docBean;
                     
    private Doc doc;

    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (doc == null){
            if (params.containsKey("docId")){
                Integer docId = Integer.valueOf(params.get("docId"));
                doc = docFacade.find(docId);            
            }        
        }
    }    
    
    public void addAttache(FileUploadEvent event) throws IOException{
        Attaches attache = docBean.addAttacheFromFile(doc, event);
        if (attache != null){
            attacheFacade.setMainAttache(doc, attache);
            docFacade.edit(doc);
        }
    }
    
    public void addAttacheFromScan(SelectEvent event){
        docBean.addAttacheFromScan(doc, event);
        docFacade.edit(doc);
    }

    @Override
    public String getFormName(){
        return DictFrmName.FRM_ADD_ATTACHE;
    }

    public Doc getDoc() {
        return doc;
    }
    public void setDoc(Doc doc) {
        this.doc = doc;
    }
      
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ADD_VERSION");
    }    
}