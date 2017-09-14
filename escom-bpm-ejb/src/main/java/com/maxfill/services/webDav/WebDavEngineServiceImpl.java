package com.maxfill.services.webDav;

import com.maxfill.Configuration;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
import com.maxfill.services.files.FileService;
import com.maxfill.services.searche.SearcheService;
import com.sun.security.auth.UserPrincipal;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jcr.Binary;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;

@Stateless
public class WebDavEngineServiceImpl implements WebDavService{    
    private static final String WEBDAV_STORAGE = "other";
    private static final Logger LOGGER = Logger.getLogger(WebDavEngineServiceImpl.class.getName());
    
    @EJB
    private Configuration conf;
    @EJB 
    private FileService fileService;
    @EJB
    private SearcheService searcheService;
        
    /* Загрузка файла в хранилище */
    @Override
    public void uploadFile(Attaches attache){
        Session session = getSession();
        if (attache == null || session == null) return; 
        try {
            String path = conf.getUploadPath() + attache.getFullName();
            File file = new File(path);
            
            Node rootFolder = session.getRootNode();
            Node folder = rootFolder.addNode(attache.getGuid());   
      
            InputStream stream = new BufferedInputStream(new FileInputStream(file));
            Node fileNode = folder.addNode(attache.getName(), "nt:file");                    
            
            Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
            Binary binary = session.getValueFactory().createBinary(stream);
            contentNode.setProperty("jcr:data", binary);

            // TODO установить права !
            session.save();
        } catch (RepositoryException | FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /* Извлечение файла из хранилища */
    @Asynchronous
    @Override
    public void downloadFile(Attaches attache){ 
        downloadFile(attache, attache);
    }
    
    @Asynchronous
    @Override
    public void downloadFile(Attaches attache, Attaches targetAttache){ 
        try {
            Session session = getSession();
            
            Node rootFolder = session.getRootNode();
            Node folder = rootFolder.getNode(attache.getGuid());            
            Node fileNode = folder.getNode(attache.getName());            
            Node contentNode = fileNode.getNode("jcr:content");                       
            
            /*
            // Get the MIME type if it's there ...
            String mimeType = null;
            if ( contentNode.hasProperty("jcr:mimeType") ) {
                mimeType = contentNode.getProperty("jcr:mimeType").getString();
            }
            */
            //String id = contentNode.getIdentifier();
                        
            Binary content = contentNode.getProperty("jcr:data").getBinary();
            fileService.doUpload(targetAttache, content.getStream());
            
            contentNode.remove();
            fileNode.remove();
            folder.remove();            
            session.save();
            if (attache.getCurrent()){
                searcheService.updateFullTextIndex(attache.getDoc());
            }
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private Session getSession(){
        Session session = null;
        try {            
            Repository repository = conf.getRepository();
            Credentials credentals = new SimpleCredentials("WebDavAdmin", "admin".toCharArray());        
            session = repository.login(credentals,  WEBDAV_STORAGE);            
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return session;
    }
    
    public void addAccess(){
        try {
            Session session = getSession();           
            
            String path = "/test5";
            String[] privileges = new String[]{Privilege.JCR_ALL};
            Principal principal1 = new UserPrincipal("admin");
            Principal principal2 = new UserPrincipal("tester");
            
            //final Principal principal2 = () -> "tester";
            
            AccessControlManager acm = session.getAccessControlManager();
            
            Privilege[] permissions = new Privilege[privileges.length];
            for (int i = 0; i < privileges.length; i++) {
                permissions[i] = acm.privilegeFromName(privileges[i]);
            }
                       
            AccessControlList acl ;
            AccessControlPolicyIterator it = acm.getApplicablePolicies(path);
            
            if (it.hasNext()) {
                acl = (AccessControlList)it.nextAccessControlPolicy();
            } else {
                acl = (AccessControlList)acm.getPolicies(path)[0];
            }
            acl.addAccessControlEntry(principal1, permissions);
            acl.addAccessControlEntry(principal2, permissions);
                        
            acm.setPolicy(path, acl);
            session.save();
        } catch (RepositoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
}
