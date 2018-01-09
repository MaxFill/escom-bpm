package com.maxfill.services.mail;

import com.maxfill.Configuration;
import com.maxfill.services.Services;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface MailService{

    Folder getInbox(Session session, MailSettings settings) throws MessagingException;
    Session getSessionReader(MailSettings settings);
    Session getSessionSender(MailSettings settings);

    MailSettings createSenderSettings(Services service, Configuration conf);
    MailSettings createReaderSettings(Services service, Configuration conf);
    void sendMultiMessage(Session session, String sender, String recipients, String copyes, String content, String subject, String encoding, Map<String,String> attachments) throws MessagingException, UnsupportedEncodingException;
}