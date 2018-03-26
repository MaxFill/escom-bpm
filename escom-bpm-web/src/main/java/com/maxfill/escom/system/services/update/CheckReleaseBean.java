package com.maxfill.escom.system.services.update;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.model.licence.Licence;
import com.maxfill.services.update.UpdateInfo;
import com.maxfill.utils.DateUtils;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;

/* Контролер формы проверки версии системы */

@ViewScoped
@Named
public class CheckReleaseBean extends BaseDialogBean{
    private static final long serialVersionUID = -5278789301121698576L;

    @Inject
    private ApplicationBean appBean;

    @Inject
    private SessionBean sessionBean;

    @EJB
    private UpdateInfo updateInfo;

    private String versionRelease;  //номер версии актуального релиза
    private String releaseNumber;   //номер актуального релиза
    private String pageRelease;     //страница актуального релиза
    private Date dateRelease;       //дата актуального релиза
    private String strDateRelease;  //служебное поле
    private Licence licence;
    
    @Override
    protected void initBean(){
       licence = appBean.getLicence();
       // вызов wss сервиса через javascript
       PrimeFaces.current().executeScript("init('" + appBean.WSS_INFO_URL +"')");
    }

    //вызов wss сервиса через jetty
    public void test(){
        Map<String, String> releaseInfoMap = updateInfo.start(licence.getLicenceNumber(), appBean.WSS_INFO_URL);
        if (MapUtils.isEmpty(releaseInfoMap )) {
            onErrorConnect();
            return;
        }
        versionRelease = releaseInfoMap.get("version");
        releaseNumber = releaseInfoMap.get("number");
        pageRelease = releaseInfoMap.get("page");
        String dateStr = releaseInfoMap.get("date");
        dateRelease = DateUtils.convertStrToDate(dateStr, sessionBean.getLocale());
    }

    /**
     * Вывод сообщения в случае неудачного соединения
     */
    public void onErrorConnect(){
        EscomMsgUtils.errorMsg("NoGetRealiseInfo");
    }

    /**
     * Обработка события получения сообщения от удалённого сервера
     * Метод вызывается с экранной формы через javascript
     */
    public void updateReleaseInfo(){      
        if (StringUtils.isBlank(strDateRelease)){
            EscomMsgUtils.errorMsg("NoGetRealiseInfo");
            return;
        }
        dateRelease = DateUtils.convertStrToDate(strDateRelease, sessionBean.getLocale());
        if (dateRelease == null){
            return;
        }
        if (dateRelease.compareTo(licence.getReleaseDate()) > 0){
            EscomMsgUtils.warnMsg("NeedUpdateProgram");
            if (!appBean.getNeedUpadateSystem()) {
                appBean.setNeedUpadateSystem(Boolean.TRUE);
                sessionBean.setCanShowNotifBar(Boolean.TRUE);
            }
        }
        if (dateRelease.compareTo(licence.getReleaseDate()) == 0){
            EscomMsgUtils.succesMsg("UsedActualVersion");
        }
        appBean.updateActualReleaseData(versionRelease, releaseNumber, pageRelease, dateRelease);
        PrimeFaces.current().ajax().update("centerFRM");
    }
    
    @Override
    public void onOpenCard(){}
    
    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);  
    }

    public void onGotoSupportPage(){
        try {
            if (!isCorrectSupportPage()){
                pageRelease = "https://escom-archive.ru";
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
