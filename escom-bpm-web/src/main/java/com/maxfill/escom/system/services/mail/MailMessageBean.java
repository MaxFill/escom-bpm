package com.maxfill.escom.system.services.mail;

import com.google.gson.Gson;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.services.mail.Mailbox;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/* Контролер формы "E-mail сообщение" */

@Named
@ViewScoped
public class MailMessageBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 9011875090040784420L;
    private static final Logger LOG = Logger.getLogger(MailMessageBean.class.getName());
    private static final String ADRESS_SEPARATOR = ",";
    
    private static final String MODE_SEND_ATTACHE = "asAttache";
    private static final String MODE_SEND_ATTACHE_PDF = "asAttachePDF";
    private static final String MODE_SEND_LINK_PDF = "asLinkPDF";
    private static final String MODE_SEND_LINK_CARD = "asLinkCard";
        
    @EJB
    private MailBoxFacade mailBoxFacade;
    @EJB
    private DocFacade docFacade;

    private final Mailbox selected = new Mailbox();
    private Map<String, String> attaches = null;
    private String modeSentAttache = null;
    private String content;

    @Override
    public void doBeforeOpenCard(Map<String,String> params){
        if (modeSentAttache != null) return;        
        modeSentAttache = params.get("modeSendAttache");        
        List<String> strDocIds = EscomUtils.SplitString(params.get("docIds"), ",");
                
        List<Integer> docIds = strDocIds.stream()
                .map(NumberUtils::toInt)
                .collect(Collectors.toList());
        
        List<Doc> sendDocs = docFacade.findByIds(docIds, getCurrentUser());
        prepareMessage(sendDocs);
    }
    
    private void prepareMessage(List<Doc> sendDocs){
        String senderEmail = sessionBean.getCurrentUser().getEmail();
        if (StringUtils.isBlank(senderEmail)){
            senderEmail = conf.getDefaultSenderEmail();
        }
        selected.setActual(true);
        selected.setSender(senderEmail);
        selected.setAuthor(sessionBean.getCurrentUser());
        selected.setDateCreate(new Date());
        selected.setSubject(makeSubject(sendDocs));
       
        StringBuilder adresses  = new StringBuilder();
        StringBuilder copies    = new StringBuilder();
        StringBuilder links     = new StringBuilder();
        List<String> addBufer = new ArrayList<>();
        for (Doc doc : sendDocs){
            Partner partner = doc.getPartner();
            if (partner != null && StringUtils.isNotBlank(partner.getEmail())){
                String adress = partner.getEmail();
                if (!addBufer.contains(adress)){
                    addBufer.add(adress);
                    adresses.append(adress);
                    adresses.append(ADRESS_SEPARATOR);
                }
            }            
            
            switch(modeSentAttache){
                case MODE_SEND_ATTACHE : {
                    prepareAttaches(doc.getMainAttache());
                    break;
                }
                case MODE_SEND_LINK_PDF : {
                    links = prepareDocLinks(doc);                    
                    break;
                }
                case MODE_SEND_LINK_CARD : {
                    links = new StringBuilder(docFacade.getItemHREF(doc)); 
                    break;
                }
                case MODE_SEND_ATTACHE_PDF : {
                    prepareAttachesAsPDF(doc.getMainAttache());
                    break;
                }
            }
        }
        links.append("<br/>");
        
        String emailSign = sessionBean.getRefreshCurrentUser().getEmailSign();
        if (StringUtils.isNotBlank(emailSign)){
            links.append(emailSign);
        }
        try {
            content = links.toString();
            byte[] compressXML = EscomUtils.compress(content);
            selected.setMsgContent(compressXML);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        selected.setAddresses(adresses.toString());
        selected.setCopies(copies.toString());
    }
    
    private void prepareAttaches(Attaches attache){
        if (attaches == null){
            attaches= new HashMap<>();
        }
        if (attache == null) return;
        attaches.put(attache.getName(), attache.getFullName());
    }
    
    private void prepareAttachesAsPDF(Attaches attache){
        if (attaches == null){
            attaches= new HashMap<>();
        }
        attaches.put(attache.getNamePDF(), attache.getFullNamePDF());
    }
        
    private String makeSubject(List<Doc> sendDocs){
        StringBuilder sb = new StringBuilder();
        if (sendDocs.size() == 1){
            String fileName = sendDocs.get(0).getName();
            sb.append(fileName);
        } else {
            sb.append(MsgUtils.getBandleLabel("Newsletter"));
        }        
        return sb.toString();
    }
    
    private StringBuilder prepareDocLinks(Doc doc){        
        StringBuilder links = new StringBuilder();
        //ссылка для скачивания оригинала
        String urlDownLoad = sessionBean.doGetItemURL(doc, "/docs/document.xhtml");
        links.append(MsgUtils.getBandleLabel("LinkForDownloadDocument")).append(": ");
        links.append("<a href=").append(urlDownLoad).append(">").append(FilenameUtils.removeExtension(doc.getFullName())).append("</a>");
        links.append("<br />");
        //ссылка для открытия на просмотр
        String urlViewDoc = sessionBean.doGetItemURL(doc, "/docs/doc-viewer.xhtml");
        links.append(MsgUtils.getBandleLabel("LinkForViewDocumentInProgram")).append(": ");
        links.append("<a href=").append(urlViewDoc).append(">").append(FilenameUtils.removeExtension(doc.getFullName())).append("</a>");
        links.append("<br />");
        return links;
    }    

    /**
     * Отправка сообщения
     * @return
     */
    public String sendMail(){        
        if (checkAdresses()){
            Gson gson = new Gson();            
            if (attaches != null){
                try {                    
                    selected.setMsgContent(EscomUtils.compress(content));
                    String attacheJson = gson.toJson(attaches);                    
                    selected.setAttaches(EscomUtils.compress(attacheJson));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }    
            mailBoxFacade.create(selected);
            String message = MessageFormat.format(MsgUtils.getMessageLabel("MessageSaveAndPutInMailBoxService"), new Object[]{selected.getAddresses()});
            return onCloseCard(message);
        } else {
            MsgUtils.errorMsg("IncorrectMailAdress");
            return "";
        }
    }
        
    private boolean checkAdresses(){
        List<String> adresses = EscomUtils.SplitString(selected.getAddresses(), ADRESS_SEPARATOR);
        for (String adress : adresses){
            try {
                InternetAddress internetAddress = new InternetAddress(adress);
            } catch (AddressException ex) {
                LOG.log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public void removeAttache(String fileName){
        attaches.remove(fileName);
    }
    
    public Mailbox getSelected() {
        return selected;
    }
        
    public Set<String> getAttaches() {
        if (attaches == null) return null;
        return attaches.keySet();
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MAIL_MESSAGE;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("MailMessage");
    }
}