
package com.maxfill.services.attaches;

import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folders;
import java.util.List;

/**
 *
 * @author mfilatov
 */
public interface AttacheService {
    
    void deleteAttache(Attaches attache);
    void deleteAttaches(List<Attaches> attaches);
    void deleteAttacheByFolder(Folders folder);
    Attaches findAttacheByDoc(Doc doc);
    

}
