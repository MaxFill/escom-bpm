package com.maxfill.escom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;

public final class EscomFileUtils {
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
}
