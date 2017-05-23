package com.maxfill.escom.system.services.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.escom.beans.ApplicationBean;
import com.maxfill.model.licence.Licence;
import com.maxfill.utils.DateUtils;
import com.maxfill.utils.EscomUtils;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.inject.Inject;

@Stateless
public class CheckNewVersion {
    protected static final Logger LOGGER = Logger.getLogger(CheckNewVersion.class.getName());
    @Inject
    private ApplicationBean appBean;
    
    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "15", dayOfMonth = "*", year = "*", minute = "20", second = "0", persistent = true)               
    protected void init(Timer timer){        
        LOGGER.log(Level.INFO, "CheckNewVersion service started!");
        if (checkNewVersionAvailable(appBean.getLicence())) {
            appBean.setNeedUpadateSystem(Boolean.TRUE);
        } 
        LOGGER.log(Level.INFO, "CheckNewVersion service completed!");
        //timer.cancel();
    }
        
    /* Установка признака наличия новой версии */
    private Boolean checkNewVersionAvailable(Licence licence){       
        String actualReleasesJSON = EscomUtils.getReleaseInfo(licence.getLicenceNumber()); 
        
        Map<String,String> releaseInfoMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            releaseInfoMap = objectMapper.readValue(actualReleasesJSON, HashMap.class);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (releaseInfoMap.isEmpty()){ 
            return false;
        }

        appBean.updateActualReleaseData(releaseInfoMap);

        Date dateRelease = licence.getReleaseDate();
        Date dateActual = licence.getActualReleaseDate();

        if (dateActual == null){
            dateActual = new Date();
            dateRelease = DateUtils.addMounth(dateRelease, 3);
        }
        
        return dateActual.after(dateRelease);
    }
}
