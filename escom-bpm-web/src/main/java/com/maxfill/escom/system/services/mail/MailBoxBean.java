package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.facade.MailBoxFacade;
import com.maxfill.services.mail.Mailbox;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class MailBoxBean extends BaseDialogBean{    
    private static final long serialVersionUID = -3168942305502913635L;
    
    @EJB
    private MailBoxFacade mailBoxFacade;
        
    private List<Mailbox> messages;
        
    @Override
    protected void initBean() {    
    }
    
    @Override
    public void onOpenCard(){
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_MAIL_BOX;
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