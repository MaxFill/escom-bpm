package com.maxfill.escom.beans.users.sessions;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.users.sessions.UsersSessions;

import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Сессии пользователей"
 */
@Named
@ViewScoped
public class UsersSessionsBean extends BaseViewBean{
    private static final long serialVersionUID = 127067622604654493L;

    private UsersSessions selectedSession;

    private List<UsersSessions> listSessions;

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

    @Override
    public String getFormName() {
        return DictFrmName.FRM_USER_SESSIONS;
    }

    /* gets & sets */

    public UsersSessions getSelectedSession() {
        return selectedSession;
    }
    public void setSelectedSession(UsersSessions selectedSession) {
        this.selectedSession = selectedSession;
    }
}
