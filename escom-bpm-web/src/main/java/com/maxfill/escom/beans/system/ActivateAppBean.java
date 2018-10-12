package com.maxfill.escom.beans.system;

import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.licenses.ActivateApp;
import org.primefaces.PrimeFaces;

import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import java.io.Serializable;
import java.util.logging.Logger;

/* Контролер страницы активации программы */
@Named
@ViewScoped
public class ActivateAppBean implements Serializable{
    private static final long serialVersionUID = -6222739585119426846L;
    protected static final Logger LOGGER = Logger.getLogger(ActivateAppBean.class.getName());

    public static final String WSS_INFO_URL = "wss://escom-doc.ru:8999/EscomServices-1.0/activate";
    //public static final String WSS_INFO_URL = "wss://localhost:9443/EscomServices-1.0/activate";

    private String licNumber;
    private String licenseData;

    @EJB
    private ActivateApp activateApp;

    @Inject
    private ApplicationBean appBean;

    @Inject
    private SessionBean sessionBean;

    /**
     * Обработка события нажатия на кнопку "Активировать"
     */
    public void onActivate(){
        PrimeFaces.current().executeScript("init('" + WSS_INFO_URL +"')");
    }

    /**
     * Отправка запроса на активацию
     */
    private void sendActivateRequest(){
        String json = Json.createObjectBuilder()
                .add("number", licNumber)
                .add("key", new String(activateApp.makeKeyInfo()))
                .build()
                .toString();
        PrimeFaces.current().executeScript("doSend('" + json +"')");
    }

    /**
     * Активация лицензии
     */
    public void activateLicence(){
        //ToDo получили строку - нужно создать лицензионный файл
        if (licenseData.contains("ERROR")){
            MsgUtils.errorMessage(licenseData);
            return;
        }
        if (activateApp.activate(licNumber, licenseData)){
            appBean.initLicense();
            MsgUtils.succesMsg("ApplicationIsActivate");
            PrimeFaces.current().ajax().update("activateFRM:mainPanel");
        }
    }

    /**
     * Вывод сообщения об успешном подключении к серверу
     */
    public void onServerConnect(){
        MsgUtils.succesMsg("ConnectionEstablished");
        sendActivateRequest();
    }

    /**
     * Вывод сообщения в случае неудачного соединения
     */
    public void onErrorConnect(){
        MsgUtils.errorMsg("ConnectServerFailed");
    }

    public void onEnterApp(){
       sessionBean.redirectToPage(SysParams.MAIN_PAGE, Boolean.FALSE);
    }

    /* Gets & sets */

    public String getLicNumber() {
        return licNumber;
    }
    public void setLicNumber(String licNumber) {
        this.licNumber = licNumber;
    }

    public String getLicenseData() {
        return licenseData;
    }
    public void setLicenseData(String licenseData) {
        this.licenseData = licenseData;
    }
}
