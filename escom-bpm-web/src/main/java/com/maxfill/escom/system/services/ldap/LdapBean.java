package com.maxfill.escom.system.services.ldap;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.ldap.LdapSettings;
import com.maxfill.services.ldap.LdapUtils;
import com.maxfill.services.ldap.LdapTimer;
import com.maxfill.services.ldap.LdapUsers;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.company.CompanyFacade;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.department.DepartmentFacade;
import com.maxfill.model.basedict.post.Post;
import com.maxfill.model.basedict.post.PostFacade;
import com.maxfill.services.BaseTimer;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.xml.bind.JAXB;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class LdapBean extends BaseServicesBean<LdapSettings>{
    private static final long serialVersionUID = 8484737343525831475L;
    private List<LdapUsers> ldapUsers = new ArrayList<>();
  
    @EJB
    private LdapTimer ldapTimer; 
    @EJB
    private CompanyFacade companyFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private PostFacade postFacade;

    @Override
    public void doBeforeOpenCard(Map<String, String> params) {
        if (settings != null){
            if (settings.getCompanyId() != null){
                Company company = companyFacade.find(settings.getCompanyId());
                settings.setCompany(company);
            }
            if (settings.getDepartmentId()!= null){
                Department department = departmentFacade.find(settings.getDepartmentId());
                settings.setDepartment(department);
            }
            if (settings.getPostId() != null){
                Post post = postFacade.find(settings.getPostId());
                settings.setPost(post);
            }
        }
    }

    @Override
    public void onSaveSettings(){
        if (settings.getCompany() == null){
            settings.setCompanyId(null);
        } else {
            settings.setCompanyId(settings.getCompany().getId());
        }
        if (settings.getDepartment()== null){
            settings.setDepartmentId(null);
        } else {
            settings.setDepartmentId(settings.getDepartment().getId());
        }
        if (settings.getPost() == null){
            settings.setPostId(null);
        } else {
            settings.setPostId(settings.getPost().getId());
        }
        super.onSaveSettings();
    }    
    
    /**
     * Тест получения пользователей из LDAP
     */
    public void onTestLoadUsers(){        
        ldapUsers = ldapTimer.doLoadUsers(Boolean.TRUE, getSettings());
        if (ldapUsers == null){
            MsgUtils.errorMsg("ConnectFailed");
        }
    }    
   
    /**
     * Тест проверки соединения с LDAP
     */
    public void onCheckConnect(){     
        try {
            LdapUtils.initLDAP(getSettings().getLdapUsername(), getSettings().getLdapPassword(), getSettings().getLdapAdServer());
            MsgUtils.succesMsg("ConnectionEstablished");

        } catch (NamingException ex) {
            MsgUtils.warnMsg("ConnectFailed");
            MsgUtils.errorMessage(ex.getMessage());
            LOG.log(Level.SEVERE, null, ex);
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
    } 

    public List<LdapUsers> getLdapUsers() {
        return ldapUsers;
    }

    @Override
    protected LdapSettings createSettings(){
        byte[] compressXML = service.getSettings();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                settings = (LdapSettings) JAXB.unmarshal(new StringReader(settingsXML), LdapSettings.class); 
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        if (settings == null) {
            settings = new LdapSettings();
        }
        return settings;
    }

    /**
     * Ручной запуск службы на выполнение
     */
    @Override
    public void doRunService(){        
        ldapTimer.doExecuteTask(service, getSettings());       
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_LDAP;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("LoadLdapUsers");
    }
    
    @Override
    public int getSERVICE_ID() {
        return DictServices.LDAP_ID;
    }

    @Override
    public BaseTimer getTimerFacade() {
        return ldapTimer;
    }
}