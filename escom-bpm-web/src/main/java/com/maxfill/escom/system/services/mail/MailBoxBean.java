package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.services.mail.Mailbox;

import java.util.List;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class MailBoxBean extends BaseViewBean{
    private static final long serialVersionUID = -3168942305502913635L;
    
    @EJB
    private MailBoxFacade mailBoxFacade;
        
    private List<Mailbox> messages;

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MAIL_BOX;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("MessagesForSent");
    }
    
    public void refreshMessages(){
        messages = null;
    }
            
    public void deleteMessage(Mailbox message){
        mailBoxFacade.remove(message);
        messages.remove(message);
    }    
        
    public List<Mailbox> getMessages() {
        if (messages == null) {
            messages = mailBoxFacade.findAll();
        }
        return messages;
    }
    public void setMessages(List<Mailbox> messages) {
        this.messages = messages;
    }
}