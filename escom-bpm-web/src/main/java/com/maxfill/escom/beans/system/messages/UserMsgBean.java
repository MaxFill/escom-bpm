package com.maxfill.escom.beans.system.messages;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.system.lazyload.LazyLoadDialogBean;
import com.maxfill.escom.beans.system.lazyload.LazyLoadModel;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.messages.UserMessages;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ViewScoped
@Named
public class UserMsgBean extends LazyLoadDialogBean{
    private static final long serialVersionUID = -7376087892834532742L;

    private Boolean showOnlyUnread;
    private List<UserMessages> checkedMessages;
    private UserMessages selectedMessages;
    private final LazyLoadModel<Authlog> lazyModel = new LazyLoadModel(null, this);

    @Inject
    private DocBean docBean;
    
    @EJB
    private UserMessagesFacade messagesFacade;

    @Override
    protected BaseLazyLoadFacade getFacade() {
        return messagesFacade;
    }

    @Override
    public LazyLoadModel getLazyDataModel() {
        return lazyModel;
    }

    @Override
    public void onBeforeOpenCard(){
        if (showOnlyUnread == null){
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            showOnlyUnread = params.get("typeMsg").equals("newMsg");
        }
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_USER_MESSAGES;
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
            removeItemFromData(message);
        }
    }
    
    public void onChangeChBoxShowMsgType(){
        refreshData();
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

    @Override
    public List loadItems(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        filters.put("addressee", sessionBean.getCurrentUser());
        if (showOnlyUnread){
            filters.put("dateReading", null);
        }
        return super.loadItems(first, pageSize, sortField, sortOrder, filters);
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

    
}
