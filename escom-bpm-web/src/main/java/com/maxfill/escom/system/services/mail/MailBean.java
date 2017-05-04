package com.maxfill.escom.system.services.mail;

import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.escom.beans.BaseServicesBean;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.services.mail.MailAuth;
import com.maxfill.services.mail.MailSettings;
import com.maxfill.services.mail.MailTimer;
import com.maxfill.services.mail.MailUtils;
import com.maxfill.services.mail.Mailbox;
import static com.maxfill.services.mail.MailUtils.serverConnect;
import com.maxfill.utils.EscomUtils;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @EJB
    private MailBoxFacade mailBoxFacade;
    
    private List<Mailbox> messages = new ArrayList<>();
            
    /**
     * Тест проверки соединения с Почтовым сервером и отправки тестового письма
     */
    public void onCheckConnect(){
        String subject = "Mail test from Escom3"; 
        String content = "<h1>Hello!</h1><br/><h2>This is the test message from escom3</h2>"; 
 
        Authenticator auth = new MailAuth(getSettings().getUser(), getSettings().getPassword());
        
        try {
            String adress = getSettings().getAdressSender();
            Session session = serverConnect(getSettings(), auth);
            MailUtils.sendMultiMessage(session, adress, adress, "", content, subject, new HashMap<>());
            EscomBeanUtils.SuccesFormatMessage("Successfully", "MessageSent",  new Object[]{adress});

        } catch (SecurityException | MessagingException | UnsupportedEncodingException ex) {
            EscomBeanUtils.ErrorMsgAdd("Error", "ConnectFailed", ex.getMessage());            
            LOG.log(Level.SEVERE, null, ex);
        } 
    } 

    @Override
    protected MailSettings createSettings() {     
        MailSettings settings;
        if (StringUtils.isNotBlank(service.getSettings())){
            settings = (MailSettings) JAXB.unmarshal(new StringReader(service.getSettings()), MailSettings.class); 
        } else {
            settings = new MailSettings();
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
    
    /**
     * Ручной запуск службы на выполнение
     */
    @Override
    public void doRunService(){        
        ServicesEvents selectedEvent = mailTimer.doExecuteTask(service, getSettings());
        setSelectedEvent(selectedEvent);
        getServicesFacade().edit(service);        
    }

    /**
     * Сброс буфера сообщений для обновления
     */
    public void refreshMessages(){
        messages = null;
    }
    
    /**
     * Удаление сообщения
     * @param message 
     */
    public void deleteMessage(Mailbox message){
        mailBoxFacade.remove(message);
        messages.remove(message);
    }
    
    public List<Mailbox> getMessages() {
        if (messages == null) {
            messages = mailBoxFacade.findAll();
        }
        return messages;
    }
    public void setMessages(List<Mailbox> messages) {
        this.messages = messages;
    }
    
}
