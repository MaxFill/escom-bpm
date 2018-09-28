package com.maxfill.services.files;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.services.searche.SearcheService;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FileServiceImpl implements FileService{
    protected static final Logger LOGGER = Logger.getLogger(FileServiceImpl.class.getName());
    
    @EJB
    protected Configuration conf;    
    @EJB
    private SearcheService searcheService;
        
    @Override
    @Asynchronous
    //TODO есть проблема при загрузке файлов из почтового сообщения почтовый ящик может закрыться раньше чем отработает upload!
    public synchronized void doUpload(Attaches attache, InputStream inputStream) {
        try {                                    
            String fileExt = attache.getExtension();
            String guid = attache.getGuid();
            
            StringBuilder sb = new StringBuilder(conf.getUploadPath());
            sb.append(guid.substring(0, 2))
                .append(File.separator)
                .append(guid.substring(2, 4));
                                            
            Files.createDirectories(Paths.get(sb.toString()));       
            sb.append(File.separator).append(guid).append(".").append(fileExt);
            Files.copy(inputStream, Paths.get(sb.toString()));                                  
                    
            if (!"PDF".equals(fileExt.toUpperCase())){
                String convPDF = conf.getConvertorPDF();
                if (StringUtils.isNotBlank(convPDF)){
                    makeCopyToPDF(conf.getUploadPath() + attache.getFullName(), convPDF);
                }
            } 
            if (attache.getDoc() != null){
               searcheService.updateFullTextIndex(attache.getDoc());
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
    
    //@Lock(LockType.WRITE)
    //@AccessTimeout(value = 20, unit = TimeUnit.SECONDS)
    /**
     * Конвертация загруженного файла в pdf 
     * Метод нельзя сделать ассинхронным, потому что pdf далее нужен сервису полнотекстового поиска!
     * @param file
     * @param pdfConvertor 
     */
    @Override
    public synchronized void makeCopyToPDF(String file, String pdfConvertor) {       
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
}
