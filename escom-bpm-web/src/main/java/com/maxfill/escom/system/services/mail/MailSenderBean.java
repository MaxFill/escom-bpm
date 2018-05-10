package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * Бин для формы "Служба отправки e-mail сообщений
 */
@Named
@ViewScoped
public class MailSenderBean extends BaseServicesBean<MailSettings> {
    private static final long serialVersionUID = 8484737343525831475L;    
    
    @EJB
    private MailSenderTimer mailSenderTimer;
    @EJB
    private MailService mailService;


    public void onCheckConnect(){
        String subject = "Mail test from Escom3"; 
        String content = "<h1>Hello!</h1><br/><h2>This is the test message from escom3</h2>";
 
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());
        
        try {
            String adress = getSettings().getAdressSender();
            Session session = mailService.getSessionSender(getSettings());
            mailService.sendMultiMessage(session, adress, adress, "", content, subject, conf.getEncoding(), new HashMap<>());
            EscomMsgUtils.succesFormatMsg("MessageSent",  new Object[]{adress});
        } catch (RuntimeException | MessagingException | UnsupportedEncodingException ex) {
            EscomMsgUtils.errorMessage(ex.getMessage());
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected MailSettings createSettings() {
        return mailService.createSenderSettings(service, conf);
    }

    @Override
    public int getSERVICE_ID() {
        return DictServices.MAIL_SENDER_ID;
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

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_MAIL_SENDER_SERVICE;
    }
}
