package com.maxfill.escom.beans.docs.attaches;

import com.maxfill.escom.beans.BaseDialogBean;
import java.io.FileInputStream;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/* Версии вложений, работа с прикреплёнными файлами  */
@Named
@ViewScoped
public class AttacheBean extends BaseDialogBean{
    private static final long serialVersionUID = -5107683464380454618L;
    
    private StreamedContent content; 
    
    @Override
    public void onOpenCard(){
        if (content != null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String path = params.get("path");
            String contentType = params.get("contentType");
            try {
                content = new DefaultStreamedContent(new FileInputStream(path), contentType);                
            } catch (Exception e) {  
            } 
        } 
    }

    public StreamedContent getContent() {
        return content;
    }
    public void setContent(StreamedContent content) {
        this.content = content;
    }

    @Override
    protected String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

}