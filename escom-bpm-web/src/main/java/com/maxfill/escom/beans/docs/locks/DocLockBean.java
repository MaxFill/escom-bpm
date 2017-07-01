package com.maxfill.escom.beans.docs.locks;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.docs.DocCardBean;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.utils.DateUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class DocLockBean extends BaseDialogBean{
    private static final long serialVersionUID = 81770245606034024L;

    private Date lockDate = DateUtils.addDays(new Date(), 1);
    
    @Override
    protected void initBean() {
    }

    @Override
    public void onOpenCard(){
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_DOC_LOCK;
    }
    
    public void makeLock(){
        // создать (или скорректировать если уже есть) таймер напоминания и авто/разблокировки        
        // записать в attache информацию о блокировке (автор, дата начала, плпновый срок разблокировки) и таймере
        // скопировать файл в хранилище в папку        
        // открыть файл на редактирование
        onCloseCard();
    }
    
    public String onGetExternalLink(Attaches attache){
        String path = conf.getUploadPath() + attache.getFullName();
        String link = "";
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext extContext = facesContext.getExternalContext();
            
            link = new URL(extContext.getRequestScheme(),
                    extContext.getRequestServerName(),
                    extContext.getRequestServerPort(), "").toString();            
        } catch (MalformedURLException ex) {
            Logger.getLogger(DocCardBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return link; 
    }

    public Date getLockDate() {
        return lockDate;
    }
    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }
 
    
}
