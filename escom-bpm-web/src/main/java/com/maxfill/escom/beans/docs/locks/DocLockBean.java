package com.maxfill.escom.beans.docs.locks;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.attaches.AttacheFacade;
import com.maxfill.model.attaches.Attaches;
import com.maxfill.model.users.User;
import com.maxfill.services.webDav.WebDavRemainder;
import com.maxfill.utils.DateUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.PrimeFaces;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Установка и снятие блокировок с документов, файлы которых открываются через WebDav
 */
@Named
@ViewScoped
public class DocLockBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = 81770245606034024L;

    @EJB
    private AttacheFacade attacheFacade;
    @EJB
    private WebDavRemainder remainder;
    
    private Date lockDate;
    private String minLockDate;
    private String maxLockDate;
    private Attaches attache;
    private Integer modeUnLock = SysParams.MODE_UNLOCK_CREATE_VERSION;
    
    @Override
    protected void initBean() {
        Date maxDate = DateUtils.addDays(new Date(), 10); 
        maxLockDate = DateUtils.dateToString(maxDate, DateFormat.SHORT, DateFormat.MEDIUM, getLocale());
        Date minDate = DateUtils.addMinute(new Date(), 10);
        minLockDate = DateUtils.dateToString(minDate, DateFormat.SHORT, DateFormat.MEDIUM, getLocale()); 
    }

    @Override
    public void doBeforeOpenCard(Map<String, String> params){
        if (attache != null) return;
        Integer attacheId = Integer.valueOf(params.get("attache"));        
        attache = attacheFacade.find(attacheId);
        if (attache.getPlanUnlockDate() == null){
            lockDate = DateUtils.addDays(new Date(), 3);
        } else {
            lockDate = attache.getPlanUnlockDate();
        }
    }

    @Override
    public String getFormHeader() {
        return isAttacheLock() ? getLabelFromBundle("UnLock") : getLabelFromBundle("SettingLock");
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_DOC_LOCK;
    }
    
    public String makeLock(){
        User editor = sessionBean.getCurrentUser();
        remainder.createTimer(attache, editor, lockDate);
        openFile();
        return onCloseCard();
    }
    
    public String makeUnLock(){
        remainder.cancelTimer(attache, modeUnLock);        
        return onCloseCard();
    }     
    
    public void changeDatePlanLock(){
        remainder.changeTimer(attache, sessionBean.getCurrentUser(), lockDate);                       
        MsgUtils.succesMsg("TimerRestarted");
    }
    
    public void onChangeDateLock(SelectEvent event){
        Date newDate = (Date) event.getObject();        
        if (isAttacheLock() && isUserIsEditor()){            
            PrimeFaces.current().ajax().update("lockForm");
        }
    }
    
    private void openFile(){        
        String folder = getFolderLink();                                    
        StringBuilder command = new StringBuilder("EditDoc('");
        command.append(folder).append(attache.getName()).append("','").append(folder).append("');");        
        PrimeFaces.current().executeScript(command.toString());
        //"EditDoc('https://fil-pc:8443/modeshape-webdav/sample/default/newdoc.docx', 'https://localhost:8443/modeshape-webdav/sample/default/');"   
    }
    
    private String getFileLink(){
        String fileLink = getFolderLink() + attache.getName();
        return fileLink;
    }
    
    public void showFileLink(){
        StringBuilder command = new StringBuilder("CopyToClipboard('");
        command.append(getFileLink()).append("')");        
        PrimeFaces.current().executeScript(command.toString());
    }
    
    private String getFolderLink(){
        StringBuilder folder = new StringBuilder();        
            //ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
            //HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
            //String serverURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "").toString();
            String serverURL = conf.getServerURL();
            //String repoName = conf.getRepositoryName();
            folder.append(serverURL)
                //.append(repoName)
                .append(attache.getGuid())
                .append("/");
        return folder.toString();
    }               
        
    public boolean isAttacheLock(){
        return !Objects.equals(attache.getLockAuthor(), null);
    }    

    public boolean isUserIsEditor(){
        return Objects.equals(attache.getLockAuthor(), sessionBean.getCurrentUser());
    }
    
    public boolean isCanShowRestartTimerBtn(){        
        if (attache.getPlanUnlockDate() == null) return false;
        return isAttacheLock() && isUserIsEditor() && lockDate.after(new Date());
    }
    
    /* GETS & SETS */

    public Integer getModeUnLock() {
        return modeUnLock;
    }
    public void setModeUnLock(Integer modeUnLock) {
        this.modeUnLock = modeUnLock;
    }
            
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
