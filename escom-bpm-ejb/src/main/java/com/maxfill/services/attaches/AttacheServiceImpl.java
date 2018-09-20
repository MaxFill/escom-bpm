package com.maxfill.services.attaches;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import com.maxfill.services.files.FileService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
            String fileName = configuration.getUploadPath() + attache.getFullName();
            File file = new File(fileName);
            file.delete();            
            File pdfFile = new File(FilenameUtils.removeExtension(fileName)+".pdf");
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
    public void doCopy(Attaches sourceAttache, Attaches targetAttache){
        String uploadPath = configuration.getUploadPath();
        
        String guid = targetAttache.getGuid();
        StringBuilder sb = new StringBuilder(uploadPath);
        sb.append(guid.substring(0, 2)).append(File.separator).append(guid.substring(2, 4));
        
        try {
            Files.createDirectories(Paths.get(sb.toString()));
        
            sb.setLength(0);
            Path targetPath = (Path) Paths.get(sb.append(uploadPath).append(targetAttache.getFullName()).toString());

            sb.setLength(0);        
            Path targetPathPDF = (Path) Paths.get(sb.append(uploadPath).append(targetAttache.getFullNamePDF()).toString());


            sb.setLength(0);        
            Path sourcePath = (Path) Paths.get(sb.append(uploadPath).append(sourceAttache.getFullName()).toString());

            sb.setLength(0);

            Path sourcePathPDF = (Path) Paths.get(sb.append(uploadPath).append(sourceAttache.getFullNamePDF()).toString());
        
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourcePathPDF, targetPathPDF, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Формирование zip архива из файлов папки во временной папке пользователя
     * @param folder
     * @param user
     * @param os
     * @return 
     */
    @Override
    public String makeFolderZIP(Folder folder, User user, String os){
        String zipFile = new StringBuilder()
                    .append(configuration.getTempFolder())
                    .append(folder.getName())
                    .append("_")
                    .append(user.getLogin())
                    .append(".zip").toString();
        byte[] buf = new byte[1024];
        
        File f = new File(zipFile);        
        
        String charsetName;
        switch (os){
            case "Linux":{
                charsetName = "Cp866";
                break;
            }
            default: charsetName = "UTF-8";
        }
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f), Charset.forName(charsetName))) {
            folder.getDetailItems().stream()
                .filter(doc->doc.getMainAttache() != null)
                .forEach(doc->{
                        Attaches attache = doc.getMainAttache();                        
                        try (InputStream inputStream = new FileInputStream(configuration.getUploadPath() + attache.getFullName())) {                            
                            ZipEntry zipEntry = new ZipEntry(new String(attache.getName().getBytes("UTF-8"), "UTF-8"));
                            out.putNextEntry(zipEntry);

                            int len;
                            while ((len = inputStream.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            out.closeEntry();
                        } catch (FileNotFoundException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
            );
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return zipFile;
    }
}
