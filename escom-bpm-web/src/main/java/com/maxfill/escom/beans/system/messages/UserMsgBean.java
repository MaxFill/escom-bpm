package com.maxfill.escom.beans.system.messages;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.messages.UserMessages;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ViewScoped
@Named
public class UserMsgBean extends BaseDialogBean{
    private static final long serialVersionUID = -7376087892834532742L;

    private boolean showOnlyUnread = true;
    private List<UserMessages> messages;        
    private List<UserMessages> checkedMessages;
    private UserMessages selectedMessages;
     
    @Inject
    private DocBean docBean;
    
    @EJB
    private UserMessagesFacade messagesFacade;
            
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
        return DictDlgFrmName.FRM_USER_MESSAGES;
    }
    
    public void refresh(){
        messages = null;
    }
    
    /* установка отметки о прочтении на выделенных сообщениях */
    public void markAsRead(){
        if (checkedMessages == null || checkedMessages.isEmpty()) return;
        checkedMessages.stream().forEach(message -> markAsRead(message));
        checkedMessages.clear();
    }

    /**
     * Установка отметки о прочтении сообщения
     * @param message
     */
    public void markAsRead(UserMessages message){
        if (message.getDateReading() == null) {
            message.setDateReading(new Date());
            messagesFacade.edit(message);
        }
        if (showOnlyUnread) {
            messages.remove(message);
        }
    }
    
    public void onChangeChBoxShowMsgType(){
        messages = null;
    }
    
    public void onSetSelectedMessage(UserMessages message){
        selectedMessages = message;
    }
    
    public void onGoToDocument(){
        if (selectedMessages == null) return;
        Doc doc = selectedMessages.getDocument();
        if (doc == null) return;
        docBean.prepEditItem(doc);
    }
    
    /* GETS & SETS */

    public List<UserMessages> getCheckedMessages() {
        return checkedMessages;
    }
    public void setCheckedMessages(List<UserMessages> checkedMessages) {
        this.checkedMessages = checkedMessages;
    }
    
    public boolean isShowOnlyUnread() {
        return showOnlyUnread;
    }
    public void setShowOnlyUnread(boolean showOnlyUnread) {
        this.showOnlyUnread = showOnlyUnread;
    }

    public List<UserMessages> getMessages() {
        if (messages == null){
            if (showOnlyUnread){
                messages = messagesFacade.findUnReadMessageByUser(sessionBean.getCurrentUser());
            } else {
                messages = messagesFacade.findMessageByUser(sessionBean.getCurrentUser());
            }    
        }
        return messages;
    }
    public void setMessages(List<UserMessages> messages) {
        this.messages = messages;
    }
    
}
