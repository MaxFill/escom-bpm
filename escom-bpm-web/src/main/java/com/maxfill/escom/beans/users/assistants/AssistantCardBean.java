package com.maxfill.escom.beans.users.assistants;

import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.assistants.Assistant;
import com.maxfill.model.users.assistants.AssistantFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Контролер формы "Карточка заместителя"
 */
@Named
@ViewScoped
public class AssistantCardBean extends BaseCardBeanGroups<Assistant, User>{    
    private static final long serialVersionUID = -891178799329541408L;

    @EJB
    private AssistantFacade assistantFacade;
    @Inject
    private UserBean userBean;
    
    @Override
    public List<User> getGroups(Assistant item) {
        return item.getChiefs();
    }

    @Override
    protected BaseDictFacade getFacade() {
        return assistantFacade;
    }
    
}
