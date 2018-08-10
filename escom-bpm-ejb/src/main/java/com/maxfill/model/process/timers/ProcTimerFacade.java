package com.maxfill.model.process.timers;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Фасад для сущности "Таймер Процесса"
 * @author maksim
 */
@Stateless
public class ProcTimerFacade extends BaseFacade<ProcTimer>{

    public ProcTimerFacade() {
        super(ProcTimer.class);
    }
    
    public ProcTimer createTimer(Process process, Scheme scheme, String timerLinkUID){
        ProcTimer procTimer = new ProcTimer();        
        procTimer.setProcess(process);
        procTimer.setStartDate(process.getPlanExecDate());
        procTimer.setStartType("on_init");
        procTimer.setScheme(scheme);
        procTimer.setTimerLinkUID(timerLinkUID);
        return procTimer;
    }        
    
    /**
     * Установка в таймере даты следующего запуска
     * @param timer 
     */
    public void updateNextStart(ProcTimer timer){
        if ("no".equals(timer.getRepeatType())){
            timer.setStartDate(null);   //таймер больше не запускается
            return;
        }
        Date startDate = new Date();
        switch(timer.getStartType()){
             case "everyday":{
                 DateUtils.addDays(startDate, 1);                 
                 break;
             }
             case "everyweek":{
                 DateUtils.addWeeks(startDate, 1);
                 break;
             }
             case "everymounth":{
                 DateUtils.addMonths(startDate, 1);
                 break;
             }
        }
        switch (timer.getRepeatVariantType()){
            case "intime":{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(timer.getRepeatTime());
                int seconds = calendar.get(Calendar.SECOND);
                DateUtils.addSeconds(startDate, seconds);
                break;
            }
            case "each":{                     
                switch (timer.getRepeatEachType()){
                    case "minute":{
                        DateUtils.addSeconds(startDate, timer.getRepeatEachInterval() * 60);        
                        break;
                    }
                    case "hour":{
                        DateUtils.addSeconds(startDate, timer.getRepeatEachInterval() * 3600);
                        break;
                    }
                    case "day":{
                        DateUtils.addSeconds(startDate, timer.getRepeatEachInterval() * 86400);
                        break;
                    }
                }
           }
        }      
        
        timer.setStartDate(startDate);
    }
        
}