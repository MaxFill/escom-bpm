package com.maxfill.utils;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author mfilatov
 */
public final class FileUtils {
    public static final int MAX_FILE_SIZE = 1000000;

    /* Путь загрузки файлов вложений */
    public static final String UPLOAD_PATCH = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("FilesUploadPath");
    
    private FileUtils() {
    }

    /**
     * Просмотр вложения
     *
     * @param attache
     */
    public static void viewAttache(Attaches attache) {
        String path = attache.getFullName();
        String contentType = attache.getType();
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", true);
        options.put("modal", true);
        options.put("width", 900);
        options.put("minWidth", 900);
        options.put("height", 600);
        options.put("minHeight", 400);
        options.put("maximizable", true);
        options.put("closable", true);
        options.put("closeOnEscape", true);
        Map<String, List<String>> paramMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        List<String> contentTypeList = new ArrayList<>();
        pathList.add(path);
        contentTypeList.add(contentType);
        paramMap.put("path", pathList);
        paramMap.put("contentType", contentTypeList);
        RequestContext.getCurrentInstance().openDialog("doc-viewer", options, paramMap);
    }

    /**
     * Обработка действия загрузки файла
     *
     * @param event
     * @return
     */
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

    /**
     * Открытие (скачивание) файла вложения для просмотра
     *
     * @param attache
     */
    public static void attacheDownLoad(Attaches attache) {       
        try {
            String filePathName = attache.getFullName();            
            String fileName = MimeUtility.encodeText(attache.getName(), "UTF-8", null);

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

    /**
     * Загрузка файла на сервер
     *
     * @param uploadFile
     * @param user
     * @return
     * @throws java.io.IOException
     */
    public static Attaches doUploadAtache(UploadedFile uploadFile, User user) throws IOException {
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

            String basePath = UPLOAD_PATCH + attache.getGuid() + "." + fileExt;
            File outputFilePath = new File(basePath);

            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = uploadFile.getInputstream();
                outputStream = new FileOutputStream(outputFilePath);

                int read;
                final byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
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

}
