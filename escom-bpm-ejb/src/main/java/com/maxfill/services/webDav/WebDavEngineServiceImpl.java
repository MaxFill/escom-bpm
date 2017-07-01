package com.maxfill.services.webDav;

import com.sun.security.auth.UserPrincipal;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateless
public class WebDavEngineServiceImpl implements WebDavService{
    
    @Override
    public void addAccess(){
        try {
            InitialContext initCtx = new InitialContext();
            Repository repository = (Repository)initCtx.lookup("jcr/sample");
            Credentials credentals = new SimpleCredentials("admin", "admin".toCharArray());
            Session session = repository.login(credentals);
            
            String path = "/test5";
            String[] privileges = new String[]{Privilege.JCR_ALL};
            Principal principal1 = new UserPrincipal("admin");
            
            final Principal principal2 = new Principal() {
            @Override
                public String getName() {
                    return "tester";
                }
            };
            
            AccessControlManager acm = session.getAccessControlManager();
            
            // Convert the privilege strings to Privilege instances ...
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
        } catch (NamingException | RepositoryException ex) {
            Logger.getLogger(WebDavEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
