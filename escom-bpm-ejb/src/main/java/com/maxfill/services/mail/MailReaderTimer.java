package com.maxfill.services.mail;

import com.maxfill.facade.DocFacade;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.BaseTimer;
import com.maxfill.services.Services;
import com.maxfill.services.common.history.ServicesEvents;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.search.FlagTerm;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.CollectionCertStoreParameters;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Таймер загрузки e-mail сообщений
 */
@Stateless
public class MailReaderTimer extends BaseTimer<MailSettings>{
    @EJB
    private MailBoxFacade mailBoxFacade;
    @EJB
    private DocFacade docFacade;
    @EJB
    private MailService mailService;

    @Override
    public ServicesEvents doExecuteTask(Services service, MailSettings settings) {
        LOG.log(Level.INFO, "Executing MAIL READER task!");
        Date startDate = new Date();
        detailInfoAddRow("The service started in " + DateUtils.dateToString(startDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale()));

        ServicesEvents selectedEvent = new ServicesEvents(service);
        selectedEvent.setDateStart(startDate);
        selectedEvent.setResult(RESULT_FAIL);
        Session session = null;
        Folder inbox = null;
        try {
            session = mailService.getSessionReader(settings);
            inbox = mailService.getInbox(session, settings);
            detailInfoAddRow("The connection is established...");

            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] messages = inbox.search(unseenFlagTerm);  //получаем только непрочитанные сообщения

            detailInfoAddRow("Mailbox contains " + messages.length + " messages");

            if (messages.length > 0) {
                List <Message> processed = Arrays.stream(messages)
                        .filter(m -> docFacade.createDocFromEmail(m, getDetailInfo(), settings))
                        .collect(Collectors.toList());

                detailInfoAddRow("All messages read!");

                if(settings.getDeleteAfterLoad()) {
                    Flags deleted = new Flags(Flags.Flag.DELETED);
                    Message[] forDelete = processed.toArray(new Message[processed.size()]);
                    inbox.setFlags(forDelete, deleted, true);
                    detailInfoAddRow("Delete " + forDelete.length + " messages.");
                } else {
                    detailInfoAddRow("No deleted messages.");
                }
            }

            selectedEvent.setResult(RESULT_SUCCESSFULLY);
        } catch(RuntimeException | MessagingException e){
            detailInfoAddRow(e.getMessage());
        } finally{
            if (session != null) {
                try {
                    session.getStore().close();
                    if (inbox != null) {
                        inbox.close(true);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            finalAction(selectedEvent);
            service.getServicesEventsList().add(selectedEvent);
            return selectedEvent;
        }
    }

    @Override
    protected MailSettings restoreSettings(Services service) {
        MailSettings mailSettings = null;
        try {
            byte[] compressXML = service.getSheduler();
            String settingsXML = EscomUtils.decompress(compressXML);
            mailSettings = JAXB.unmarshal(new StringReader(settingsXML), MailSettings.class);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return mailSettings;
    }
}
