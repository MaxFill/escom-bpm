package com.maxfill.escom.system.services.ldap;

import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.services.ldap.LdapSettings;
import com.maxfill.services.ldap.LdapUtils;
import com.maxfill.services.ldap.LdapTimer;
import com.maxfill.services.ldap.LdapUsers;
import com.maxfill.escom.beans.BaseServicesBean;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.EscomUtils;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.xml.bind.JAXB;
import org.apache.commons.lang3.StringUtils;

@Named
@ViewScoped
public class LdapBean extends BaseServicesBean<LdapSettings>{
    private static final long serialVersionUID = 8484737343525831475L;
    private List<LdapUsers> ldapUsers = new ArrayList<>();
  
    @EJB
    private LdapTimer ldapTimer;   

    /**
     * Тест получения пользователей из LDAP
     */
    public void onTestLoadUsers(){
        StringBuilder detailInfo = new StringBuilder();        
        ldapUsers = ldapTimer.doLoadUsers(detailInfo, Boolean.TRUE, getSettings());
        if (ldapUsers == null){
            EscomBeanUtils.ErrorMsgAdd("Warning", "ConnectFailed", "");
        }
    }    
   
    /**
     * Тест проверки соединения с LDAP
     */
    public void onCheckConnect(){     
        try {
            LdapUtils.initLDAP(getSettings().getLdapUsername(), getSettings().getLdapPassword(), getSettings().getLdapAdServer());
            EscomBeanUtils.SuccesMsgAdd("Successfully", "ConnectionEstablished");

        } catch (NamingException ex) {
            EscomBeanUtils.WarnMsgAdd("Warning", "ConnectFailed");
            EscomBeanUtils.ErrorMessage(ex.getMessage());
            LOG.log(Level.SEVERE, null, ex);
        }
    } 

    public List<LdapUsers> getLdapUsers() {
        return ldapUsers;
    }

    @Override
    protected LdapSettings createSettings() {     
        LdapSettings settings;
        if (StringUtils.isNotBlank(service.getSettings())){
            settings = (LdapSettings) JAXB.unmarshal(new StringReader(service.getSettings()), LdapSettings.class); 
        } else {
            settings = new LdapSettings();
        }
        return settings;
    }

    @Override
    public Integer getSERVICE_ID() {
        return 1;
    }

    @Override
    public BaseTimer getTimerFacade() {
        return ldapTimer;
    }

    /**
     * Ручной запуск службы на выполнение
     */
    @Override
    public void doRunService(){        
        ServicesEvents selectedEvent = ldapTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }

}