package com.maxfill.services.files;

import com.maxfill.Configuration;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class FileServiceImpl implements FileService{
    protected static final Logger LOGGER = Logger.getLogger(FileServiceImpl.class.getName());
    
    @EJB    
    protected Configuration conf;
    @EJB
    private AttacheFacade attacheFacade;
    
    @Override
    @Asynchronous
    public void doCopy(Attaches sourceAttache, Attaches targetAttache){
        String uploadPath = conf.getUploadPath();
        StringBuilder sb = new StringBuilder();        
        Path targetPath = (Path) Paths.get(sb.append(uploadPath).append(targetAttache.getFullName()).toString());
        
        sb.setLength(0);        
        Path targetPathPDF = (Path) Paths.get(sb.append(uploadPath).append(targetAttache.getFullNamePDF()).toString());
        
        sb.setLength(0);        
        Path sourcePath = (Path) Paths.get(sb.append(uploadPath).append(sourceAttache.getFullName()).toString());
        
        sb.setLength(0);
        Path sourcePathPDF = (Path) Paths.get(sb.append(uploadPath).append(sourceAttache.getFullNamePDF()).toString());
        try  {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourcePathPDF, targetPathPDF, StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
    
    @Override
    @Asynchronous
    public void doUpload(Attaches attache, InputStream inputStream) {
        FileOutputStream outputStream = null;
        try {
            String uploadPath = conf.getUploadPath();
            StringBuilder sb = new StringBuilder();
            String fileName = attache.getName();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
            String basePath = sb.append(uploadPath).append(attache.getGuid()).append(".").append(fileExt).toString();
            File outputFile = new File(basePath);
            outputStream = new FileOutputStream(outputFile);
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            if (!Objects.equals(fileExt.toUpperCase(), "PDF")){                                   
                makeCopyToPDF(basePath, conf.getConvertorPDF());
            }
            loadContentFromPDF(attache, basePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /* Загрузка в attache текстового контента из pdf */
    @Asynchronous
    private void loadContentFromPDF(Attaches attache, String basePath){        
        String pdfFileName = FilenameUtils.removeExtension(basePath)+".pdf";
        try {            
            CommandLine commandLine = CommandLine.parse("pdftotxt.cmd");            
            commandLine.addArgument(pdfFileName);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);    
            String txtFileName = FilenameUtils.removeExtension(basePath)+".txt";
            File txtFile = new File(txtFileName);
            if (txtFile.exists()){
                byte[] encoded = Files.readAllBytes(Paths.get(txtFileName));
                Charset encoding = StandardCharsets.UTF_8;
                String content = new String(encoded, encoding);
                attache.setContent(content);
                txtFile.delete();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    @Asynchronous
    public void uploadScan(Attaches attache, byte[] data){
        FileOutputStream outputStream = null;
        try {            
            String uploadPath = conf.getUploadPath();
            StringBuilder sb = new StringBuilder();
            String fileName = attache.getName();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            String basePath = sb.append(uploadPath).append(attache.getGuid()).append(".").append(fileExt).toString();
            File outputFile = new File(basePath);
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(data, 0, data.length);
            if (!Objects.equals(fileExt.toLowerCase(), "pdf")){                                   
                makeCopyToPDF(basePath, conf.getConvertorPDF());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void makeCopyToPDF(String file, String pdfConvertor) {       
        if (StringUtils.isBlank(pdfConvertor)) return;
        try {            
            CommandLine commandLine = CommandLine.parse(pdfConvertor);
            commandLine.addArgument("-f");
            commandLine.addArgument("pdf");
            commandLine.addArgument(file);
            //System.out.println("Command line = " + commandLine.toString());
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    
}
