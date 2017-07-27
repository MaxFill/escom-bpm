package com.maxfill.escom.beans.system.license;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.BaseDialogBean;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@SessionScoped
public class LicenseBean extends BaseDialogBean{
    private static final long serialVersionUID = -740004080013527296L; 
    private static final String LICENSE_FILE = "License_";        
    private StreamedContent content;
        
    @Override
    protected void initBean(){        
    }
    
    public void onAcceptAndClose(){
       sessionBean.getUserSettings().setAgreeLicense(true);
       String targetPage = SysParams.MAIN_PAGE;
       sessionBean.redirectToPage(targetPage, Boolean.FALSE);
    }
    
    public void onExitProgram(){
        sessionBean.onSessionExit();
    }
    
    @Override
    public void onOpenCard(){    
        if (content != null) return;
        String separator = File.separator;
        Locale locale = sessionBean.getLocale();
        String localeName = locale.getLanguage();
        StringBuilder sb = new StringBuilder(conf.getServerPath());
        sb.append("standalone")
                .append(separator)
                .append("configuration")
                .append(separator)
                .append(LICENSE_FILE)
                .append(localeName)
                .append(".pdf");
        try {
            //!!! InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/demo/images/optimus.jpg");
            content = new DefaultStreamedContent(new FileInputStream(sb.toString()), "application/pdf");                
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
        return DictDlgFrmName.FRM_AGREE_LICENSE;
    }
      
}