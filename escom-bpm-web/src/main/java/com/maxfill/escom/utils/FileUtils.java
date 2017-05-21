package com.maxfill.escom.utils;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
import com.maxfill.utils.ItemUtils;
import com.maxfill.utils.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/* Утилиты для работы с файлами */
public final class FileUtils {
    public static final int MAX_FILE_SIZE = 1000000;
    
    private FileUtils() {
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
                String message = ItemUtils.getBandleLabel("INVALID_FILE_SIZE");
                throw new Exception(message);
            }
            return file;

        } catch (Exception exception) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, exception);
            return null;
        }
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

    /* Загрузка файла на сервер  */
    public static Attaches doUploadAtache(UploadedFile uploadFile, User user, String uploadPath) throws IOException {
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

            StringBuilder sb = new StringBuilder();
            String basePath = sb.append(uploadPath).append(attache.getGuid()).append(".").append(fileExt).toString();
            File outputFile = new File(basePath);

            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = uploadFile.getInputstream();
                outputStream = new FileOutputStream(outputFile);

                int read;
                final byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                if (!Objects.equals(fileExt.toUpperCase(), "PDF")){                                   
                    makeCopyToPDF(basePath);
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
        return attache;
    }

    public static void makeCopyToPDF(String file){
        try {            
            CommandLine commandLine = CommandLine.parse("unoconv.cmd");
            commandLine.addArgument("-f");
            commandLine.addArgument("pdf");
            commandLine.addArgument(file);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(commandLine);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
