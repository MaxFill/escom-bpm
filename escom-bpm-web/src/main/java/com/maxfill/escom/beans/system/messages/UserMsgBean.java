package com.maxfill.escom.beans.system.messages;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.system.lazyload.LazyLoadDialogBean;
import com.maxfill.facade.UserMessagesFacade;
import com.maxfill.facade.base.BaseLazyLoadFacade;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.messages.UserMessages;
import com.maxfill.utils.DateUtils;
import org.primefaces.model.SortOrder;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named
public class UserMsgBean extends LazyLoadDialogBean{
    private static final long serialVersionUID = -7376087892834532742L;

    private Boolean showOnlyUnread;
    private UserMessages selectedMessages;

    @Inject
    private DocBean docBean;
    
    @EJB
    private UserMessagesFacade messagesFacade;

    @Override
    protected void initBean() {
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, - 7);
    }

    @Override
    protected BaseLazyLoadFacade getFacade() {
        return messagesFacade;
    }

    @Override
    protected String getFieldDateCrit() {
        return "dateSent";
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
        if (checkedItemsEmpty()) return;
        checkedItems.stream().forEach(message -> markAsRead((UserMessages)message));
        checkedItems.clear();
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

    /**
     * Обработка события нажатия флага отображения только новых сообщений
     */
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
    protected Map <String, Object> makeFilters(Map filters) {
        super.makeFilters(filters);
        filters.put("addressee", sessionBean.getCurrentUser());
        if (showOnlyUnread){
            filters.put("dateReading", null);
            filters.remove("dateSent");
        }
        return filters;
    }

    /* GETS & SETS */
    
    public boolean isShowOnlyUnread() {
        return showOnlyUnread;
    }
    public void setShowOnlyUnread(boolean showOnlyUnread) {
        this.showOnlyUnread = showOnlyUnread;
    }

    
}
