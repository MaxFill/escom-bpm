package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.dictionary.DictServices;
import com.maxfill.escom.system.services.BaseServicesBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.*;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
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
            String adress = getSettings().getAdressSender();
            EscomMsgUtils.succesFormatMsg("TestMailInbox", new Object[]{adress, countMsg, countUnread});
        } catch (RuntimeException | MessagingException ex) {
            EscomMsgUtils.errorMessage(ex.getMessage());
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
        ServicesEvents selectedEvent = mailReaderTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_MAIL_READER_SERVICE;
    }

}