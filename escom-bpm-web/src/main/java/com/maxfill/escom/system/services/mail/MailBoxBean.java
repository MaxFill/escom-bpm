
package com.maxfill.escom.system.services.mail;

import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.mail.Mailbox;
import com.google.gson.Gson;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.docs.Doc;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.partners.Partner;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.model.layout.LayoutOptions;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author mfilatov
 */
@Named
@ViewScoped
public class MailBoxBean implements Serializable{    
    private static final long serialVersionUID = 9011875090040784420L;
    private static final Logger LOGGER = Logger.getLogger(MailBoxBean.class.getName());
    private static final String ADRESS_SEPARATOR = ",";
    private static final String ATTACHE_MODE = "asAttache";
    private static final String LINK_MODE = "asLink";
    private final LayoutOptions layoutOptions = new LayoutOptions(); 
    private static final String DEFAULT_SENDER = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("DefSenderEmail");
        
    @Inject
    private SessionBean workPlaceBean;
    @EJB
    private MailBoxFacade mailBoxFacade;
    @EJB
    private DocFacade docFacade;
    
    private Mailbox selected;
    private List<Attaches> attaches = new ArrayList<>();
    private final List<Doc> sendDocs = new ArrayList<>();
    private String modeSentAttache = LINK_MODE;
        
    @PostConstruct
    public void init(){
        String senderEmail = workPlaceBean.getCurrentUser().getEmail();
        if (StringUtils.isBlank(senderEmail)){
            senderEmail = DEFAULT_SENDER;
        }
        selected = new Mailbox();
        selected.setActual(true);
        selected.setSender(senderEmail);
        selected.setAuthor(workPlaceBean.getCurrentUser());
        selected.setDateCreate(new Date());
        selected.setSubject(EscomBeanUtils.getBandleLabel("Newsletter"));
        prepareMessage();
        initLayoutOptions(); 
    }
    
    public void onOpenForm(){
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        modeSentAttache = params.get("modeSendAttache");        
        List<String> strDocIds = EscomUtils.SplitString(params.get("docIds"), ",");
                
        List<Integer> docIds = strDocIds.stream()
                .map(NumberUtils::toInt)
                .collect(Collectors.toList());
        
        sendDocs.addAll(docFacade.findByIds(docIds));
        
        //docIds.stream().map(docId -> docFacade.find(docId)).forEach(doc -> sendDocs.add(doc));
    }
    
    /**
     * Формирование сообщения
     */
    private void prepareMessage(){
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
            Staff manager = doc.getManager();
            if (manager != null && StringUtils.isNotBlank(manager.getEmail())){  
                String adress = manager.getEmail();      
                if (!addBufer.contains(adress)){
                    addBufer.add(adress);
                    copies.append(adress);
                    copies.append(ADRESS_SEPARATOR);
                }
            }
            
            switch(modeSentAttache){
                case ATTACHE_MODE : {
                    attaches.add(doc.getAttache());
                    break;
                }
                case LINK_MODE : {
                    links = prepareDocLinks(doc);                    
                    break;
                }
            }
        }
        links.append("<br/>");
        String emailSign = workPlaceBean.getRefreshCurrentUser().getEmailSign();
        if (StringUtils.isNotBlank(emailSign)){
            links.append(emailSign);
        }
        selected.setMsgContent(links.toString());
        selected.setAddresses(adresses.toString());
        selected.setCopies(copies.toString());
    }
    
    /**
     * Формирование ссылок на документы
     * @param doc
     * @return 
     */
    private StringBuilder prepareDocLinks(Doc doc){        
        StringBuilder links = new StringBuilder();
        String url = EscomBeanUtils.doGetItemURL(doc, "docs/document", "0");
        links.append(EscomBeanUtils.getBandleLabel("Document")).append(": ");
        links.append("<a href=").append(url).append(">").append(doc.getFullName()).append("</a>");
        links.append("<br />");
        return links;
    }
    
    public MailBoxBean() {
    }

    /**
     * Отправка сообщения
     */
    public void sendMail(){        
        if (checkAdresses()){
            Gson gson = new Gson();
            Map<String, String> attacheMap = new HashMap<>();
            //TODO переписать в потоковую операцию создания новой коллекции
            for(Attaches attache : attaches){
                attacheMap.put(attache.getName(), attache.getFullName());
            }
            String attacheJson = gson.toJson(attacheMap);
            selected.setAttaches(attacheJson);
            mailBoxFacade.create(selected);
            RequestContext.getCurrentInstance().closeDialog(null);
        } else {
            EscomBeanUtils.ErrorMsgAdd("Error", "IncorrectMailAdress", "");
        }
    }
    
    /**
     * Отмена сообщения
     */
    public void cancel(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    /**
     * Проверка e-mail адресов
     * @return 
     */
    private boolean checkAdresses(){
        List<String> adresses = EscomUtils.SplitString(selected.getAddresses(), ADRESS_SEPARATOR);
        for (String adress : adresses){
            try {
                InternetAddress internetAddress = new InternetAddress(adress);
            } catch (AddressException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Удаление вложения из сообщения
     * @param attache 
     */
    public void removeAttache(Attaches attache){
        attaches.remove(attache);
    }
    
    public Mailbox getSelected() {
        return selected;
    }

    public void setSelected(Mailbox selected) {
        this.selected = selected;
    }
    
    private void initLayoutOptions() {
        LayoutOptions panes = new LayoutOptions();
        panes.addOption("slidable", false);
        panes.addOption("resizable", true);
        layoutOptions.setPanesOptions(panes);

        LayoutOptions south = new LayoutOptions();
        south.addOption("resizable", false);
        south.addOption("closable", false);
        south.addOption("size", 39);
        layoutOptions.setSouthOptions(south);

        LayoutOptions west = new LayoutOptions();
        west.addOption("size", 100);
        west.addOption("minSize", 100);
        west.addOption("maxSize", 350);
        layoutOptions.setWestOptions(west);
        
        LayoutOptions east = new LayoutOptions();
        east.addOption("size", 300);
        east.addOption("minSize", 150);
        east.addOption("maxSize", 450);
        layoutOptions.setEastOptions(east);

        LayoutOptions center = new LayoutOptions();
        center.addOption("resizable", true);
        center.addOption("closable", false);
        center.addOption("size", 800);
        center.addOption("minWidth", 300);
        center.addOption("minHeight", 300);
        layoutOptions.setCenterOptions(center);       
    }
    
    public LayoutOptions getLayoutOptions() {
        return layoutOptions;
    }

    public List<Attaches> getAttaches() {
        return attaches;
    }

    public void setAttaches(List<Attaches> attaches) {
        this.attaches = attaches;
    }
    
}
