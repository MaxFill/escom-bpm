package com.maxfill.services.files;

import com.maxfill.model.attaches.Attaches;
import java.io.InputStream;

public interface FileService {
    void doUpload(Attaches attache, InputStream inputStream);
    void makeCopyToPDF(String file, String pdfConvertor);
}
