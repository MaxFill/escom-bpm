package com.maxfill.escom.system.services.update;

import com.maxfill.escom.beans.ApplicationBean;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервис проверки наличия обновлений программы
 */
@Stateless
public class CheckNewVersion {
    protected static final Logger LOGGER = Logger.getLogger(CheckNewVersion.class.getName());
    @Inject
    private ApplicationBean appBean;
    
    @Schedule(dayOfWeek = "Mon-Fri", month = "*", hour = "21", dayOfMonth = "*", year = "*", minute = "08", second = "0", persistent = true)               
    protected void init(Timer timer){
        try {
            LOGGER.log(Level.INFO, "Check new version service started.");
            appBean.checkNewVersionAvailable();
            LOGGER.log(Level.INFO, "Check new version service finished.");
        }catch (RuntimeException ex){
            LOGGER.log(Level.INFO, ex.getMessage());
        }
        //timer.cancel();
    }

}