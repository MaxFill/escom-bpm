package com.maxfill.escom.beans.notyfy;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.DictRoles;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.docs.DocCardBean;
import com.maxfill.escom.beans.processes.ProcessCardBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.core.messages.UserMessagesFacade;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.remark.Remark;
import com.maxfill.model.basedict.remark.RemarkFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.user.UserFacade;
import com.maxfill.utils.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.SelectEvent;
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
    private Remark remark;
    private Doc doc;    
    
    @Override
    public void doBeforeOpenCard(Map params){        
        List<User> source = new ArrayList<>();
        if (params.containsKey("remarkID")){
            Integer remarkID = Integer.valueOf((String)params.get("remarkID"));
            remark = remarkFacade.find(remarkID);
            if (remark != null){
                content = remark.getContent();
                process = remark.getProcess();                
            }
            message = MsgUtils.getMessageLabel("PleasePayAttentionMyRemark");
        } else             
            if (sourceBean != null){
                if (sourceBean instanceof ProcessCardBean){
                    process = ((ProcessCardBean) sourceBean).getEditedItem();
                }
                if (sourceBean instanceof DocCardBean){
                    doc = ((DocCardBean)sourceBean).getEditedItem();
                }
            }
        
        if (process != null){            
            source = processFacade.getUsersProcessRole(process, DictRoles.ROLE_CONCORDER, getCurrentUser());            
        } else {
            source = userFacade.findAll(getCurrentUser());
        }
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
        if (remark != null){
            links.add(remark.getOwner());   //ссылка на документ
        } else 
            if (doc != null){
                links.add(doc);
            }
        StringBuilder cb = new StringBuilder();
        cb.append("<h3>").append(message).append("<h3/>");
        cb.append("<br/>");
        cb.append(content);
        cb.append("<br/>");
        User sender = getCurrentUser();
        recipients.forEach(recipient -> { 
                    Locale locale = userFacade.getUserLocale(recipient);
                    StringBuilder subject = new StringBuilder();
                    if (remark != null){
                        subject.append(ItemUtils.getMessageLabel("NotificationNewRemark", locale));
                    } else 
                        if (process != null){
                            subject.append(ItemUtils.getMessageLabel("NotifyFromProcess", locale));
                            subject.append(": ").append(process.getNameEndElipse());
                        } else
                            if (doc != null){
                                subject.append(ItemUtils.getMessageLabel("NotifyFromDoc", locale));
                                subject.append(": ").append(doc.getNameEndElipse());
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
    
    public void onAddRecipients(SelectEvent event){
        List<User> selectedUsers = (List<User>) event.getObject();
        if (CollectionUtils.isNotEmpty(selectedUsers)){
            users.getTarget().addAll(selectedUsers);
        }
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
