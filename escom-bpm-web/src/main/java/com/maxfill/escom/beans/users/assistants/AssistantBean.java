package com.maxfill.escom.beans.users.assistants;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.user.User;
import com.maxfill.model.basedict.assistant.Assistant;
import com.maxfill.model.basedict.assistant.AssistantFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Сервисный бин для сущности "Заместитель"
 */
@Named
@SessionScoped
public class AssistantBean extends BaseDetailsBean<Assistant, User>{    
    private static final long serialVersionUID = -5283581947435927447L;

    @EJB
    private AssistantFacade assistantFacade;
    @Inject
    private UserBean userBean;    

    @Override
    public List<User> getGroups(Assistant item) {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return userBean;
    }

    @Override
    public Class<User> getOwnerClass() {
        return User.class;
    }

    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getLazyFacade() {
        return assistantFacade;
    }
    
}
