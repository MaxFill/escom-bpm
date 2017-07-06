package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.beans.BaseServicesBean;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.MailAuth;
import com.maxfill.services.mail.MailSettings;
import com.maxfill.services.mail.MailTimer;
import com.maxfill.services.mail.MailUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.xml.bind.JAXB;
import org.apache.commons.lang3.StringUtils;

@Named
@ViewScoped
public class MailBean extends BaseServicesBean<MailSettings>{
    private static final long serialVersionUID = 8484737343525831475L;
    private static final Integer MAIL_SERVICE_ID = 2;
    
    @EJB
    private MailTimer mailTimer;       
            
    public void onCheckConnect(){
        String subject = "Mail test from Escom3"; 
        String content = "<h1>Hello!</h1><br/><h2>This is the test message from escom3</h2>"; 
 
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());
        
        try {
            String adress = getSettings().getAdressSender();
            Session session = MailUtils.serverConnect(getSettings(), auth, conf.getEncoding());
            MailUtils.sendMultiMessage(session, adress, adress, "", content, subject, conf.getEncoding(), new HashMap<>());
            EscomBeanUtils.SuccesFormatMessage("Successfully", "MessageSent",  new Object[]{adress});

        } catch (SecurityException | MessagingException | UnsupportedEncodingException ex) {
            EscomBeanUtils.ErrorMsgAdd("Error", "ConnectFailed", ex.getMessage());            
            LOG.log(Level.SEVERE, null, ex);
        } 
    } 

    @Override
    protected MailSettings createSettings() {     
        MailSettings settings = null;
        byte[] compressXML = service.getSettings();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                settings = (MailSettings) JAXB.unmarshal(new StringReader(settingsXML), MailSettings.class); 
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } 
        if (settings == null) {
            settings = new MailSettings();
        }
        if (StringUtils.isBlank(settings.getAdressSender())){
            settings.setAdressSender(conf.getDefaultSenderEmail());
        }
        if (StringUtils.isBlank(settings.getServerAdress())){
            settings.setServerAdress(conf.getDefaultEmailServer());
        }
        if (settings.getSmtpPort() == null){
            settings.setSmtpPort(Integer.valueOf(conf.getDefaultEmailServerPort()));
        }
        return settings;
    }

    @Override
    public Integer getSERVICE_ID() {
        return MAIL_SERVICE_ID;
    }

    @Override
    public BaseTimer getTimerFacade() {
        return mailTimer;
    }
    
    @Override
    public void doRunService(){        
        ServicesEvents selectedEvent = mailTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }
    
    public void onOpenMailBox(){
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_MAIL_BOX, null);
    }
}
