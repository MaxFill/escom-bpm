package com.maxfill.services.mail;

import com.google.gson.Gson;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.mail.MessagingException;

/**
 * Таймер отправки e-mail сообщений
 */
@Stateless
public class MailSenderTimer extends BaseTimer<MailSettings>{
    
    @EJB
    private MailBoxFacade mailBoxFacade;
    @EJB
    private MailService mailService;

    public MailSenderTimer() {
        super(MailSettings.class);
    }

    @Override
    public ServicesEvents doExecuteTask(Services service, MailSettings settings){        
        ServicesEvents selectedEvent = startAction(service);        
        try {
            List<Mailbox> messages = mailBoxFacade.findAll();
            if (!messages.isEmpty()){
                Session session = mailService.getSessionSender(settings);
                detailInfoAddRow("The connection is established...");
                if (session != null){
                    for (Mailbox message : messages){
                        String subject = message.getSubject(); 
                        
                        byte[] compressXML = message.getMsgContent();
                        String content = EscomUtils.decompress(compressXML);
                        
                        Map<String, String> attachments = prepareAttaches(message.getAttaches());                       
                        
                        String from = message.getSender();                
                        String to = message.getAddresses();
                        String copyes = message.getCopies();
                        String encoding = conf.getEncoding();
                        mailService.sendMultiMessage(session, from, to, copyes, content, subject, encoding, attachments);
                        detailInfoAddRow("The message id=" + message.getId() + " is sent!");
                        mailBoxFacade.remove(message);
                    }
                    selectedEvent.setResult(RESULT_SUCCESSFULLY);
                } else {
                    detailInfoAddRow("Failed to establish connection!");
                }    
            } else {
                detailInfoAddRow("No message to sent!");
                selectedEvent.setResult(RESULT_SUCCESSFULLY);
            }    
        } catch(RuntimeException | IOException | MessagingException e){
            detailInfoAddRow(e.getMessage());
        } finally{
            finalAction(selectedEvent);
            service.getServicesEventsList().add(selectedEvent);             
            return selectedEvent;
        }     
    }

    private Map<String, String> prepareAttaches(byte[] compressXML){
        Map<String, String> attachments = new HashMap<>(); 
        if (compressXML != null && compressXML.length >0){
            String path = conf.getUploadPath();
            Gson gson = new Gson();
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                attachments = gson.fromJson(settingsXML, Map.class);
                Set<Entry<String, String>> attachesSet = attachments.entrySet();
                for (Entry<String, String> entry : attachesSet){
                    String fileName = path + entry.getValue();
                    entry.setValue(fileName);
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } 
        return attachments;
    }    
    
}