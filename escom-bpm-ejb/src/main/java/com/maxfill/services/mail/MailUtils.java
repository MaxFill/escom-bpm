package com.maxfill.services.mail;

import com.maxfill.Configuration;
import com.maxfill.services.Services;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
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
import javax.mail.internet.*;
import javax.xml.bind.JAXB;

/**
 * Утилиты для работы с email
 */
public final class MailUtils {
    private static final Logger LOG = Logger.getLogger(MailUtils.class.getName());

    /**
     * Отправка сообщения
     */
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

    /**
     * Устанавливает соединение с почтовым сервером для отправки сообщений
     */
    public static Session sessionSender(MailSettings settings, Authenticator auth){
        try {
            Properties props = System.getProperties(); 
            props.put("mail.smtp.port", settings.getPort());
            props.put("mail.smtp.host", settings.getServerAdress()); 
            props.put("mail.smtp.auth", "true"); 
            props.put("mail.mime.charset", settings.getEncoding());
            if (settings.getUseSSL()) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            Session session = Session.getInstance(props, auth);
            session.setDebug(false);
            return session;
        } catch(SecurityException e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        }
    }

    /**
     *  Устанавливает соединение с почтовым сервером для получения сообщений
     */
    public static Folder sessionReader(MailSettings settings, Authenticator auth) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.debug", "false");
        props.put("mail.store.protocol", "imaps");
        if(settings.getUseSSL()) {
            props.put("mail.imap.ssl.enable", "true");
        }
        props.put("mail.imap.port", settings.getPort());

        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(false);
        Store store = session.getStore();
        store.connect(settings.getServerAdress(), settings.getUser(), settings.getPassword());
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    /**
     * Создание настроек почтовой службы
     */
    public static MailSettings createSettings(Services service, Configuration conf) {
        MailSettings settings = null;
        byte[] compressXML = service.getSettings();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                settings = (MailSettings) JAXB.unmarshal(new StringReader(settingsXML), MailSettings.class);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        if (settings == null) {
            settings = new MailSettings();
        }
        if (StringUtils.isBlank(settings.getAdressSender())){
            settings.setAdressSender(conf.getDefaultSenderEmail());
        }
        if (StringUtils.isBlank(settings.getServerAdress())){
            settings.setServerAdress(conf.getDefaultSmtpServer());
        }
        if (settings.getPort() == null){
            settings.setPort(Integer.valueOf(conf.getDefaultEmailServerPort()));
        }
        if (settings.getUseSSL() == null){
            settings.setUseSSL(conf.getUseMailSSLConnect());
        }
        if (settings.getEncoding() == null){
            settings.setEncoding(conf.getEncoding());
        }
        return settings;
    }
}