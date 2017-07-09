package com.maxfill.services.mail;

import com.google.gson.Gson;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;

@Stateless
public class MailTimer extends BaseTimer<MailSettings>{
    
    @EJB
    private MailBoxFacade mailBoxFacade;
    
    @Override
    public ServicesEvents doExecuteTask(Services service, MailSettings settings){
        LOG.log(Level.INFO, "Executing MAIL task!");
        StringBuilder detailInfo = new StringBuilder("");        
        Date startDate = new Date();
        detailInfoAddRow(detailInfo, "The service started in " + DateUtils.dateToString(startDate, ""));

        ServicesEvents selectedEvent = new ServicesEvents();
        selectedEvent.setServiceId(service);
        selectedEvent.setDateStart(startDate);
        selectedEvent.setResult(RESULT_FAIL);
        try {
            List<Mailbox> messages = mailBoxFacade.findAll();
            if (!messages.isEmpty()){
                Authenticator auth = new MailAuth(settings.getUser(), settings.getPassword());
                Session session = MailUtils.serverConnect(settings, auth, conf.getEncoding());
                detailInfoAddRow(detailInfo, "The connection is established...");
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
                        MailUtils.sendMultiMessage(session, from, to, copyes, content, subject, encoding, attachments);
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
        } catch(IOException | MessagingException e){
            detailInfoAddRow(detailInfo, e.getMessage());
        } finally{
            Date finishDate = new Date();
            detailInfoAddRow(detailInfo, "The service finished in " + DateUtils.dateToString(finishDate, ""));
            selectedEvent.setDetails(detailInfo.toString());
            selectedEvent.setDateFinish(finishDate);
            servicesEventsFacade.create(selectedEvent);
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
    
    @Override
    protected MailSettings restoreSettings(Services service) {
        MailSettings mailSettings = null;
        try {
            byte[] compressXML = service.getSheduler();
            String settingsXML = EscomUtils.decompress(compressXML);
            mailSettings = (MailSettings) JAXB.unmarshal(new StringReader(settingsXML), MailSettings.class);
        } catch (IOException ex) {
            Logger.getLogger(MailTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mailSettings;
    }
    
}