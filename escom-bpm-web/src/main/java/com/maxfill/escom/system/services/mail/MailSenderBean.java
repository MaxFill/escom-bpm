package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.mail.*;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import org.primefaces.PrimeFaces;

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
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());    
        try {
            String adress = getSettings().getAdressSender();
            Session session = mailService.getSessionSender(getSettings());
            if (session == null){
                MsgUtils.errorMsg("ConnectServerFailed");
                return;
            }
            mailService.sendMultiMessage(session, adress, adress, "", 
                    "<h1>Hello!</h1><br/><h2>This is the test message from escom-bpm.web</h2>", 
                    "Mail test from escom-bpm.web", 
                    conf.getEncoding(), 
                    new HashMap<>());
            MsgUtils.succesFormatMsg("MessageSent",  new Object[]{adress});
        } catch (RuntimeException | MessagingException | UnsupportedEncodingException ex) {
            MsgUtils.errorMsg("ConnectServerFailed");
            LOG.log(Level.SEVERE, null, ex);
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
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
        mailSenderTimer.doExecuteTask(service, getSettings());
    }
    
    public void onOpenMailBox(){
        sessionBean.openDialogFrm(DictFrmName.FRM_MAIL_BOX, getParamsMap());
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MAIL_SENDER_SERVICE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("MailService");
    }
}
