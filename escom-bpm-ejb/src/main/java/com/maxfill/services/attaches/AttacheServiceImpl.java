package com.maxfill.services.attaches;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import com.maxfill.services.files.FileService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.io.FilenameUtils;

/* Сервис работы с файлами вложений */
@Stateless
public class AttacheServiceImpl implements AttacheService{
    private static final Logger LOGGER = Logger.getLogger(AttacheServiceImpl.class.getName());
    @EJB
    private AttacheFacade attacheFacade;    
    @EJB
    private DocFacade docFacade;
    @EJB
    private Configuration configuration;
    @EJB
    private FileService fileService;

    /**
     * Загрузка файла на сервер с созданием Attaches
     * @param params
     * @param inputStream
     * @return 
     * @throws java.io.IOException
     */
    @Override
    public Attaches uploadAtache(Map<String, Object> params, InputStream inputStream) throws IOException{
        if (params.isEmpty()) return null;        

        String fileName = (String) params.get("fileName");
        String contType = (String) params.get("contentType");
        Long size = (Long) params.get("size");
        User author = (User) params.get("author");

        String fileExt;
        if (params.containsKey("fileExt")) {
            fileExt = (String) params.get("fileExt");
        } else{
            fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        Attaches attache = new Attaches();
        attache.setName(fileName);
        attache.setExtension(fileExt);
        attache.setType(contType);
        attache.setSize(size);
        attache.setAuthor(author);
        attache.setDateCreate(new Date());

        fileService.doUpload(attache, inputStream);
        return attache;
    }
    
    @Override
    public Attaches findAttacheByDoc(Doc doc){
        return attacheFacade.findCurrentAttacheByDoc(doc);         
    }
    
    @Override
    public void deleteAttache(Attaches attache){
        try{
            StringBuilder sb = new StringBuilder(configuration.getUploadPath());
            sb.append(attache.getFullName());
            String fileName = sb.toString();
            File file = new File(fileName);
            file.delete();
            String pdfFileName = FilenameUtils.removeExtension(fileName)+".pdf";
            File pdfFile = new File(pdfFileName);
            pdfFile.delete();
        } catch (Exception ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
        
    @Override
    public void deleteAttaches(List<Attaches> attaches){
        if (attaches != null){
            attaches.stream().forEach(attache -> deleteAttache(attache));
        }
    }
    
    @Override
    public void deleteAttacheByFolder(Folder folder){
        folder.getDetailItems().stream().forEach(doc -> deleteAttaches(doc.getAttachesList()));
    }
    
    @Override
    @Asynchronous
    public void deleteTmpFiles(String login){
        try {
            String deleteDirectory = configuration.getTempFolder();
            Path path = Paths.get(deleteDirectory);
            Files.list(path).filter(p -> p.toString().contains(login))
                    .forEach(file -> {
                            try {
                                Files.deleteIfExists(file);
                            } catch (IOException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                    });
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    @Asynchronous
    public void uploadAsynhAttache(Doc doc, Map<String, Object> params, InputStream inputStream) throws IOException {
        Attaches attache = uploadAtache(params, inputStream);
        Integer version = doc.getNextVersionNumber();
        attache.setNumber(version);
        attache.setDoc(doc);
        String fileName = attache.getName();
        doc.setName(fileName);
        doc.getAttachesList().add(attache);
        docFacade.edit(doc);
    }
}
