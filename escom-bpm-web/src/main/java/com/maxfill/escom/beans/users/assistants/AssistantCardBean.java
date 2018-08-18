package com.maxfill.escom.beans.users.assistants;

import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.users.User;
import com.maxfill.model.users.assistants.Assistant;
import com.maxfill.model.users.assistants.AssistantFacade;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/**
 * Контролер формы "Карточка заместителя"
 */
@Named
@ViewScoped
public class AssistantCardBean extends BaseCardBeanGroups<Assistant, User>{    
    private static final long serialVersionUID = -891178799329541408L;

    @EJB
    private AssistantFacade assistantFacade;
    
    @Override
    public List<User> getGroups(Assistant item) {
        return null;
    }

    @Override
    protected BaseDictFacade getFacade() {
        return assistantFacade;
    }
    
    /**
     * Обработка события выбора пользователя на форме
     * @param event 
     */
    public void onChangeAssistant(SelectEvent event){
        if (event.getObject() instanceof String) return;
        List<User> items = (List<User>)event.getObject();
        if (items.isEmpty()) return;
        User assist = items.get(0);
        onItemChange();
        getEditedItem().setUser(assist);
    }
    public void onChangeAssistant(ValueChangeEvent event){
        User assist = (User) event.getNewValue();
        getEditedItem().setUser(assist);
    }

    @Override
    public Assistant getEditedItem() {
        return super.getEditedItem(); 
    }
    
    
}
