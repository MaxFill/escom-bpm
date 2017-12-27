package com.maxfill.escom.system.services.update;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.licence.Licence;
import com.maxfill.utils.DateUtils;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;

@ViewScoped
@Named
public class CheckReleaseBean extends BaseDialogBean{
    private static final long serialVersionUID = -5278789301121698576L;

    @Inject
    private ApplicationBean appBean; 
    
    private String versionRelease;  //номер версии актуального релиза
    private String releaseNumber;   //номер актуального релиза
    private String pageRelease;     //страница актуального релиза
    private Date dateRelease;       //дата актуального релиза
    private String strDateRelease;  //служебное поле
    private Licence licence;
    
    @Override
    protected void initBean(){       
       licence = appBean.getLicence();
    }
    
    public void updateReleaseInfo(){      
        if (StringUtils.isBlank(strDateRelease)){
            EscomMsgUtils.errorMsgAdd("Error", "NoGetRealiseInfo", "");
            return;
        }
        dateRelease = DateUtils.convertStrToDate(strDateRelease, sessionBean.getLocale());
        if (dateRelease == null){
            return;
        }
        if (dateRelease.compareTo(licence.getReleaseDate()) > 0){
            EscomMsgUtils.warnMsgAdd("NewVersionAvailable", "NeedUpdateProgram");
            appBean.setNeedUpadateSystem(Boolean.TRUE);
            sessionBean.setCanShowNotifBar(Boolean.TRUE);
        }
        if (dateRelease.compareTo(licence.getReleaseDate()) == 0){
            EscomMsgUtils.succesMsgAdd("Successfully", "UsedActualVersion");
        }
        appBean.updateActualReleaseData(versionRelease, releaseNumber, pageRelease, dateRelease);
        RequestContext.getCurrentInstance().update("checkFRM");
    }
    
    @Override
    public void onOpenCard(){
    }
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);  
    }

    public void onGotoSupportPage(){
        try {
            if (!isCorrectSupportPage()){
                pageRelease = "http://www.escom-bpm.com";
            }
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect(pageRelease);
        } catch (IOException ex) {
            Logger.getLogger(CheckReleaseBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isCorrectSupportPage(){
        return StringUtils.isNotBlank(pageRelease);
    }
    
    /* GETS & SETS */
    
    @Override
    protected String getFormName() {
        return DictDlgFrmName.FRM_CHECK_RELEASE;
    }

    public String getStrDateRelease() {
        return strDateRelease;
    }
    public void setStrDateRelease(String strDateRelease) {
        this.strDateRelease = strDateRelease;
    }
    
    public String getReleaseNumber() {
        return releaseNumber;
    }
    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
    }

    public String getPageRelease() {
        return pageRelease;
    }
    public void setPageRelease(String pageRelease) {
        this.pageRelease = pageRelease;
    }

    public String getVersionRelease() {
        return versionRelease;
    }
    public void setVersionRelease(String versionRelease) {
        this.versionRelease = versionRelease;
    }
    
    public Date getDateRelease() {
        return dateRelease;
    }
    public void setDateRelease(Date dateRelease) {
        this.dateRelease = dateRelease;
    }

    public Licence getLicence() {
        return licence;
    }
    
}
