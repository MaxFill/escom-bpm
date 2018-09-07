package com.maxfill.services.attaches;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mfilatov
 */
public interface AttacheService {
    
    void deleteAttache(Attaches attache);
    void deleteAttaches(List<Attaches> attaches);
    void deleteAttacheByFolder(Folder folder);
    void deleteTmpFiles(String login);
    Attaches findAttacheByDoc(Doc doc);
    Attaches uploadAtache(Map<String, Object> params, InputStream inputStream) throws IOException;
    void doCopy(Attaches sourceAttache, Attaches targetAttache);
}
