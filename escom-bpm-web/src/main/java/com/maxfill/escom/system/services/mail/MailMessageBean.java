package com.maxfill.escom.system.services.mail;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.mail.Mailbox;
import com.google.gson.Gson;
import com.maxfill.Configuration;
import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.docs.Doc;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.partners.Partner;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;

/* Контролер формы "E-mail сообщение" */

@Named
@ViewScoped
public class MailMessageBean extends BaseDialogBean{    
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
    @EJB
    private Configuration configuration;
    
    private final Mailbox selected = new Mailbox();
    private Map<String, String> attaches = null;
    private String modeSentAttache = null;
    private String content;

    @Override
    protected void initBean() {
    }

    @Override
    public void onBeforeOpenCard(){
        if (modeSentAttache != null) return;
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        modeSentAttache = params.get("modeSendAttache");        
        List<String> strDocIds = EscomUtils.SplitString(params.get("docIds"), ",");
                
        List<Integer> docIds = strDocIds.stream()
                .map(NumberUtils::toInt)
                .collect(Collectors.toList());
        
        List<Doc> sendDocs = new ArrayList<>();
        sendDocs.addAll(docFacade.findByIds(docIds));
        prepareMessage(sendDocs);
    }
    
    private void prepareMessage(List<Doc> sendDocs){
        String senderEmail = sessionBean.getCurrentUser().getEmail();
        if (StringUtils.isBlank(senderEmail)){
            senderEmail = configuration.getDefaultSenderEmail();
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
                    links = prepareDocCardLinks(doc);                    
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
            sb.append(EscomMsgUtils.getBandleLabel("Newsletter"));
        }        
        return sb.toString();
    }
    
    private StringBuilder prepareDocLinks(Doc doc){        
        StringBuilder links = new StringBuilder();
        String urlDownLoad = EscomBeanUtils.doGetItemURL(doc, "docs/document");
        links.append(EscomMsgUtils.getBandleLabel("LinkForDownloadDocument")).append(": ");
        links.append("<a href=").append(urlDownLoad).append(">").append(FilenameUtils.removeExtension(doc.getFullName())).append("</a>");
        links.append("<br />");
        String urlViewDoc = EscomBeanUtils.doGetItemURL(doc, "docs/doc-viewer");
        links.append(EscomMsgUtils.getBandleLabel("LinkForViewDocumentInProgram")).append(": ");
        links.append("<a href=").append(urlViewDoc).append(">").append(FilenameUtils.removeExtension(doc.getFullName())).append("</a>");
        links.append("<br />");
        return links;
    }
    
    private StringBuilder prepareDocCardLinks(Doc doc){
        StringBuilder links = new StringBuilder();
        String urlDownLoad = EscomBeanUtils.doGetItemURL(doc, "docs/doc-card");
        links.append(EscomMsgUtils.getBandleLabel("LinkForDownloadDocument")).append(": ");
        links.append("<a href=").append(urlDownLoad).append("&openMode=0").append(">").append(FilenameUtils.removeExtension(doc.getFullName())).append("</a>");
        links.append("<br />");
        return links;
    }
    
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
            return onCloseCard();
        } else {
            EscomMsgUtils.errorMsg("IncorrectMailAdress");
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

    /**
     * Обработка события изменения сообщения
     * @param event
     */
    public void onMessageChange(ValueChangeEvent event){
        onItemChange();
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
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_MAIL_MESSAGE;
    }
    
}