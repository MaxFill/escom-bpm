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
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

/**
 * Таймер загрузки e-mail сообщений
 */
@Stateless
public class MailReaderTimer extends BaseTimer<MailSettings>{
    @EJB
    private MailBoxFacade mailBoxFacade;
    @EJB
    private DocFacade docFacade;

    @Override
    public ServicesEvents doExecuteTask(Services service, MailSettings settings) {
        LOG.log(Level.INFO, "Executing MAIL READER task!");
        Date startDate = new Date();
        detailInfoAddRow("The service started in " + DateUtils.dateToString(startDate, DateFormat.SHORT, DateFormat.MEDIUM, conf.getServerLocale()));

        ServicesEvents selectedEvent = new ServicesEvents(service);
        selectedEvent.setDateStart(startDate);
        selectedEvent.setResult(RESULT_FAIL);
        try {
            Authenticator auth = new MailAuth(settings.getUser(), settings.getPassword());
            Folder folder = MailUtils.sessionReader(settings, auth);
            detailInfoAddRow("The connection is established...");
            if (folder != null) {
                Arrays.stream(folder.getMessages()).forEach(m -> docFacade.createDocFromEmail(m));
                selectedEvent.setResult(RESULT_SUCCESSFULLY);
            }
        } catch(MessagingException e){
            detailInfoAddRow(e.getMessage());
        } finally{
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
