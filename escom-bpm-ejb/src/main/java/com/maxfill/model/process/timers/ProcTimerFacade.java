package com.maxfill.model.process.timers;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.schemes.Scheme;

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
}
