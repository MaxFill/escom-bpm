package com.maxfill.services.mail;

import com.maxfill.Configuration;
import com.maxfill.services.Services;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang3.StringUtils;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.*;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class MailServiceImpl implements MailService{
    private static final String INBOX = "INBOX";
    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class.getName());

    @Override
    public Folder getInbox(Session session, MailSettings settings){
        return getFolder(session, settings, INBOX);
    }

    /**
     *  Формирует соединение с почтовым сервером для получения сообщений
     */
    @Override
    public Session getSessionReader(MailSettings settings) {
        Authenticator auth = new MailAuth(settings.getUser(), settings.getPassword());
        final Properties props = new Properties();
        props.put("mail.debug", "false");
        props.put("mail.store.protocol", "imaps");
        if(settings.getUseSSL()) {
            props.put("mail.imap.ssl.enable", "true");
        }
        props.put("mail.imap.port", settings.getPort());
        final Session session = Session.getInstance(props, auth);
        if (session == null){
            throw new RuntimeException("Failed to establish connection!");
        }
        session.setDebug(false);
        return session;
    }

    /**
     *  Формирует соединение с почтовым сервером для отправки сообщений
     */
    @Override
    public Session getSessionSender(MailSettings settings) {
        Session session = null;
        try {
            Authenticator auth = new MailAuth(settings.getUser(), settings.getPassword());
            Properties props = System.getProperties();
            props.put("mail.smtp.port", settings.getPort());
            props.put("mail.smtp.host", settings.getServerAdress());
            props.put("mail.smtp.auth", "true");
            props.put("mail.mime.charset", settings.getEncoding());
            if (settings.getUseSSL()) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            session = Session.getInstance(props, auth);
            if (session == null){
                throw new RuntimeException("Failed to establish connection!");
            }
            session.setDebug(false);
        } catch(SecurityException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        return session;
    }

    private Folder getFolder(Session session, MailSettings settings, String folderName){
        Folder folder = null;
        try {
            Store store = session.getStore();
            store.connect(settings.getServerAdress(), settings.getUser(), settings.getPassword());
            folder = store.getFolder(folderName);
            if (folder == null) {
                throw new RuntimeException("Failed to connect to mailbox!");
            }
            folder.open(Folder.READ_WRITE);
        } catch (MessagingException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return folder;
    }

    /**
     * Создание настроек почтовой службы
     */
    @Override
    public MailSettings createSenderSettings(Services service, Configuration conf){
        MailSettings settings = createSettings(service, conf);
        if (StringUtils.isBlank(settings.getServerAdress())){
            settings.setServerAdress(conf.getDefaultSmtpServer());
        }
        if (settings.getPort() == null){
            settings.setPort(Integer.valueOf(conf.getDefaultSmtpPort()));
        }
        return settings;
    }
    @Override
    public MailSettings createReaderSettings(Services service, Configuration conf){
        MailSettings settings = createSettings(service, conf);
        if (StringUtils.isBlank(settings.getServerAdress())){
            settings.setServerAdress(conf.getDefaultImapServer());
        }
        if (settings.getPort() == null){
            settings.setPort(Integer.valueOf(conf.getDefaultIMAPPort()));
        }
        return settings;
    }
    private MailSettings createSettings(Services service, Configuration conf) {
        MailSettings settings = null;
        byte[] compressXML = service.getSettings();
        if (compressXML != null && compressXML.length >0){
            try {
                String settingsXML = EscomUtils.decompress(compressXML);
                settings = (MailSettings) JAXB.unmarshal(new StringReader(settingsXML), MailSettings.class);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        if (settings == null) {
            settings = new MailSettings();
        }
        if (StringUtils.isBlank(settings.getAdressSender())){
            settings.setAdressSender(conf.getDefaultSenderEmail());
        }
        if (settings.getUseSSL() == null){
            settings.setUseSSL(conf.getUseMailSSLConnect());
        }
        if (settings.getEncoding() == null){
            settings.setEncoding(conf.getEncoding());
        }
        return settings;
    }


    /**
     * Отправка сообщения
     */
    public void sendMultiMessage(Session session, String sender, String recipients, String copyes, String content, String subject, String encoding, Map<String,String> attachments) throws MessagingException, UnsupportedEncodingException {
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
}
