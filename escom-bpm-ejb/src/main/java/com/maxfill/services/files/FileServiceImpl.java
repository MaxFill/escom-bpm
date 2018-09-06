package com.maxfill.services.files;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.WRITE)
@AccessTimeout(value = 20, unit = TimeUnit.SECONDS)
public class FileServiceImpl implements FileService{
    protected static final Logger LOGGER = Logger.getLogger(FileServiceImpl.class.getName());
    
    @EJB
    protected Configuration conf;    
    
    @Override
    //@Asynchronous
    // отключил, потому что при загрузке файлов из почтового сообщения почтовый ящик может закрыться раньше чем отработает upload!
    public void doUpload(Attaches attache, InputStream inputStream) {
        try {                                    
            String fileExt = attache.getExtension();
            String guid = attache.getGuid();
            
            StringBuilder sb = new StringBuilder(conf.getUploadPath());
            sb.append(guid.substring(0, 2))
                .append(File.separator)
                .append(guid.substring(2, 4));
                                            
            Files.createDirectories(Paths.get(sb.toString())); 
      
            String fullPath = sb.append(File.separator).append(guid).append(".").append(fileExt).toString();
                        
            Files.copy(inputStream, Paths.get(fullPath)); 

            String convPDF = conf.getConvertorPDF();
            if (StringUtils.isNotBlank(convPDF) && !Objects.equals(fileExt.toUpperCase(), "PDF")){
                makeCopyToPDF(fullPath, convPDF);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void makeCopyToPDF(String file, String pdfConvertor) {       
        try {            
            CommandLine commandLine = CommandLine.parse(pdfConvertor);
            commandLine.addArgument("-f");
            commandLine.addArgument("pdf");
            commandLine.addArgument(file);
            //System.out.println("PDFConvertor command line = " + commandLine.toString());
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
      
    @Override
    public String loadAttacheContent(Attaches attache){
        if (attache == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(conf.getUploadPath()).append(attache.getGuid()).append(".");
        if ("txt".equals(attache.getExtension().toLowerCase())){
            sb.append(attache.getExtension());            
            return loadContentFromTXT(new File(sb.toString()));
        } else {            
            return loadContentFromPDF(sb.toString());
        }
    } 
        
    private String loadContentFromTXT(File txtFile){
        String content = "";
        if (txtFile.exists()){
            try {
                    String path = txtFile.getPath();
                    byte[] encoded = Files.readAllBytes(Paths.get(path));
                    Charset encoding = StandardCharsets.UTF_8;
                    content = new String(encoded, encoding);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        return content;
    }
    
    /* Получение текстового контента из файла pdf */
    private String loadContentFromPDF(String basePath){
        String convertTXT = conf.getConvertorTXT();
        if (org.apache.commons.lang.StringUtils.isEmpty(convertTXT)) return "";
        
        String content = "";        
        String pdfFileName = basePath + "pdf";
        String txtFileName = basePath + "txt";
        
        try {
            CommandLine commandLine = CommandLine.parse(convertTXT);
            commandLine.addArgument(pdfFileName);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);          //создан временный файл txt    
            File txtFile = new File(txtFileName);   
            content = loadContentFromTXT(txtFile);
            txtFile.delete();                       //удалён временный файл
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return content;
    }
}
