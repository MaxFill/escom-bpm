package com.maxfill.escom.beans.system.license;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
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
    public void onExitProgram(){
        onCloseCard("exit");
    }

    public StreamedContent getContent() {
        if (content == null){
            content = new DefaultStreamedContent(
                    FacesContext.getCurrentInstance().getExternalContext()
                            .getResourceAsStream("/resources/License_ru.pdf"));
        }
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