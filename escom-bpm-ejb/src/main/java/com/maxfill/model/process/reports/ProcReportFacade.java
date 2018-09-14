package com.maxfill.model.process.reports;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 *
 * @author maksim
 */
@Stateless
public class ProcReportFacade extends BaseFacade<ProcReport>{

    public ProcReportFacade() {
        super(ProcReport.class);
    }
    
}
