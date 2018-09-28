package com.maxfill.escom.system.services.mail;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.lazyload.LazyLoadBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.BaseLazyFacade;
import com.maxfill.services.mail.MailBoxFacade;
import com.maxfill.services.mail.Mailbox;
import java.text.MessageFormat;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class MailBoxBean extends LazyLoadBean<Mailbox>{
    private static final long serialVersionUID = -3168942305502913635L;
    
    @EJB
    private MailBoxFacade mailBoxFacade;        

    @Override
    public String getFormName() {
        return DictFrmName.FRM_MAIL_BOX;
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("MessagesForSent");
    }    
    
    /**
     * Обработка команды очистки журнала
     */
    public void onClearData(){
        Integer countDelete = deleteItems();
        MsgUtils.succesFormatMsg("RemovedEntries", new Object[]{countDelete});
    }
    
    /**
     * Формирует сообщение для вывода в диалоге подтверждения очистки журнала
     * @return
     */
    public String clearEventsConfirmMsg(){
        Object[] params = new Object[]{countItems()};
        return MessageFormat.format(MsgUtils.getBandleLabel("WillBeDeleted"), params);
    }

    @Override
    protected BaseLazyFacade getLazyFacade() {
        return mailBoxFacade;
    }
}