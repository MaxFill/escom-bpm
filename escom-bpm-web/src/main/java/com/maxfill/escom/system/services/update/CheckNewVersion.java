
package com.maxfill.escom.system.services.update;

import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.services.update.WSInfoClient;
import com.maxfill.utils.DateUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.inject.Inject;

/**
 * Таймер проверки актуальности лицензии
 * @author mfilatov
 */
@Stateless
public class CheckNewVersion {
    protected static final Logger LOG = Logger.getLogger(CheckNewVersion.class.getName());
    @Inject
    private ApplicationBean appBean;
    
    //@Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "9-17", dayOfMonth = "*", year = "*", minute = "*/5", second = "0", persistent = true)               
    protected void init(Timer timer){
        //timer.cancel();
        LOG.log(Level.INFO, "CheckNewVersion service started!");
        if (newVersionAvailable()) {
            appBean.setNeedUpadateSystem(Boolean.TRUE);
        } 
        LOG.log(Level.INFO, "CheckNewVersion service completed!");
    }
        
    /**
     * Установка признака наличия новой версии
     * @return 
     */
    private Boolean newVersionAvailable(){    
        String versionNumber = appBean.getLicence().getVersionNumber();
        String releaseNumber = appBean.getLicence().getReleaseNumber();
        Date dateLastUpdate = appBean.getLicence().getDateUpdate();
        String actualVersion = "";
        String actualRelease = "";
        HashMap<String, Object> versionInfo = onGetCurrentVersionMap();
        if (!versionInfo.isEmpty()){ 
            actualVersion = (String) versionInfo.get("Version");
            actualRelease = (String) versionInfo.get("Release");
        }
        if (!Objects.equals(versionNumber, actualVersion)){
            return true;
        }
        if (!Objects.equals(releaseNumber, actualRelease) && badDateUpdate(dateLastUpdate, 1)){
            return true;
        }
        if (badDateUpdate(dateLastUpdate, 3)){
            return true;
        }
        return false;
    }
    
    /**
     * Получение данных от web socket 
     * @return 
     */
    private HashMap<String, Object> onGetCurrentVersionMap(){
        LOG.log(Level.INFO, "get connected to web socket...");
        try {
            // open websocket
            final WSInfoClient clientEndPoint = new WSInfoClient(new URI("wss://real.okcoin.cn:10440/websocket/okcoinapi/"));

            // add listener
            clientEndPoint.addMessageHandler(new WSInfoClient.MessageHandler() {
                @Override
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
            clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");

            // wait 5 seconds for messages from websocket
            Thread.sleep(5000);

        } catch (InterruptedException | URISyntaxException ex) {
            Logger.getLogger(CheckNewVersion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String version = "";
        String release = "";
        HashMap<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("Version", version);
        versionInfo.put("Release", release);
        return versionInfo;
    }
    
    /**
     * Проверяет просроченность даты обновления версии
     * @param dateLastUpdate
     * @return 
     */
    private Boolean badDateUpdate(Date dateLastUpdate, Integer mounthCount){
        Date checkDate = DateUtils.addMounth(dateLastUpdate, mounthCount);
        Date currentDate = new Date();
        if (checkDate.after(currentDate)) {
            return true;
        }
        return false;
    }
    
}
