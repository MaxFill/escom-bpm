package com.maxfill.services.files;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.utils.EscomUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class FileServiceImpl implements FileService{
    protected static final Logger LOG = Logger.getLogger(FileServiceImpl.class.getName());
    
    @EJB    
    protected Configuration conf;
    
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
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
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
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
            String basePath = sb.append(uploadPath).append(attache.getGuid()).append(".").append(fileExt).toString();
            File outputFile = new File(basePath);
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(data, 0, data.length);
            if (!Objects.equals(fileExt.toUpperCase(), "PDF")){                                   
                makeCopyToPDF(basePath, conf.getConvertorPDF());
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
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
            Logger.getLogger(FileServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
