
package com.maxfill.services.attaches;

import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.folders.Folders;
import java.io.File;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author mfilatov
 */
@Stateless
public class AttacheServiceImpl implements AttacheService{
    @EJB
    private AttacheFacade attacheFacade;    
    
    @Override
    public Attaches findAttacheByDoc(Doc doc){
        return attacheFacade.findCurrentAttacheByDoc(doc);         
    }
    
    /**
     * Удаление файла вложения
     * @param attache
     */
    @Override
    public void deleteAttache(Attaches attache){
        String fileName = attache.getFullName();
        File file = new File(fileName);
        file.delete();
    }
    
    /**
     * Удаление файлов вложений 
     * @param attaches 
     */
    @Override
    public void deleteAttaches(List<Attaches> attaches){
        if (attaches != null){
            attaches.stream().forEach(attache -> deleteAttache(attache));
        }
    }
    
    /**
     * Удаление файлов вложений из всех документов папки
     * @param folder 
     */
    @Override
    public void deleteAttacheByFolder(Folders folder){
        folder.getDocsList().stream().forEach(doc -> deleteAttaches(doc.getAttachesList()));
    }
}
