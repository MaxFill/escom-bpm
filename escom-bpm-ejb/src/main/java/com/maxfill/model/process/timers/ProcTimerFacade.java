package com.maxfill.model.process.timers;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;
import com.maxfill.model.process.Process;

/**
 * Фасад для сущности "Таймер Процесса"
 * @author maksim
 */
@Stateless
public class ProcTimerFacade extends BaseFacade<ProcTimer>{

    public ProcTimerFacade() {
        super(ProcTimer.class);
    }
    
    public ProcTimer createTimer(Process process){
        ProcTimer procTimer = new ProcTimer();        
        procTimer.setProcess(process);
        procTimer.setStartDate(process.getPlanExecDate());
        procTimer.setStartType("on_init");
        return procTimer;
    }
}
