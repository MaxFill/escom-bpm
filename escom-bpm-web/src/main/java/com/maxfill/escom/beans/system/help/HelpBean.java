package com.maxfill.escom.beans.system.help;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named
@SessionScoped
public class HelpBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -740004080013527296L; 
    private static final String HELP_FILE = "UserManual_Storage.pdf";        
    private StreamedContent content;
        
    public StreamedContent getContent() {
        String separator = File.separator;
        StringBuilder sb = new StringBuilder(conf.getServerPath());
        sb.append("standalone")
                .append(separator)                
                .append("configuration")
                .append(separator)
                .append(HELP_FILE);
        try {
            content = new DefaultStreamedContent(new FileInputStream(sb.toString()), "application/pdf");
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return content;
    }

    @Override
    public String getFormName(){
        return DictFrmName.FRM_HELP;
    }
      
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ManualBook");
    }
}