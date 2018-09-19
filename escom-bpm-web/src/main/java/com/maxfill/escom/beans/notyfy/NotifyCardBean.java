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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    private DualListModel<User> users;     
    private String content;
    private Process process;
    
    @Override
    public void doBeforeOpenCard(Map params){        
        List<User> source = new ArrayList<>();
        if (params.containsKey("remarkID")){
            Integer remarkID = Integer.valueOf((String)params.get("remarkID"));
            Remark remark = remarkFacade.find(remarkID);
            if (remark != null){
                content = remark.getContent();
                process = remark.getProcess();
                if (process != null){
                    source.addAll(processFacade.actualiseRole(process, DictRoles.ROLE_CONCORDER, getCurrentUser()));
                }
            }
        }
        users = new DualListModel<>(source, new ArrayList<>());
    }
    
    /**
     * Отправка уведомлений
     */
    public void onSend(){
        List<User> recipients = users.getTarget();
        if (recipients.isEmpty()) return;
        Map<String, BaseDict> links = new HashMap<>();
        if (process != null){
            links.put("process", process);
        }
        String subject = MsgUtils.getMessageLabel("NotificationNewRemark");
        StringBuilder sb = new StringBuilder(content);
        messagesFacade.sendMessageUsers(recipients, getCurrentUser(), subject, sb, links);
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
    
    public DualListModel<User> getUsers() {         
        return users;
    }
    public void setUsers(DualListModel<User> users) {
        this.users = users;
    }
    
}
