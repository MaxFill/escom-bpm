
package com.maxfill.services.attaches;

import com.maxfill.Configuration;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import java.io.File;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.io.FilenameUtils;

/* Сервис работы с файлами вложений */
@Stateless
public class AttacheServiceImpl implements AttacheService{
    @EJB
    private AttacheFacade attacheFacade;    
    @EJB
    private Configuration configuration;
    
    @Override
    public Attaches findAttacheByDoc(Doc doc){
        return attacheFacade.findCurrentAttacheByDoc(doc);         
    }
    
    @Override
    public void deleteAttache(Attaches attache){
        StringBuilder sb = new StringBuilder(configuration.getUploadPath());
        sb.append(attache.getFullName());
        String fileName = sb.toString();
        File file = new File(fileName);
        file.delete();
        String pdfFileName = FilenameUtils.removeExtension(fileName)+".pdf";
        File pdfFile = new File(pdfFileName);
        pdfFile.delete();
    }
        
    @Override
    public void deleteAttaches(List<Attaches> attaches){
        if (attaches != null){
            attaches.stream().forEach(attache -> deleteAttache(attache));
        }
    }
    
    @Override
    public void deleteAttacheByFolder(Folder folder){
        folder.getDocsList().stream().forEach(doc -> deleteAttaches(doc.getAttachesList()));
    }
}
