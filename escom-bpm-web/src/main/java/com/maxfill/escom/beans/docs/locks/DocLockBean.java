package com.maxfill.escom.beans.docs.locks;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
import com.maxfill.services.webDav.WebDavRemainder;
import com.maxfill.utils.DateUtils;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

@Named
@ViewScoped
public class DocLockBean extends BaseDialogBean{
    private static final long serialVersionUID = 81770245606034024L;

    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private WebDavRemainder remainder;
    
    private Date lockDate;
    private String minLockDate;
    private String maxLockDate;
    private Attaches attache;
    
    @Override
    protected void initBean() {
        Date maxDate = DateUtils.addDays(new Date(), 10); 
        maxLockDate = DateUtils.dateToString(maxDate, "");
        Date minDate = DateUtils.addMinute(new Date(), 10);
        minLockDate = DateUtils.dateToString(minDate, ""); 
    }

    @Override
    public void onOpenCard(){
        if (attache != null) return;
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Integer attacheId = Integer.valueOf(params.get("attache"));        
        attache = attacheFacade.find(attacheId);
        if (attache.getPlanUnlockDate() == null){
            lockDate = DateUtils.addDays(new Date(), 3);
        } else {
            lockDate = attache.getPlanUnlockDate();
        }
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_DOC_LOCK;
    }
    
    public String makeLock(){
        User editor = sessionBean.getCurrentUser();
        remainder.createTimer(attache, editor, lockDate);
        openFile();
        return onCloseCard();
    }
    
    public String makeUnLock(){
        remainder.cancelTimer(attache);        
        return onCloseCard();
    }
     
    public void changeDatePlanLock(){
        remainder.changeTimer(attache, sessionBean.getCurrentUser(), lockDate);                       
        EscomBeanUtils.SuccesMsgAdd("Successfully", "TimerRestarted");
    }
    
    public void onChangeDateLock(SelectEvent event){
        Date newDate = (Date) event.getObject();
        RequestContext requestContext = RequestContext.getCurrentInstance();
        if (isAttacheLock() && isUserIsEditor()){            
            requestContext.update("lockForm");
        }
    }
    
    private void openFile(){        
        String folder = getFolderLink();                                    
        StringBuilder command = new StringBuilder("EditDoc('");
        command.append(folder).append(attache.getName()).append("','").append(folder).append("');");
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute(command.toString());
        //"EditDoc('https://fil-pc:8443/modeshape-webdav/sample/default/newdoc.docx', 'https://localhost:8443/modeshape-webdav/sample/default/');"   
    }
    
    private String getFileLink(){
        String fileLink = getFolderLink() + attache.getName();
        return fileLink;
    }
    
    public void showFileLink(){
        StringBuilder command = new StringBuilder("CopyToClipboard('");
        command.append(getFileLink()).append("')");
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute(command.toString());
    }
    
    private String getFolderLink(){
        StringBuilder folder = new StringBuilder();        
            //ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
            //HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
            //String serverURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "").toString();
            String serverURL = conf.getServerURL();
            String repoName = conf.getRepositoryName();
            folder.append(serverURL)
                .append(repoName)
                .append(attache.getGuid())
                .append("/");
        return folder.toString();
    }               
        
    public boolean isAttacheLock(){
        return attache.getLockAuthor() != null;
    }    

    public boolean isUserIsEditor(){
        return Objects.equals(attache.getLockAuthor(), sessionBean.getCurrentUser());
    }
    
    public boolean isCanShowRestartTimerBtn(){        
        if (attache.getPlanUnlockDate() == null) return false;
        return isAttacheLock() && isUserIsEditor() && lockDate.after(new Date());
    }
    
    /* GETS & SETS */
        
    public Date getLockDate() {
        return lockDate;
    }
    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public String getMinLockDate() {
        return minLockDate;
    }
    public void setMinLockDate(String minLockDate) {
        this.minLockDate = minLockDate;
    }
    
    public String getMaxLockDate() {
        return maxLockDate;
    }    
    
}
