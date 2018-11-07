package com.maxfill.escom.beans.system.messages;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.docs.DocBean;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.beans.docs.DocCardBean;
import com.maxfill.escom.beans.processes.ProcessBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.beans.task.TaskBean;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.core.messages.UserMessages;
import com.maxfill.model.basedict.task.Task;
import com.maxfill.utils.DateUtils;
import com.maxfill.model.basedict.process.Process;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@ViewScoped
@Named
public class UserMsgBean extends LazyLoadBean<UserMessages> {
    private static final long serialVersionUID = -7376087892834532742L;
    
    @Inject
    private DocBean docBean;
    @Inject
    private ProcessBean processBean;
    @Inject
    private TaskBean taskBean;
    
    @EJB
    private UserMessagesFacade messagesFacade;

    private boolean showOnlyUnread = true;
    private UserMessages selectedMessages;
    private Process process;
    private Doc doc;
    
    @Override
    protected void initBean() {
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, - 7);
    }

    @Override
    protected BaseLazyFacade getLazyFacade() {
        return messagesFacade;
    }
    
    @Override
    public void doBeforeOpenCard(Map params){
        if (params.containsKey("typeMsg")){
            showOnlyUnread = "newMsg".equals(params.get("typeMsg"));
        }
        if (sourceBean != null && sourceBean instanceof ProcessCardBean){
            process = ((ProcessCardBean) sourceBean).getEditedItem();
        }
        if (sourceBean != null && sourceBean instanceof DocCardBean){
            doc = ((DocCardBean) sourceBean).getEditedItem();
        }
    }

    @Override
    public String getFormName() {
        return DictFrmName.FRM_USER_MESSAGES;
    }
    
    public boolean isShowRecipient(){
        return process != null;
    }
    
    public boolean isCanMarkRead(UserMessages msg){
        return msg.getDateReading() == null && getCurrentUser().equals(msg.getAddressee());
    }
    
    /**
     * Установка отметки о прочтении на выделенных сообщениях
     */ 
    public void markAsRead(){         
        List<UserMessages> msgs = messagesFacade.findItemsByFilters("", "", makeFilters(new HashMap<>()), getCurrentUser());
        msgs.stream()
                .filter(message->message.getDateReading() == null 
                        && getCurrentUser().equals(message.getAddressee()))
                .forEach(message->markAsRead((UserMessages)message)); 
    }

    /**
     * Установка отметки о прочтении сообщения
     * @param message
     */
    public void markAsRead(UserMessages message){        
        messagesFacade.makeAsRead(message); 
        if (showOnlyUnread) {
            removeItemFromData(message);
        }
    }

    public void markSelectedAsRead(){
        if (CollectionUtils.isEmpty(checkedItems)) return;
        checkedItems.stream()
                .filter(message->message.getDateReading() == null
                    && getCurrentUser().equals(message.getAddressee()))
                .forEach(message->markAsRead((UserMessages)message)); 
    }
    
    /**
     * Обработка события нажатия флага отображения только новых сообщений
     */
    public void onChangeChBoxShowMsgType(){
        refreshLazyData();
    }
    
    public void onSetSelectedMessage(UserMessages message){
        selectedMessages = message;
    }    
    
    @Override
    protected Map<String, Object> makeFilters(Map filters) {        
        if (process != null){
            filters.put("process", process);
        } else 
            if (doc != null && doc.getId() != null){
                filters.put("document", doc);
            } else {
                filters.put("addressee", sessionBean.getCurrentUser());
            }
        if (showOnlyUnread){
            filters.put("dateReading", null);
            filters.remove("dateSent");
        } else {
            if(dateStart != null || dateEnd != null) {
                Map <String, Date> dateFilters = new HashMap <>();
                dateFilters.put("startDate", dateStart);
                dateFilters.put("endDate", dateEnd);
                filters.put("dateSent", dateFilters);
            }
        }
        return filters;
    }

    public void onOpenTask(UserMessages message) {               
        if (message == null || message.getTask() == null) return;
        taskBean.prepEditItem(message.getTask(), getParamsMap());
    }
    
    public void onGoToDocument(UserMessages message){
        if (message == null || message.getDocument() == null) return;             
        docBean.prepEditItem(message.getDocument(), getParamsMap());
    }
    
    public void onGoToProcess(UserMessages message){
        if (message == null || message.getProcess() == null) return;        
        processBean.prepEditItem(message.getProcess(), getParamsMap());
    }
    
    /* GETS & SETS */
    
    public boolean isShowOnlyUnread() {
        return showOnlyUnread;
    }
    public void setShowOnlyUnread(boolean showOnlyUnread) {
        this.showOnlyUnread = showOnlyUnread;
    }

    @Override
    public Task getSourceItem() {
        return selectedMessages.getTask();
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Messages");
    }
    
}
