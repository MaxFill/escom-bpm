package com.maxfill.escom.system.services.mail;

import com.maxfill.escom.beans.BaseServicesBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.*;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Бин для формы "Служба получения e-mail сообщений"
 */
@Named
@ViewScoped
public class MailReaderBean extends BaseServicesBean<MailSettings>{
    private static final long serialVersionUID = 8484737343525831475L;
    private static final Integer SERVICE_ID = 3;
    
    @EJB
    private MailReaderTimer mailReaderTimer;

    //Todo переделать c учётом ssl!
    public void onCheckConnect(){
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());
        try {
            String adress = getSettings().getAdressSender();
            Folder inbox = MailUtils.sessionReader(getSettings(), auth);
            if (inbox != null) {
                EscomBeanUtils.SuccesFormatMessage("Successfully", "MessageSent", new Object[]{adress});
            } else {
                EscomBeanUtils.errorMsgAdd("Error", "ConnectFailed", "");
            }
        } catch (SecurityException | MessagingException ex) {
            EscomBeanUtils.errorMsgAdd("Error", "ConnectFailed", ex.getMessage());
            LOG.log(Level.SEVERE, null, ex);
        } 
    } 

    @Override
    protected MailSettings createSettings() {
        return MailUtils.createSettings(service, conf);
    }

    @Override
    public Integer getSERVICE_ID() {
        return SERVICE_ID;
    }

    @Override
    public BaseTimer getTimerFacade() {
        return mailReaderTimer;
    }
    
    @Override
    public void doRunService(){        
        ServicesEvents selectedEvent = mailReaderTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }

}
