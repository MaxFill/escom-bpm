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
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;


public final class EscomFileUtils {
    public static final int MAX_FILE_SIZE = 1000000;

    private EscomFileUtils() {
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
