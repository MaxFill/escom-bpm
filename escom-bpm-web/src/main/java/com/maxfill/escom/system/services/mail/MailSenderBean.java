package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.beans.BaseServicesBean;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.MailAuth;
import com.maxfill.services.mail.MailSettings;
import com.maxfill.services.mail.MailSenderTimer;
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

/**
 * Бин для формы "Служба отправки e-mail сообщений
 */
@Named
@ViewScoped
public class MailSenderBean extends BaseServicesBean<MailSettings> {
    private static final long serialVersionUID = 8484737343525831475L;
    private static final Integer SERVICE_ID = 2;
    
    @EJB
    private MailSenderTimer mailSenderTimer;

    //Todo переделать c учётом ssl!
    public void onCheckConnect(){
        String subject = "Mail test from Escom3"; 
        String content = "<h1>Hello!</h1><br/><h2>This is the test message from escom3</h2>";
 
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());
        
        try {
            String adress = getSettings().getAdressSender();
            Session session = MailUtils.sessionSender(getSettings(), auth);
            MailUtils.sendMultiMessage(session, adress, adress, "", content, subject, conf.getEncoding(), new HashMap<>());
            EscomBeanUtils.SuccesFormatMessage("Successfully", "MessageSent",  new Object[]{adress});
        } catch (SecurityException | MessagingException | UnsupportedEncodingException ex) {
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
        return mailSenderTimer;
    }
    
    @Override
    public void doRunService(){        
        ServicesEvents selectedEvent = mailSenderTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }
    
    public void onOpenMailBox(){
        sessionBean.openDialogFrm(DictDlgFrmName.FRM_MAIL_BOX, null);
    }
}
