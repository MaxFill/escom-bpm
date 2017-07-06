/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.utils;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
import static com.maxfill.utils.FileUtils.doUpload;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;


public final class EscomFileUtils {
    public static final int MAX_FILE_SIZE = 1000000;
    private  static final Logger LOG = Logger.getLogger(EscomFileUtils.class.getName());
    
    private EscomFileUtils() {
    }
    
    /* Открытие (скачивание) файла вложения для просмотра  */
    public static void attacheDownLoad(String filePathName, String fileName) {       
        try {                      
            fileName = MimeUtility.encodeText(fileName, "UTF-8", null);

            File file = new File(filePathName);
           
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
                LOG.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                facesContext.responseComplete();
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    /* Загрузка файла на сервер  */
    public static Attaches uploadAtache(UploadedFile uploadFile, User user, Configuration conf) throws IOException {
        Attaches attache = new Attaches();
        if (uploadFile != null) {
            int length = uploadFile.getContents().length;

            String fileName = uploadFile.getFileName();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

            attache.setName(fileName);
            attache.setExtension(fileExt);
            attache.setType(uploadFile.getContentType());
            attache.setSize(length);
            attache.setAuthor(user);
            attache.setDateCreate(new Date());                        

            doUpload(attache, uploadFile.getInputstream(), conf);            
        }
        return attache;
    }
    
    /* Обработка действия загрузки файла  */
    public static UploadedFile handleUploadFile(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        if (file == null) {
            return null;
        }
        try {
            int length = file.getContents().length;
            if (length > MAX_FILE_SIZE) {
                String message = EscomBeanUtils.getBandleLabel("INVALID_FILE_SIZE");
                throw new Exception(message);
            }
            return file;

        } catch (Exception exception) {
            Logger.getLogger(EscomFileUtils.class.getName()).log(Level.SEVERE, null, exception);
            return null;
        }
    }
}
