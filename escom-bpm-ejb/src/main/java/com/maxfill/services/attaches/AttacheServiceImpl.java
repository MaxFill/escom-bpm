
package com.maxfill.services.attaches;

import com.maxfill.Configuration;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    @Override
    public void deleteTmpFiles(String login){
        try {
            String deleteDirectory = configuration.getTempFolder();
            Path path = Paths.get(deleteDirectory);
            Files.list(path).filter(p -> p.toString().contains(login))
                    .forEach(file -> {
                            try {
                                Files.deleteIfExists(file);
                            } catch (IOException ex) {
                                Logger.getLogger(AttacheServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    });
        } catch (IOException ex) {
            Logger.getLogger(AttacheServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
