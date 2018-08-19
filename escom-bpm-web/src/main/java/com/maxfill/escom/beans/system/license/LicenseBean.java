package com.maxfill.escom.beans.system.license;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import java.io.IOException;
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
    private StreamedContent content;

    /**
     * Принятие соглашения пользователем
     */
    public void onAcceptAndClose(){
       sessionBean.getUserSettings().setAgreeLicense(true);
       onCloseCard(SysParams.EXIT_NEED_UPDATE);
    }

    /**
     * Обработка события отказа от лицензионного соглашения
     */
    public void onExitProgram(){
        onCloseCard(SysParams.EXIT);
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
    public String onCloseCard(Object result) {
         try {
            if (content != null && content.getStream() != null){
                content.getStream().close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return super.onCloseCard(result);
    }

    @Override
    public String getFormName(){
        return DictFrmName.FRM_AGREE_LICENSE;
    }
     
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("LicenseAgreement");
    }
    
}