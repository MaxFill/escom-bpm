package com.maxfill.escom.system.services.update;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.core.Release;
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
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;

/* Контролер формы проверки версии системы */

@ViewScoped
@Named
public class CheckReleaseBean extends BaseViewBean{
    private static final long serialVersionUID = -5278789301121698576L;

    @EJB
    private UpdateInfo updateInfo;

    private String versionRelease;  //номер версии актуального релиза
    private String releaseNumber;   //номер актуального релиза
    private String pageRelease;     //страница актуального релиза
    private Date dateRelease;       //дата актуального релиза
    private String strDateRelease;  //служебное поле
    private Licence licence;
    private Release release;
    
    @Override
    protected void initBean(){
        licence = appBean.getLicence();
        release = appBean.getRelease();
        if (release != null) {
            // вызов wss сервиса через javascript
            String wccUrl = appBean.WSS_INFO_URL;
            PrimeFaces.current().executeScript("serverConnect('" + wccUrl + "')");
        }
    }

    /**
     * вызов wss сервиса через jetty
     * код оставлен для тестирования
     */
    public void test(){
        Map<String, String> releaseInfoMap = updateInfo.start(licence.getNumber(), appBean.WSS_INFO_URL);
        if (MapUtils.isEmpty(releaseInfoMap )) {
            onErrorConnect();
            return;
        }
        versionRelease = releaseInfoMap.get("version");
        releaseNumber = releaseInfoMap.get("number");
        pageRelease = releaseInfoMap.get("page");
        String dateStr = releaseInfoMap.get("date");
        dateRelease = DateUtils.convertStrToDate(dateStr,  "yyyy-MM-dd", getLocale());
    }

    /**
     * Вывод сообщения об успешном подключении к серверу
     */
     public void onServerConnect(){
         MsgUtils.succesMsg("ConnectionEstablished");
         PrimeFaces.current().executeScript("doSend('" + licence.getNumber() +"')");
     }

    /**
     * Вывод сообщения в случае неудачного соединения
     */
    public void onErrorConnect(){
        MsgUtils.errorMsg("NoGetRealiseInfo");
    }

    /**
     * Обработка события получения сообщения от удалённого сервера
     * Метод вызывается с экранной формы через javascript
     */
    public void updateReleaseInfo(){      
        if (StringUtils.isBlank(strDateRelease)){
            MsgUtils.errorMsg("NoGetRealiseInfo");
            return;
        }
        dateRelease = DateUtils.convertStrToDate(strDateRelease, "yyyy-MM-dd", getLocale());
        if (dateRelease == null){
            return;
        }
        if (dateRelease.compareTo(release.getReleaseDate()) > 0){
            MsgUtils.warnMsg("NeedUpdateSystem");
            if (!appBean.getNeedUpadateSystem()) {
                appBean.setNeedUpadateSystem(Boolean.TRUE);
                sessionBean.setCanShowNotifBar(Boolean.TRUE);
            }
        }
        if (dateRelease.compareTo(release.getReleaseDate()) == 0){
            MsgUtils.succesMsg("UsedActualVersion");
        }
        appBean.updateActualReleaseData(versionRelease, releaseNumber, pageRelease, dateRelease);
        PrimeFaces.current().ajax().update("mainFRM");
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
    public String getFormName() {
        return DictFrmName.FRM_CHECK_RELEASE;
    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("CheckUpdates");
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

    public Release getRelease() {
        return release;
    }
}
