
package com.maxfill.services.mail;

import com.google.gson.Gson;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.DateUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.maxfill.services.mail.MailUtils.serverConnect;

/**
 * Таймер службы MAIL
 * @author Maxim
 */
@Stateless
public class MailTimer extends BaseTimer<MailSettings>{
    
    @EJB
    private MailBoxFacade mailBoxFacade;
    
    @Override
    public ServicesEvents doExecuteTask(Services service, MailSettings settings){
        LOG.log(Level.INFO, "Executing MAIL task!");
        StringBuilder detailInfo = new StringBuilder("");        
        Date startDate = new Date();
        detailInfoAddRow(detailInfo, "The service started in " + DateUtils.dateToString(startDate));

        ServicesEvents selectedEvent = new ServicesEvents();
        selectedEvent.setServiceId(service);
        selectedEvent.setDateStart(startDate);
        selectedEvent.setResult(RESULT_FAIL);
        try {
            List<Mailbox> messages = mailBoxFacade.findAll();
            if (!messages.isEmpty()){
                Authenticator auth = new MailAuth(settings.getUser(), settings.getPassword());
                Session session = serverConnect(settings, auth);
                detailInfoAddRow(detailInfo, "The connection is established...");
                if (session != null){
                    for (Mailbox message : messages){
                        String subject = message.getSubject(); 
                        String content = message.getMsgContent();
                        Gson gson = new Gson();
                        Map<String, String> attachments = gson.fromJson(message.getAttaches(), Map.class); 

                        String from = message.getSender();                
                        String to = message.getAddresses();
                        String copyes = message.getCopies();
                        MailUtils.sendMultiMessage(session, from, to, copyes, content, subject, attachments);
                        detailInfoAddRow(detailInfo, "The message id=" + message.getId() + " is sent!");
                        mailBoxFacade.remove(message);
                    }
                    selectedEvent.setResult(RESULT_SUCCESSFULLY);
                } else {
                    detailInfoAddRow(detailInfo, "Failed to establish connection!");
                }    
            } else {
                detailInfoAddRow(detailInfo, "No message to sent!");
            }    
        } catch(Exception e){
            detailInfoAddRow(detailInfo, e.getMessage());
        } finally{
            Date finishDate = new Date();
            detailInfoAddRow(detailInfo, "The service finished in " + DateUtils.dateToString(finishDate));
            selectedEvent.setDetails(detailInfo.toString());
            selectedEvent.setDateFinish(finishDate);
            servicesEventsFacade.create(selectedEvent);
            service.getServicesEventsList().add(selectedEvent); 
            return selectedEvent;           
        }        
    }    

    @Override
    protected MailSettings restoreSettings(Services service) {
        return (MailSettings) JAXB.unmarshal(new StringReader(service.getSettings()), MailSettings.class);
    }
    
}
