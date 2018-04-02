package com.maxfill.escom.beans.system.help;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.maxfill.utils.Tuple;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@SessionScoped
public class HelpBean extends BaseDialogBean{
    private static final long serialVersionUID = -740004080013527296L; 
    private static final String HELP_FILE = "UserManual_Storage.pdf";        
    private StreamedContent content;
        
    @Override
    protected void initBean(){        
    }    
    
    @Override
    public void onBeforeOpenCard(){
        if (content != null) return;
        String separator = File.separator;
        StringBuilder sb = new StringBuilder(conf.getServerPath());
        sb.append("standalone")
                .append(separator)                
                .append("configuration")
                .append(separator)
                .append(HELP_FILE);
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
        content = null;
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_HELP;
    }
      
}