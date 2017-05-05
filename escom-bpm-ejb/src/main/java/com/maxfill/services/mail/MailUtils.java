
package com.maxfill.services.mail;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Утилиты для работы с email
 * @author mfilatov
 */
public final class MailUtils {
    private static final Logger LOG = Logger.getLogger(MailUtils.class.getName());
        
    public static void sendMultiMessage(Session session, String sender, String recipients, String copyes, String content, String subject, String encoding, Map<String,String> attachments) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = new MimeMessage(session); 
 
        msg.setFrom(new InternetAddress(sender)); 
        msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients)); 
        msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(copyes));
        msg.setSubject(subject, encoding); 

        BodyPart messageBodyPart = new MimeBodyPart(); 
        messageBodyPart.setContent(content, "text/html; charset=" + encoding + ""); 
        Multipart multipart = new MimeMultipart(); 
        multipart.addBodyPart(messageBodyPart); 
 
        for(Map.Entry<String, String> attachment : attachments.entrySet()){
            MimeBodyPart attachmentBodyPart = new MimeBodyPart(); 
            DataSource source = new FileDataSource(attachment.getValue()); 
            attachmentBodyPart.setDataHandler(new DataHandler(source)); 
            attachmentBodyPart.setFileName(MimeUtility.encodeText(attachment.getKey())); 
            multipart.addBodyPart(attachmentBodyPart); 
        }
        msg.setContent(multipart); 
 
        Transport.send(msg); 
    } 

    /* Устанавливает соединение с почтовым сервером */
    public static Session serverConnect(MailSettings settings, Authenticator auth, String encoding){
        try {
            Properties props = System.getProperties(); 
            props.put("mail.smtp.port", settings.getSmtpPort()); 
            props.put("mail.smtp.host", settings.getServerAdress()); 
            props.put("mail.smtp.auth", "true"); 
            props.put("mail.mime.charset", encoding); 
            return Session.getInstance(props, auth); 
        } catch(SecurityException e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        }
    }
}