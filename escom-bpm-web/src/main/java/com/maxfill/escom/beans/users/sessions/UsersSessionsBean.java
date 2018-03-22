package com.maxfill.escom.beans.users.sessions;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.model.users.sessions.UsersSessions;
import com.maxfill.escom.beans.ApplicationBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.model.layout.LayoutOptions;

/**
 * Контролер формы "Сессии пользователей"
 * @author mfilatov
 */
@Named
@ViewScoped
public class UsersSessionsBean extends BaseDialogBean{
    private static final long serialVersionUID = 127067622604654493L;

    private UsersSessions selectedSession;

    private List<UsersSessions> listSessions;
            
    @Inject 
    private ApplicationBean appBean;

    @Override
    protected void initBean() {
    }

    public List<UsersSessions> getListSessions() {           
        if (listSessions == null){
            listSessions = new ArrayList<>();
            appBean.getUserSessions().forEach(
                    (key, usSession) -> listSessions.add(usSession)
            );
        }
        return listSessions;
    } 
    
    public void refreshData(){
       listSessions = null; 
    }
    
    /**
     * Закрытие формы
     */
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_USER_SESSIONS;
    }

    /* gets & sets */

    public UsersSessions getSelectedSession() {
        return selectedSession;
    }
    public void setSelectedSession(UsersSessions selectedSession) {
        this.selectedSession = selectedSession;
    }
}
