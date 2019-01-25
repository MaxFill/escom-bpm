package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.mail.*;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.logging.Level;

/**
 * Бин для формы "Служба получения e-mail сообщений"
 */
@Named
@ViewScoped
public class MailReaderBean extends BaseServicesBean<MailSettings>{
    private static final long serialVersionUID = 8484737343525831475L;
    
    @EJB
    private MailReaderTimer mailReaderTimer;
    @EJB
    private MailService mailService;

    /**
     * Проверка соединения с почтовым сервером
     */
    public void onCheckConnect(){
        Session session = null;
        Folder inbox = null;
        try {
            session = mailService.getSessionReader(getSettings());
            inbox = mailService.getInbox(session, getSettings());
            int countMsg = inbox.getMessageCount();
            int countUnread = inbox.getUnreadMessageCount();
            String adress = getSettings().getServerAdress();
            MsgUtils.succesFormatMsg("TestMailInbox", new Object[]{adress, countMsg, countUnread});
        } catch (RuntimeException | MessagingException ex) {
            MsgUtils.errorMessage(ex.getMessage());
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            if (session != null){
                try {
                    session.getStore().close();
                    if (inbox != null){
                        inbox.close(true);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    } 

    @Override
    protected MailSettings createSettings() {
        return mailService.createReaderSettings(service, conf);
    }

    @Override
    public int getSERVICE_ID() {
        return DictServices.MAIL_READER_ID;
    }

    @Override
    public BaseTimer getTimerFacade() {
        return mailReaderTimer;
    }
    
    @Override
    public void doRunService(){        
        mailReaderTimer.doExecuteTask(service, getSettings());       
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MAIL_READER_SERVICE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("ServiceReaderEmail");
    }
}