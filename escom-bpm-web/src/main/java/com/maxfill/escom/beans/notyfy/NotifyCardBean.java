package com.maxfill.escom.beans.notyfy;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.process.Process;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.messages.UserMessagesFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.process.remarks.Remark;
import com.maxfill.model.process.remarks.RemarkFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.UserFacade;
import com.maxfill.utils.ItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DualListModel;

/**
 * Контролер карточки уведомления
 * @author maksim
 */
@Named
@ViewScoped
public class NotifyCardBean extends BaseViewBean{
    private static final long serialVersionUID = 459868635296369432L;

    @EJB
    private RemarkFacade remarkFacade;
    @EJB
    private ProcessFacade processFacade;
    @EJB
    private UserMessagesFacade messagesFacade;
    @EJB
    private UserFacade userFacade;
        
    private DualListModel<User> users;     
    private String content;
    private String message;
    private Process process;
    private Remark editedRemark;
    
    @Override
    public void doBeforeOpenCard(Map params){        
        List<User> source = new ArrayList<>();
        if (params.containsKey("remarkID")){
            Integer remarkID = Integer.valueOf((String)params.get("remarkID"));
            editedRemark = remarkFacade.find(remarkID);
            if (editedRemark != null){
                content = editedRemark.getContent();
                process = editedRemark.getProcess();
                if (process != null){
                    source.addAll(processFacade.actualiseRole(process, DictRoles.ROLE_CONCORDER, getCurrentUser()));
                }
            }
        }
        message = MsgUtils.getMessageLabel("PleasePayAttentionMyRemark");
        users = new DualListModel<>(source, new ArrayList<>());
    }
    
    /**
     * Отправка уведомлений
     */
    public void onSend(){
        List<User> recipients = users.getTarget();
        if (recipients.isEmpty()) return;
        List<BaseDict> links = new ArrayList<>();
        if (process != null){
            links.add(process);         
        }
        links.add(editedRemark.getOwner());
        StringBuilder cb = new StringBuilder();
        cb.append("<h3>").append(message).append("<h3/>");
        cb.append("<br/>");
        cb.append(content);
        cb.append("<br/>");
        User sender = getCurrentUser();
        recipients.forEach(recipient -> { 
                    Locale locale = userFacade.getUserLocale(recipient);
                    StringBuilder subject = new StringBuilder(ItemUtils.getMessageLabel("NotificationNewRemark", locale));
                    if (process != null){
                        subject.append(": ").append(process.getNameEndElipse());
                    }                    
                    messagesFacade.createMessage(recipient, sender.getName(), sender.getEmail(), subject.toString(), cb, links);         
                });
        MsgUtils.succesMsg("MessageSentUsers");
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_NOTIFY;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Notify");
    }
    
    /* GETS & SETS */

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }    
    
    public DualListModel<User> getUsers() {         
        return users;
    }
    public void setUsers(DualListModel<User> users) {
        this.users = users;
    }
    
}
