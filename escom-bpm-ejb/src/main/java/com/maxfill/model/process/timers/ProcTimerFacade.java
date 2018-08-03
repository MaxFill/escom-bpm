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
    
    public ProcTimer createTimer(Process process, String name){
        ProcTimer procTimer = new ProcTimer();
        procTimer.setName(name);
        procTimer.setProcess(process);
        procTimer.setStartDate(process.getPlanExecDate());
        return procTimer;
    }
}
