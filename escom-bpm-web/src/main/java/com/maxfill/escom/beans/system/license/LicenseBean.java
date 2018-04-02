package com.maxfill.escom.beans.system.license;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.BaseDialogBean;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
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
        return super.onFinalCloseCard("no_agree");
    }
    
    @Override
    public void onBeforeOpenCard(){
        if (content != null) return;
        String separator = File.separator;
        Locale locale = sessionBean.getLocale();
        String localeName = locale.getLanguage().toLowerCase();
        StringBuilder sb = new StringBuilder(conf.getServerPath());
        sb.append("standalone")
                .append(separator)
                .append("configuration")
                .append(separator)
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
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_AGREE_LICENSE;
    }
      
}