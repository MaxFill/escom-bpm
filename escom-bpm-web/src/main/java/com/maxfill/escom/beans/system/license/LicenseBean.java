package com.maxfill.escom.beans.system.license;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@SessionScoped
public class LicenseBean extends BaseViewBean{
    private static final long serialVersionUID = -740004080013527296L; 
    private static final String LICENSE_FILE = "License_";        
    private StreamedContent content;
        
    /**
     * Принятие соглашения пользователем
     */
    public void onAcceptAndClose(){
       sessionBean.getUserSettings().setAgreeLicense(true);
       onCloseCard();
    }

    /**
     * Обработка события отказа от лицензионного соглашения
     * @return
     */
    public String onExitProgram(){
        return super.onCloseCard();
    }
    
    @Override
    public void onBeforeOpenCard(){
        if (content != null) return;
        Locale locale = sessionBean.getLocale();
        String localeName = locale.getLanguage().toLowerCase();
        String serverDir = System.getProperty("server-dir");
        StringBuilder sb = new StringBuilder(serverDir);
        sb.append(File.separator)
                .append(LICENSE_FILE)
                .append(localeName)
                .append(".pdf");
        try {
            content = new DefaultStreamedContent(
                    FacesContext.getCurrentInstance().getExternalContext()
                            .getResourceAsStream("/resources/License_ru.pdf"));
            //content = new DefaultStreamedContent(new FileInputStream(sb.toString()), "application/pdf");
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
        content = null;
        return super.onCloseCard();
    }

    @Override
    public String getFormName(){
        return DictDlgFrmName.FRM_AGREE_LICENSE;
    }
      
}