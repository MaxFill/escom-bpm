package com.maxfill.utils;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;

/* Утилиты для работы с файлами */
public final class FileUtils {
    public static final int MAX_FILE_SIZE = 1000000;
    
    private FileUtils() {
    }   

    /* Открытие (скачивание) файла вложения для просмотра  */
    public static void attacheDownLoad(String filePathName, String fileName) {       
        try {                      
            fileName = MimeUtility.encodeText(fileName, "UTF-8", null);

            File file = new File(filePathName);

            // Get HTTP response
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            String contentType = "application/octet-stream; charset=UTF-8";

            // Set response headers
            response.reset();   // Reset the response in the first place
            response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Length", String.valueOf(file.length()));    //устанавливаем размер файла
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            response.setCharacterEncoding("UTF-8");

            // Read PDF contents
            try (OutputStream responseOutputStream = response.getOutputStream();
                    InputStream pdfInputStream = new FileInputStream(filePathName)) {

                // Read PDF contents and write them to the output
                byte[] bytesBuffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = pdfInputStream.read(bytesBuffer)) > 0) {
                    responseOutputStream.write(bytesBuffer, 0, bytesRead);
                }

                responseOutputStream.flush();
                responseOutputStream.close();

            } catch (IOException e) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            } finally {
                facesContext.responseComplete();
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    public static void doUpload(Attaches attache, InputStream inputStream, Configuration conf) throws IOException {
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
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    
    public static void makeCopyToPDF(String file, String convertor){
        if (StringUtils.isBlank(convertor)) return;
        try {            
            CommandLine commandLine = CommandLine.parse(convertor);
            commandLine.addArgument("-f");
            commandLine.addArgument("pdf");
            commandLine.addArgument(file);
            //System.out.println("Command line = " + commandLine.toString());
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}